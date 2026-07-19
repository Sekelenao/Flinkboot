# Flinkboot

> **Speed & Safety by design.** Zero-boilerplate configuration that fails fast to keep your Apache Flink pipelines running.

[![Java](https://img.shields.io/badge/Java_11-%23ED8B00.svg?logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)
[![Flink](https://img.shields.io/badge/Flink_1.20-%23E6526F.svg?logo=apacheflink&logoColor=white)](https://flink.apache.org/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sekelenao/flinkboot-core?label=Maven%20central&logo=apachemaven&logoColor=white&color=E6526F&labelColor=E6526F)](https://central.sonatype.com/artifact/io.github.sekelenao/flinkboot-core)

---

## What is Flinkboot?

**Flinkboot** is a lightweight, high-performance configuration utility designed to bootstrap Apache Flink applications. It unifies command-line arguments, environment variables, and hierarchical YAML configuration files into a single, strongly-typed Java model with **zero boilerplate**.

By combining clean Java configuration models and Jakarta Bean Validation (JSR-380), Flinkboot guarantees **fail-early safety**: typos, missing keys, or out-of-range parameters are caught on the JobManager immediately at startup, preventing jobs from failing mid-execution on the cluster.

---

## Key Capabilities

* **Unified Configuration Loading** — Parse and merge multiple YAML configuration files, CLI arguments, and environment variables into immutable Java classes/records.
* **Fail-Fast Validation** — Enforce constraints (non-blank strings, numeric ranges, non-null properties maps, regex validation) before Flink resources are allocated.
* **Safe Merge Semantics** — Detect and prevent accidental property overrides during configuration merges unless explicit override options are passed.
* **Auto-configured Connectors** — Boostrap Apache Flink sources and sinks (e.g. Apache Kafka) directly from configuration files with built-in validation rules and customizers.

---

## 🚀 Must-Read Guides (Getting Started)

Before building your first Flinkboot application, we highly recommend reading these guides in order:

1. **[Avoid Classpath & Dependency Conflicts](howto/avoid-dependency-conflicts.md) (MUST READ)**  
   *Learn how to configure your project's Maven pom.xml and shading settings to avoid typical Flink runtime conflicts with Jackson and Log4j.*
2. **[How to Load & Merge Configurations](howto/load-configurations.md)**  
   *Understand how to define your configuration models, run Flinkboot initialization in your job's main class, and use CLI overrides.*
3. **[How to Configure a Kafka Source](howto/configure-kafka-source.md) / [Sink](howto/configure-kafka-sink.md)**  
   *Learn how to configure Kafka consumers and producers, handle delivery guarantees (Exactly-Once, At-Least-Once), and customize offset strategies.*

---

## 📖 All Guides

For all other specific configurations and detailed features (flags, parameters, POJO compliance), please refer to the complete **[How-To Guides Index](howto/README.md)**.

---

## Contributing & Development

Flinkboot is built using Maven. To run tests locally:
```bash
mvn clean test
```

*Not affiliated with the Apache Software Foundation or the Apache Flink project.*