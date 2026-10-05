# How to Configure a Kafka Source

Flinkboot provides typed configuration models and a factory to easily initialize Apache Flink's `KafkaSource` directly from YAML configuration files, supporting both infinite streaming and finite/batch execution modes.

---

## Maven Dependencies

Import the Flinkboot BOM in your `<dependencyManagement>` and add the Flinkboot Kafka module along with Flink APIs:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.sekelenao</groupId>
            <artifactId>flinkboot</artifactId>
            <version>${flinkboot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Flinkboot Kafka -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-kafka</artifactId>
    </dependency>

    <!-- Flink Streaming API (Provided by Flink cluster) -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-java</artifactId>
    </dependency>
</dependencies>
```

---

## 1. YAML Configuration Properties & Structure

You can configure your Kafka Source using either a static list of topics or a topic pattern regex with `KafkaSourceProperties`.

### Option A: Infinite Streaming from Earliest Offsets

```yaml
name: "my-kafka-source"
bootstrap-servers:
  - "localhost:9092"
group-id: "my-consumer-group"
topics:
  - "users"
  - "orders"
starting-offsets:
  strategy: EARLIEST
properties:
  session.timeout.ms: "45000"
```

### Option B: Bounded Batch Execution with Stopping Offsets

```yaml
name: "batch-kafka-source"
bootstrap-servers:
  - "localhost:9092"
group-id: "analytics-batch-group"
topic-pattern: "^analytics-.*$"
boundedness: BOUNDED
starting-offsets:
  strategy: TIMESTAMP
  timestamp: 1689717600000
stopping-offsets:
  strategy: TIMESTAMP
  timestamp: 1689721200000
```

---

## 2. Configuration Parameters Reference

| Property Key        | Type                    | Required                     | Validation                     | Description                                                                                                        |
|:--------------------|:------------------------|:-----------------------------|:-------------------------------|:-------------------------------------------------------------------------------------------------------------------|
| `name`              | String                  | **Yes**                      | `@NotBlank`                    | Logical name of the Kafka source configuration and Flink operator name.                                            |
| `bootstrap-servers` | List of Strings         | **Yes**                      | `@NotEmpty`, items `@NotBlank` | Kafka bootstrap broker hosts/ports (e.g. `localhost:9092`).                                                        |
| `group-id`          | String                  | **Yes**                      | `@NotBlank`                    | Consumer group ID.                                                                                                 |
| `topics`            | List of Strings         | **Yes** (Mutually exclusive) | items `@NotBlank`              | Static list of topics to subscribe to (mutually exclusive with `topic-pattern`).                                   |
| `topic-pattern`     | String                  | **Yes** (Mutually exclusive) | `@Pattern` regex               | Regex pattern to match topic subscriptions (mutually exclusive with `topics`).                                     |
| `starting-offsets`  | `KafkaOffsetProperties` | **Yes**                      | `@NotNull @Valid`              | Strategy and parameters for initial offset positioning.                                                            |
| `boundedness`       | `KafkaBoundedness`      | No                           | Enum                           | Execution boundedness mode (`BOUNDED`, `UNBOUNDED`). Defaults to `UNBOUNDED`.                                      |
| `stopping-offsets`  | `KafkaOffsetProperties` | No                           | `@Valid`                       | Strategy and parameters for stopping offset positioning. **Mandatory** when `boundedness` is `BOUNDED`.            |
| `properties`        | Map                     | No                           | Keys/values `@NotNull`         | Universal escape hatch for vendor-specific Kafka consumer tuning (e.g. `fetch.max.wait.ms`, `session.timeout.ms`). |

---

## 3. Offset Properties (`KafkaOffsetProperties`)

Both `starting-offsets` and `stopping-offsets` are configured as structured, encapsulated objects with the following schema:

| Property Key | Type            | Required    | Validation        | Description                                                                                                           |
|:-------------|:----------------|:------------|:------------------|:----------------------------------------------------------------------------------------------------------------------|
| `strategy`   | Enum            | **Yes**     | `@NotNull`        | Offset strategy: `EARLIEST`, `LATEST`, `COMMITTED`, `COMMITTED_EARLIEST`, `COMMITTED_LATEST`, `TIMESTAMP`, `OFFSETS`. |
| `timestamp`  | Long            | Conditional | `@PositiveOrZero` | Timestamp in epoch milliseconds. **Mandatory** only if `strategy` is `TIMESTAMP` (otherwise forbidden).               |
| `partitions` | List of Objects | Conditional | `@Valid` items    | Explicit partition offset assignments. **Mandatory** only if `strategy` is `OFFSETS` (otherwise forbidden).           |

### Strategies Overview

| Value                | Description                                                                          | Required Parameter |
|:---------------------|:-------------------------------------------------------------------------------------|:-------------------|
| `EARLIEST`           | Start or stop consuming at the earliest offset.                                      | None               |
| `LATEST`             | Start or stop consuming at the latest offset (log end).                              | None               |
| `COMMITTED`          | Start from committed consumer group offsets. Defaults to latest if none found.       | None               |
| `COMMITTED_EARLIEST` | Start from committed consumer group offsets, falling back to earliest if none found. | None               |
| `COMMITTED_LATEST`   | Start from committed consumer group offsets, falling back to latest if none found.   | None               |
| `TIMESTAMP`          | Target a specific epoch timestamp across all assigned partitions.                    | `timestamp`        |
| `OFFSETS`            | Target custom offsets specified per topic-partition.                                 | `partitions`       |

### Timestamp Configuration Example

```yaml
starting-offsets:
  strategy: TIMESTAMP
  timestamp: 1689717600000 # Epoch millisecond timestamp
```

### Partition-Specific Offsets Example

```yaml
starting-offsets:
  strategy: OFFSETS
  partitions:
    - topic: "orders"
      partition: 0
      offset: 12345
    - topic: "orders"
      partition: 1
      offset: 23456
```

---

## 4. Dual-Mode Boundedness & Stopping Offsets

Apache Flink's native `KafkaSourceBuilder` natively supports dual-mode execution:

1. **Continuous Streaming (`UNBOUNDED`, default)**:
   - Without `stopping-offsets`: Consumes continuously and indefinitely.
   - With `stopping-offsets` (Finite Streaming): Executes in streaming mode with checkpoints and watermarks, but cleanly terminates upon reaching the stopping offsets.
2. **Batch / Backfill (`BOUNDED`)**:
   - Requires `stopping-offsets`. Configures `KafkaSourceBuilder.setBounded(stoppingOffsets)`, enabling Flink's batch runtime (`RuntimeExecutionMode.BATCH`), finite backfills, and automatic job completion.

---

## 5. Java Integration

Embed `KafkaSourceProperties` inside your application's root configuration class:

### Step 1: Define Root Configuration Model

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public final class MyJobConfig {

    @Valid
    @NotNull
    private final KafkaSourceProperties kafka;

    @JsonCreator
    public MyJobConfig(@JsonProperty("kafka") KafkaSourceProperties kafka) {
        this.kafka = kafka;
    }

    public KafkaSourceProperties kafka() { return kafka; }
}
```

### Step 2: Build KafkaSource in Flink Application

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.kafka.api.source.KafkaSourceFactory;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

public class KafkaConsumerJob {
    public static void main(String[] args) throws Exception {
        Flinkboot boot = Flinkboot.initialize(args);
        
        // 1. Load configuration from job-configuration.yaml
        MyJobConfig config = boot.configuration(MyJobConfig.class);
        
        // 2. Define your deserialization schema
        KafkaRecordDeserializationSchema<String> schema = 
            KafkaRecordDeserializationSchema.valueOnly(new SimpleStringSchema());
        
        // 3. Instantiate Flink Kafka Source from properties
        KafkaSource<String> kafkaSource = KafkaSourceFactory.supplyFor(config.kafka(), schema);
        
        // ... build your Flink pipeline
    }
}
```

### Programmatic Customization
If you need to customize Flink's builder (e.g. client ID prefix, custom properties) before building:

```java
KafkaSource<String> customKafkaSource = KafkaSourceFactory.supplyBuilderFor(config.kafka(), schema)
    .setClientIdPrefix("custom-client-id")
    .build();
```

---

## 6. Fail-Fast Validation & Invariants

- **Mutual Exclusivity:** Configuring both `topics` and `topic-pattern` or configuring neither fails fast during validation (reported on `topic-pattern` or `topics`).
- **Boundedness Requirement:** When `boundedness` is set to `BOUNDED`, omitting `stopping-offsets` fails fast (reported on `stopping-offsets`).
- **Offset Strategy Invariants:**
  - `strategy: TIMESTAMP` strictly requires `timestamp` and forbids `partitions`.
  - `strategy: OFFSETS` strictly requires `partitions` and forbids `timestamp`.
  - Standard strategies (`EARLIEST`, `LATEST`, etc.) forbid specifying `timestamp` or `partitions`.
- **Nested Container Validation:** Invalid partition offsets (negative partition index or null topic) or empty broker collections fail fast before DAG submission.
