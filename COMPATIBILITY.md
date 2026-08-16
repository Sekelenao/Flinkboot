# Compatibility Matrix

This document outlines the compatibility between Flinkboot, Apache Flink versions, and Java (JDK) runtimes.

---

## Version Matrix

| Flinkboot Version | Apache Flink Version | Java Runtime | Status           |
|:------------------|:---------------------|:-------------|:-----------------|
| `0.2.x-1.20`      | `1.20.x`             | Java 11+     | Active / Current |
| `0.1.x-1.20`      | `1.20.x`             | Java 11+     | Deprecated (EOL) |

---

## Versioning Scheme

Flinkboot follows a composite versioning format:

$$\text{<flinkboot-version>}-\text{<flink-major.minor>}$$

* **Flinkboot Version** (e.g., `0.2.0`): Follows [Semantic Versioning](https://semver.org/) (`MAJOR.MINOR.PATCH`) indicating features, improvements, and fixes in Flinkboot itself.
* **Flink Target Version** (e.g., `-1.20`): Identifies the compatible Apache Flink minor release branch.

---

## Java Runtime Support

* **Compilation Target**: Flinkboot artifacts are compiled with Java bytecode target level **11** (`<maven.compiler.release>11</maven.compiler.release>`).
* **Cluster Runtimes**: Flinkboot jobs can be executed on Flink clusters running with **Java 11**, **Java 17**, or **Java 21**.
