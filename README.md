# Flinkboot

> **Speed & Safety by design.** Zero-boilerplate configuration that fails fast to keep your Apache Flink pipelines running.

[![Java](https://img.shields.io/badge/Java_11-%23ED8B00.svg?logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)
[![Flink](https://img.shields.io/badge/Flink_1.20-%23E6526F.svg?logo=apacheflink&logoColor=white)](https://flink.apache.org/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sekelenao/flinkboot-core?label=Maven%20central&logo=apachemaven&logoColor=white&color=E6526F&labelColor=E6526F)](https://central.sonatype.com/artifact/io.github.sekelenao/flinkboot-core)

---

## What is Flinkboot?

**Flinkboot** is a lightweight, high-performance configuration and developer toolkit designed to bootstrap Apache Flink applications. It unifies command-line arguments, environment variables, and hierarchical YAML configuration files into strongly-typed Java models with **zero boilerplate**.

By combining clean Java records and Jakarta Bean Validation (JSR-380), Flinkboot guarantees **fail-early safety**: typos, missing keys, or out-of-range parameters are caught on the JobManager immediately at startup, preventing jobs from failing mid-execution on the cluster.

---

## Getting Started

Follow these 3 essential steps to get started with Flinkboot:

1. **[Setup POM & Avoid Conflicts](howto/avoid-dependency-conflicts.md) (MUST READ)**  
   *Configure your project's Maven POM, BOM dependencies, and shading relocations to prevent Jackson/Log4j runtime conflicts on your Flink cluster.*

2. **[Configure Your Jobs (Load & Merge Configurations)](howto/load-configurations.md)**  
   *Define your YAML configurations (defaults to `file:job-configuration.yaml`), load them into strongly-typed Java models, and apply CLI/environment overrides.*

3. **[Create an Execution Environment](howto/configure-execution-environment.md)**  
   *Configure execution modes, checkpointing, restart strategies, and RocksDB state backends to instantiate Flink's `StreamExecutionEnvironment` with zero boilerplate.*

---

## Key Capabilities

* **Unified Configuration Loading** — Parse and merge multiple YAML files, CLI arguments, and environment variables into immutable Java records.
* **Fail-Fast Validation** — Catch missing parameters, invalid ranges, and syntax errors on the JobManager before resources are allocated.
* **Declarative Execution Environment** — Configure and instantiate Flink's `StreamExecutionEnvironment` with zero boilerplate.
* **Native JDK Type Serialization** — Built-in `@TypeInfo` factories for `Duration`, Java 8 time types, and generic collections without Kryo fallback.
* **Deep POJO Compliance Verification** — Test utility (`assertPojo`) to recursively verify that data models serialize natively without Kryo.
* **Testing Helpers** — Load and validate configurations directly in JUnit 5 tests.
* **Auto-configured Connectors** — Production-ready sources and sinks (e.g. Apache Kafka) built directly from configuration.

---

## Documentation & References

* **[How-To Guides Index](howto/README.md)** — Step-by-step guides for configurations, connectors, POJO compliance, and testing.
* **[Compatibility Matrix](COMPATIBILITY.md)** — Supported Apache Flink versions and Java (JDK) runtimes.
* **[Changelog](CHANGELOG.md)** — Release notes and user-facing change history.

---

## Contributing & Development

Flinkboot is built using Maven. To run tests locally:
```bash
mvn clean test
```

*Not affiliated with the Apache Software Foundation or the Apache Flink project.*