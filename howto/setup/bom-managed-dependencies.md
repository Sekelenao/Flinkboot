# Understanding the Flinkboot Bill of Materials (BOM)

The **Flinkboot BOM (`io.github.sekelenao:flinkboot`)** centralizes dependency versions and enforces correct Maven scopes (`provided`, `compile`, `test`) across your entire Apache Flink project.

By importing the Flinkboot BOM into your root `pom.xml`, you no longer need to manage disparate version tags or manually specify `<scope>provided</scope>` for Apache Flink and Log4j runtime libraries.

---

## How to Import the BOM

Add the Flinkboot BOM to your project's `<dependencyManagement>` section:

```xml
<dependencyManagement>
    <dependencies>
        <!-- Flinkboot Bill of Materials (BOM) -->
        <dependency>
            <groupId>io.github.sekelenao</groupId>
            <artifactId>flinkboot</artifactId>
            <version>${flinkboot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Once imported, child dependencies can be declared **without specifying `<version>` or `<scope>`**:

```xml
<dependencies>
    <!-- Flink Streaming Java (Automatically managed as provided) -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-java</artifactId>
    </dependency>

    <!-- Flinkboot Core (Automatically managed with aligned version) -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-core</artifactId>
    </dependency>

    <!-- Flinkboot Test (Automatically scoped as test) -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-test</artifactId>
    </dependency>
</dependencies>
```

---

## Managed Dependencies Breakdown

The Flinkboot BOM categorizes dependencies into six logical groups:

### 1. Flinkboot Modules

| Group ID | Artifact ID | Default Scope | Description |
| :--- | :--- | :--- | :--- |
| `io.github.sekelenao` | `flinkboot-core` | `compile` | Core bootstrap API, YAML parsing, validation, and serialization. |
| `io.github.sekelenao` | `flinkboot-kafka` | `compile` | Pre-configured Kafka Source and Sink factories. |
| `io.github.sekelenao` | `flinkboot-fluss` | `compile` | Pre-configured Apache Fluss Source and Sink factories. |
| `io.github.sekelenao` | `flinkboot-test` | `test` | Unit test helpers (`FlinkbootTest.assertPojo`, test config loaders). |

---

### 2. Apache Flink & Connector Libraries

All standard Flink execution components are pre-configured with **`provided`** scope to prevent packaging duplicate Flink runtime classes into your application fat JAR.

| Group ID           | Artifact ID             | Managed Version    | Pre-configured Scope | Purpose                                                  |
|:-------------------|:------------------------|:-------------------|:---------------------|:---------------------------------------------------------|
| `org.apache.flink` | `flink-streaming-java`  | `1.20.5`           | `provided`           | Flink DataStream API runtime.                            |
| `org.apache.flink` | `flink-core`            | `1.20.5`           | `provided`           | Core Flink abstractions, type extractors, serializers.   |
| `org.apache.flink` | `flink-clients`         | `1.20.5`           | `provided`           | Local MiniCluster runner and job submission client.      |
| `org.apache.flink` | `flink-connector-base`  | `1.20.5`           | `provided`           | Base interfaces for modern Flink 1.20+ connectors.       |
| `org.apache.flink` | `flink-table-common`    | `1.20.5`           | `provided`           | Flink Table & SQL common types and logical structures.   |
| `org.apache.flink` | `flink-connector-kafka` | `3.4.0-1.20`       | `compile`            | Apache Kafka Source and Sink connector for Flink 1.20.   |
| `org.apache.kafka` | `kafka-clients`         | `3.4.0`            | `compile`            | Official Apache Kafka Java client.                       |
| `org.apache.fluss` | `fluss-flink-1.20`      | `0.9.1-incubating` | `compile`            | Apache Fluss streaming storage connector for Flink 1.20. |
| `org.apache.fluss` | `fluss-client`          | `0.9.1-incubating` | `compile`            | Official Apache Fluss Java client.                       |

---

### 3. Configuration & Serialization (Jackson)

Flinkboot relies on Jackson for deserializing YAML configurations and Java 8 Date/Time types.

| Group ID                           | Artifact ID               | Managed Version | Default Scope | Purpose                                                             |
|:-----------------------------------|:--------------------------|:----------------|:--------------|:--------------------------------------------------------------------|
| `com.fasterxml.jackson.dataformat` | `jackson-dataformat-yaml` | `2.22.2`        | `compile`     | YAML parser engine.                                                 |
| `com.fasterxml.jackson.core`       | `jackson-databind`        | `2.22.2`        | `compile`     | Object mapper and data binding.                                     |
| `com.fasterxml.jackson.core`       | `jackson-core`            | `2.22.2`        | `compile`     | Streaming JSON/YAML parser abstractions.                            |
| `com.fasterxml.jackson.datatype`   | `jackson-datatype-jsr310` | `2.22.2`        | `compile`     | `java.time.*` (`Instant`, `Duration`, `LocalDate`) deserialization. |

> [!IMPORTANT]
> When building your fat JAR for deployment to a Flink cluster, Jackson must be relocated (shaded) to prevent version conflicts with Flink's internal Jackson classes. See [How to Avoid Dependency Conflicts](avoid-dependency-conflicts.md).

---

### 4. Validation (Jakarta Bean Validation)

| Group ID | Artifact ID | Managed Version | Default Scope | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `org.hibernate.validator` | `hibernate-validator` | `8.0.4.Final` | `compile` | JSR-380 reference implementation for `@NotNull`, `@Min`, `@Pattern`, etc. |
| `org.glassfish.expressly` | `expressly` | `5.0.0` | `compile` | Jakarta Expression Language (EL) engine required for dynamic validation messages. |

---

### 5. Logging (SLF4J & Log4j)

All logging implementations are pre-marked **`provided`** so that your fat JAR uses the logging backend configured by the target Flink cluster without conflicts.

| Group ID | Artifact ID | Managed Version | Pre-configured Scope | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `org.slf4j` | `slf4j-api` | `2.0.16` | `provided` | Unified logging API facade for application code. |
| `org.apache.logging.log4j` | `log4j-api` | `2.24.3` | `provided` | Log4j 2 core API. |
| `org.apache.logging.log4j` | `log4j-core` | `2.24.3` | `provided` | Log4j 2 implementation. |
| `org.apache.logging.log4j` | `log4j-slf4j2-impl` | `2.24.3` | `provided` | SLF4J 2 binding for Log4j 2. |

---

### 6. Testing Libraries

| Group ID | Artifact ID | Managed Version | Pre-configured Scope | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `org.junit.jupiter` | `junit-jupiter` | `5.11.4` | `test` | JUnit 5 testing engine and assertions. |
| `org.mockito` | `mockito-core` | `5.23.0` | `test` | Mocking framework. |
| `org.mockito` | `mockito-junit-jupiter` | `5.23.0` | `test` | Mockito JUnit 5 extension (`@ExtendWith(MockitoExtension.class)`). |

---

## Key Benefits of Using the Flinkboot BOM

1. **Zero Version Mismatches**: Guarantees that `flinkboot-core`, `flinkboot-kafka`, `flinkboot-fluss`, and `flinkboot-test` always run with verified, binary-compatible versions of Jackson, Flink, and Hibernate Validator.
2. **Safe Fat JARs**: Pre-configured `provided` scopes prevent packaging `flink-core` or `log4j` inside your application JAR, avoiding classloader linkage errors on TaskManagers.
3. **Painless Upgrades**: Upgrading your Flink or Flinkboot version only requires changing a single property: `<flinkboot.version>`.
