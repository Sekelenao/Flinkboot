# Flinkboot How-To Guides

This section contains step-by-step guides to help you implement specific features and configurations in Flinkboot.

> [!TIP]
> **Looking for a ready-to-run template?** Explore the [Flinkboot Quickstart repository](https://github.com/Sekelenao/Flinkboot-Quickstart) for a complete end-to-end streaming application showcasing multi-source configuration, Kafka connectors, and native POJO serialization.

## Index

- [How to Load a Flag](load-a-flag.md) — Learn how to define, read, and override boolean flags via CLI and environment variables.
- [How to Load a Parameter](load-a-parameter.md) — Learn how to define and retrieve key-value parameters via CLI and environment variables.
- [How to Load & Merge Configurations](load-configurations.md) — Learn how to load, merge, and validate YAML configurations.
- [How to Configure the Execution Environment](configure-execution-environment.md) — Learn how to configure and instantiate Flink's StreamExecutionEnvironment.
- [How to Configure a Kafka Source](configure-kafka-source.md) — Learn how to configure and build a Kafka Source with custom offset strategies.
- [How to Configure a Kafka Sink](configure-kafka-sink.md) — Learn how to configure and build a Kafka Sink with custom delivery guarantees.
- [How to Assert Flink POJO Compliance](assert-pojo-compliance.md) — Verify that your data classes are compatible with Flink's optimized POJO serializer.
- [How to Serialize Common JDK Types](serialize-jdk-types.md) — Learn how to serialize Java Date/Time, Duration, and Collections natively using Flinkboot's built-in factories.
- [How to Load Configurations in Tests](load-configurations-in-tests.md) — Easily load, merge, and validate YAML configurations within JUnit 5 tests.
- [How to Load Resources](load-resources.md) — Load files and assets seamlessly across classpath and file systems with a unified URI syntax.
- [Avoid Classpath & Dependency Conflicts](avoid-dependency-conflicts.md) — Best practices for shading and aligning dependencies in production.
- [Flinkboot BOM & Managed Dependencies](bom-managed-dependencies.md) — Complete breakdown of all dependencies, versions, and scopes managed by the BOM.
- [Reserved Keys & Configuration Properties](reserved-keys.md) — View the configuration keys and environment variables reserved by Flinkboot.
