---
name: connectors
description: "Enforce Flinkboot connector property and factory design rules separating core infrastructure and Flink semantics from vendor client tuning. Use when designing, implementing, or reviewing Flinkboot connector properties DTOs or factories."
---

# Connector Architecture

Follow these rules when designing, implementing, or reviewing Flinkboot connector `*Properties` DTOs and `*Factory` classes.

## Top-Level Property Scope

- Declare top-level fields on connector `*SourceProperties` and `*SinkProperties` DTOs only for:
  - DAG identity: the Flink operator name (`name` with `@NotBlank`).
  - Infrastructure coordinates: stable connection endpoints (such as `bootstrapServers`, `url`, or `hosts`).
  - Logical target entities: core resources being read or written (such as `topic`, `database`, `table`, or `warehouse`).
  - Flink execution semantics: options that directly invoke specialized Flink runtime APIs (such as `startupMode`, `deliveryGuarantee`, or `transactionalIdPrefix`).
- Keep top-level structure symmetric across all connectors.

## Forbidden Top-Level Tuning Options

- Never promote vendor client tuning knobs or SDK-specific options to top-level DTO fields (such as batching, buffer sizes, linger times, retries, acks, compression, network timeouts, or fetcher/scanner tuning).
- Leave vendor-specific parsing and validation to the underlying client SDK rather than duplicating third-party option rules in Flinkboot.

## Universal Properties Escape Hatch

- Include an unmodifiable `Map<@NotBlank String, @NotNull String> properties` field on every connector DTO for all vendor-specific client tuning.
- Pass `config.properties()` directly into the underlying Flink or vendor builder in the connector factory, and apply it last so explicit Flink semantic options are not silently misconfigured.
