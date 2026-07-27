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
* **Auto-configured Connectors** — Bootstrap Apache Flink sources and sinks (e.g. Apache Kafka) directly from configuration files with built-in validation rules.

---

## 🚀 Getting Started

Follow these 3 essential steps to get started with Flinkboot:

1. **[Setup POM & Avoid Conflicts](howto/avoid-dependency-conflicts.md) (MUST READ)**  
   *Configure your project's Maven POM, BOM dependencies, and shading relocations to prevent Jackson/Log4j runtime conflicts on your Flink cluster.*

2. **[Configure Your Jobs (Load & Merge Configurations)](howto/load-configurations.md)**  
   *Define your YAML configurations (defaults to `file:job-configuration.yaml`), load them into strongly-typed Java models, and apply CLI/environment overrides.*

3. **[Create an Execution Environment](howto/configure-execution-environment.md)**  
   *Configure execution modes, checkpointing, restart strategies, and RocksDB state backends to instantiate Flink's `StreamExecutionEnvironment` with zero boilerplate.*

---

## 📖 How-To Guides

For all specific guides (connector integrations, parameters, flags, POJO compliance), please refer to the complete **[How-To Guides Index](howto/README.md)**.

---

## Contributing & Development

Flinkboot is built using Maven. To run tests locally:
```bash
mvn clean test
```

*Not affiliated with the Apache Software Foundation or the Apache Flink project.*