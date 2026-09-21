---
name: connectors
description: Strict architectural rules for designing Flinkboot connectors (Kafka, Fluss, Iceberg, JDBC). Distinguishes immutable infrastructure/semantic options from vendor client tuning.
---

# Connector Architecture & Design Rules

Flinkboot provides typed, immutable, and fail-fast configuration wrappers for Apache Flink sources and sinks (e.g. Kafka, Fluss, Iceberg, JDBC).

Flinkboot is NOT an exhaustive mirror or wrapper of third-party vendor SDKs. It is an opinionated configuration layer designed around a strict separation of concerns.

---

## 1. The Golden Rule of Connector Properties

Connector properties DTOs (*SourceProperties, *SinkProperties) must ONLY declare top-level fields for:
1. DAG Identity: The Flink operator name.
2. Infrastructure Coordinates: Stable endpoints (e.g. brokers, endpoints, URLs).
3. Logical Target Entities: Core domain targets (e.g. topics, tables, databases).
4. Flink-Level Semantics: Options that directly configure Flink's execution graph, checkpointing, or watermark APIs.

ALL client performance tuning, buffer sizing, network timeouts, and vendor-specific options MUST go into the universal properties map.

---

## 2. What Belongs in Top-Level Fields

| Category | Description | Examples |
|---|---|---|
| DAG Topology | Operator name in the Flink execution graph. | name (@NotBlank) |
| Infrastructure Coordinates | Stable connection coordinates that define where the service lives. | bootstrapServers (@NotEmpty List<@NotBlank String>), url, hosts |
| Logical Target Entities | The logical resources being read or written. | topic (Kafka), database & table (Fluss, JDBC), warehouse & path (Iceberg) |
| Flink Execution Semantics | Invariants that directly trigger specialized Flink runtime API methods (e.g. offsets initialization, 2-phase commit). | startupMode (EARLIEST, LATEST, TIMESTAMP), deliveryGuarantee (EXACTLY_ONCE, AT_LEAST_ONCE), transactionalIdPrefix |

---

## 3. What is STRICTLY FORBIDDEN at Top Level (Anti-Patterns)

NEVER promote client tuning knobs or vendor-specific options to first-class fields in *Properties DTOs:

* Batching & Buffers: Do NOT add batch-size, batch-timeout, linger-ms, buffer-memory, max-request-size.
* Network & Reliability: Do NOT add acks, retries, compression-type, request-timeout, keep-alive.
* Fetcher & Scanner Settings: Do NOT add fetch-min-bytes, fetch-max-wait-ms, max-poll-records.

### Why this rule is absolute:
1. No Arbitrary Favoritism: Each client SDK (Kafka Producer, Fluss Writer, Iceberg Catalog, HikariCP) has 40 to 100 tuning knobs. Elevating one (like batch-size) over another (like compression or acks) is arbitrary and creates inconsistent DTOs.
2. Zero Vendor Drift: Vendor libraries frequently change property names, deprecate keys, or alter parsing formats across versions. Keeping tuning options in properties ensures Flinkboot never breaks when upstream libraries evolve.
3. No Redundant Validation Complexity: The vendor client already has its own comprehensive parsing and validation logic with accurate diagnostic error messages. Flinkboot should not maintain complex regexes (e.g. memory units) for third-party options.

---

## 4. The Universal Escape Hatch: properties: Map<String, String>

Every connector DTO must provide an unmodifiable properties map:

```java
private final Map<@NotNull String, @NotNull String> properties;
```

- Users configure all vendor-specific tuning directly under properties in their YAML:
  ```yaml
  properties:
    client.writer.batch-size: "1mb"
    client.writer.batch-timeout: "50ms"
    acks: "all"
    compression.type: "zstd"
  ```
- Connector factories (*Factory) pass this map directly into the vendor or Flink builder:
  ```java
  builder.setOptions(config.properties()); // or setProperties(...)
  ```

---

## 5. Symmetry Across All Connectors

All connectors must exhibit identical symmetry:

```yaml
# Kafka Sink
name: "kafka-sink"
bootstrap-servers: ["localhost:9092"]
topic: "events"
delivery-guarantee: "AT_LEAST_ONCE"
properties:
  compression.type: "snappy"
  linger.ms: "20"

# Fluss Sink
name: "fluss-sink"
bootstrap-servers: ["localhost:9123"]
database: "analytics"
table: "events"
properties:
  client.writer.batch-size: "1mb"
  client.writer.batch-timeout: "50ms"
```
