# Flinkboot How-To Guides

This section contains step-by-step guides to help you implement specific features and configurations in Flinkboot.

> [!NOTE]
> **Version-Specific Guides**: These guides reflect the `main` branch (latest development state). If you are using a published version of Flinkboot, please **switch to your version's Git tag** (e.g. [`v0.4.0-1.20`](https://github.com/Sekelenao/Flinkboot/releases)) to ensure configuration keys and APIs match your dependencies.

## Project Setup & Packaging

- [Avoid Classpath & Dependency Conflicts](setup/avoid-dependency-conflicts.md) — Best practices for Maven shading, relocations, and Fat JAR packaging.
- [Flinkboot BOM & Managed Dependencies](setup/bom-managed-dependencies.md) — Complete breakdown of all dependencies, versions, and scopes managed by the BOM.

---

## Configuration & Environment

- [How to Load & Merge Configurations](configuration/load-configurations.md) — Load, merge, and validate YAML configurations into strongly-typed Java models.
- [How to Load a Parameter](configuration/load-a-parameter.md) — Define and retrieve key-value parameters via CLI and environment variables.
- [How to Load a Flag](configuration/load-a-flag.md) — Define, read, and override boolean flags via CLI and environment variables.
- [How to Load Resources](configuration/load-resources.md) — Load files and assets seamlessly across classpath and file systems with unified URI syntax.
- [Reserved Keys & Configuration Properties](configuration/reserved-keys.md) — View configuration keys and environment variables reserved by Flinkboot.
- [How to Configure the Execution Environment](configuration/configure-execution-environment.md) — Configure and instantiate Flink's `StreamExecutionEnvironment` with zero boilerplate.

---

## Connectors: Apache Kafka

- [How to Configure a Kafka Source](kafka/configure-kafka-source.md) — Configure and build a Kafka Source with custom offset strategies and fail-fast checks.
- [How to Configure a Kafka Sink](kafka/configure-kafka-sink.md) — Configure and build a Kafka Sink with delivery guarantees and custom serializers.

---

## Connectors: Apache Fluss

- [How to Configure a Fluss Source](fluss/configure-fluss-source.md) — Configure and build an Apache Fluss Source with offset, timestamp, and snapshot strategies.
- [How to Configure a Fluss Sink](fluss/configure-fluss-sink.md) — Configure and build an Apache Fluss Sink with batch size and timeout tuning.

---

## Serialization & POJO Compliance

- [How to Assert Flink POJO Compliance](serialization/assert-pojo-compliance.md) — Recursively verify that data classes serialize natively without Kryo fallback.
- [How to Serialize Common JDK Types](serialization/serialize-jdk-types.md) — Serialize Java Date/Time, Duration, and Collections natively using Flinkboot's built-in factories.

---

## Testing

- [How to Load Configurations in Tests](testing/load-configurations-in-tests.md) — Load, merge, and validate YAML configurations within JUnit 5 unit tests.
