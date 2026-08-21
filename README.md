# Flinkboot

> **Enterprise-Grade Safety & Zero-Boilerplate Runtime for Apache Flink.** Fail fast, serialize natively, and ship bulletproof streaming applications to production.

[![Java](https://img.shields.io/badge/Java_11%2B-%23ED8B00.svg?logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)
[![Flink](https://img.shields.io/badge/Flink_1.20-%23E6526F.svg?logo=apacheflink&logoColor=white)](https://flink.apache.org/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sekelenao/flinkboot-core?label=Maven%20central&logo=apachemaven&logoColor=white&color=C71A36&labelColor=C71A36)](https://central.sonatype.com/artifact/io.github.sekelenao/flinkboot-core)
![Tests](https://raw.githubusercontent.com/Sekelenao/Flinkboot/badges/Tests.svg)
![Coverage](https://raw.githubusercontent.com/Sekelenao/Flinkboot/badges/Coverage.svg)
![Branches](https://raw.githubusercontent.com/Sekelenao/Flinkboot/badges/Branches.svg)

---

## What is Flinkboot?

**Flinkboot** is a comprehensive, production-grade development and reliability framework designed to bootstrap, configure, and secure Apache Flink applications with **zero boilerplate**.

In standard Flink deployments, misconfigurations, missing parameters, state backend errors, and silent fallbacks to slow Kryo serialization often go unnoticed until runtime—leading to costly cluster failures or degraded pipeline throughput. Flinkboot eliminates these risks before your code ever reaches the TaskManagers:

* **Fail-Fast Startup & Multi-Source Configuration**: Unifies hierarchical YAML configurations, CLI arguments, and environment variables into immutable, validated Java records using Jakarta Bean Validation (JSR-380). Every parameter, numeric boundary, and regex is verified on the JobManager before resources are provisioned.
* **High-Performance Native Serialization**: Built-in `@TypeInfo` factories and optimized serializers for Java 8 date/time types, collections (`List<E>`, `Map<K, V>`), and `Duration` (encoded in a compact 12-byte binary format) ensure pure native Flink serialization without slow Kryo fallback.
* **Deep POJO Compliance Verification**: Unit testing utilities (`FlinkbootTest.assertPojo`) recursively inspect entire data model hierarchies—including nested objects, generics, collections, arrays, tuples, and `Either` types—guaranteeing 100% native Flink serialization compliance at build time.
* **Declarative Execution & Pre-Built Connectors**: Bootstrap Flink's `StreamExecutionEnvironment` (checkpointing, RocksDB state backends, restart strategies, savepoints, Web UI) and production-ready connectors (e.g. Apache Kafka, Apache Fluss sources/sinks) with a single method call.

---

## Quickstart

Looking for an end-to-end, runnable example project? Check out the **[Flinkboot Quickstart](https://github.com/Sekelenao/Flinkboot-Quickstart)** repository:
* **Complete Real-Time Pipeline**: Edge heartbeat and latency monitoring with Apache Kafka source & sink.
* **Fail-Fast Configuration**: Java 17 Record configuration model with Jakarta Bean Validation and YAML merging.
* **Native POJO Serialization**: 100% native Flink serialization (zero Kryo fallback) with `java.time.LocalDateTime` and `Duration`.
* **Production Packaging**: Fully pre-configured Maven shade plugin setup ready to build and submit to Apache Flink 1.20+.

---

## Getting Started

Follow these 3 essential steps to get started with Flinkboot:

1. **[Setup POM & Avoid Conflicts](howto/avoid-dependency-conflicts.md) (MUST READ)**  
   *Configure your project's Maven POM, BOM dependencies, and shading relocations to prevent Jackson/Log4j runtime conflicts on your Flink cluster.*

2. **[Configure Your Jobs (Load & Merge Configurations)](howto/load-configurations.md)**  
   *Define your YAML configurations (defaults to `classpath:job-configuration.yaml`), load them into strongly-typed Java models, and apply CLI/environment overrides.*

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
* **Unified Resource Loading** — Load files and assets seamlessly across classpath and file systems with a unified URI syntax (`Resource.of`).
* **Auto-configured Connectors** — Production-ready sources and sinks (e.g. Apache Kafka, Apache Fluss) built directly from configuration.

---

## Documentation & References

* **[Flinkboot Quickstart Repository](https://github.com/Sekelenao/Flinkboot-Quickstart)** — Complete, runnable example project with Kafka and Docker Compose.
* **[How-To Guides Index](howto/README.md)** — Step-by-step guides for configurations, connectors, POJO compliance, and testing.
* **[Compatibility Matrix](COMPATIBILITY.md)** — Supported Apache Flink versions and Java (JDK) runtimes.
* **[Contributing Guide](CONTRIBUTING.md)** — Guidelines for reporting issues, submitting pull requests, and coding standards.
* **[Changelog](CHANGELOG.md)** — Release notes and user-facing change history.

---

*Apache®, Apache Flink®, Apache Kafka®, and Apache Fluss™ are trademarks of the Apache Software Foundation. Flinkboot is an independent open-source project and is not affiliated with, endorsed by, or sponsored by the Apache Software Foundation.*
