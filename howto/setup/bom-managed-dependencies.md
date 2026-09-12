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

The Flinkboot BOM categorizes dependencies into six logical groups. Exact versions are centrally managed by the Flinkboot BOM and strictly align with the project's root pom.xml.

### 1. Flinkboot Modules

| Group ID | Artifact ID | Default Scope | Description |
| :--- | :--- | :--- | :--- |
| `io.github.sekelenao` | `flinkboot-core` | `compile` | Core bootstrap API, YAML parsing, validation, and serialization. |
| `io.github.sekelenao` | `flinkboot-kafka` | `compile` | Pre-configured Kafka Source and Sink factories. |
| `io.github.sekelenao` | `flinkboot-fluss` | `compile` | Pre-configured Apache Fluss Source and Sink factories. |
| `io.github.sekelenao` | `flinkboot-test` | `test` | Unit test helpers (fluent POJO assertions, test config loaders). |

---

### 2. Apache Flink & Connector Libraries

All standard Flink execution components are pre-configured with **`provided`** scope to prevent packaging duplicate Flink runtime classes into your application fat JAR.

| Group ID           | Artifact ID             | Pre-configured Scope | Purpose                                                  |
|:-------------------|:------------------------|:---------------------|:---------------------------------------------------------|
| `org.apache.flink` | `flink-streaming-java`  | `provided`           | Flink DataStream API runtime.                            |
| `org.apache.flink` | `flink-core`            | `provided`           | Core Flink abstractions, type extractors, serializers.   |
| `org.apache.flink` | `flink-clients`         | `provided`           | Local MiniCluster runner and job submission client.      |
| `org.apache.flink` | `flink-connector-base`  | `provided`           | Base interfaces for modern Flink 1.20+ connectors.       |
| `org.apache.flink` | `flink-table-common`    | `provided`           | Flink Table & SQL common types and logical structures.   |
| `org.apache.flink` | `flink-runtime-web`     | `provided`           | Embedded Web Dashboard runtime for local execution.      |
| `org.apache.flink` | `flink-connector-kafka` | `compile`            | Apache Kafka Source and Sink connector for Flink 1.20.   |
| `org.apache.kafka` | `kafka-clients`         | `compile`            | Official Apache Kafka Java client.                       |
| `org.apache.fluss` | `fluss-flink-1.20`      | `compile`            | Apache Fluss streaming storage connector for Flink 1.20. |
| `org.apache.fluss` | `fluss-client`          | `compile`            | Official Apache Fluss Java client.                       |

---

### 3. Configuration & Serialization (Jackson)

Flinkboot relies on Jackson for deserializing YAML configurations and Java 8 Date/Time types.

| Group ID                           | Artifact ID               | Default Scope | Purpose                                                             |
|:-----------------------------------|:--------------------------|:--------------|:--------------------------------------------------------------------|
| `com.fasterxml.jackson.dataformat` | `jackson-dataformat-yaml` | `compile`     | YAML parser engine.                                                 |
| `com.fasterxml.jackson.core`       | `jackson-databind`        | `compile`     | Object mapper and data binding.                                     |
| `com.fasterxml.jackson.core`       | `jackson-core`            | `compile`     | Streaming JSON/YAML parser abstractions.                            |
| `com.fasterxml.jackson.datatype`   | `jackson-datatype-jsr310` | `compile`     | `java.time.*` (`Instant`, `Duration`, `LocalDate`) deserialization. |

> [!IMPORTANT]
> When building your fat JAR for deployment to a Flink cluster, Jackson must be relocated (shaded) to prevent version conflicts with Flink's internal Jackson classes. See [How to Avoid Dependency Conflicts](avoid-dependency-conflicts.md).

---

### 4. Validation (Jakarta Bean Validation)

| Group ID | Artifact ID | Default Scope | Purpose |
| :--- | :--- | :--- | :--- |
| `org.hibernate.validator` | `hibernate-validator` | `compile` | JSR-380 reference implementation for `@NotNull`, `@Min`, `@Pattern`, etc. |
| `org.glassfish.expressly` | `expressly` | `compile` | Jakarta Expression Language (EL) engine required for dynamic validation messages. |

---

### 5. Logging (SLF4J & Log4j)

All logging implementations are pre-marked **`provided`** so that your fat JAR uses the logging backend configured by the target Flink cluster without conflicts.

| Group ID | Artifact ID | Pre-configured Scope | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `org.slf4j` | `slf4j-api` | `provided` | Unified logging API facade for application code. |
| `org.apache.logging.log4j` | `log4j-api` | `provided` | Log4j 2 core API. |
| `org.apache.logging.log4j` | `log4j-core` | `provided` | Log4j 2 implementation. |
| `org.apache.logging.log4j` | `log4j-slf4j2-impl` | `provided` | SLF4J 2 binding for Log4j 2. |

---

### 6. Testing Libraries

| Group ID | Artifact ID | Pre-configured Scope | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `org.junit.jupiter` | `junit-jupiter` | `test` | JUnit 5 testing engine and assertions. |
| `org.mockito` | `mockito-core` | `test` | Mocking framework. |
| `org.mockito` | `mockito-junit-jupiter` | `test` | Mockito JUnit 5 extension (`@ExtendWith(MockitoExtension.class)`). |

---

## Key Benefits of Using the Flinkboot BOM

1. **Zero Version Mismatches**: Guarantees that `flinkboot-core`, `flinkboot-kafka`, `flinkboot-fluss`, and `flinkboot-test` always run with verified, binary-compatible versions of Jackson, Flink, and Hibernate Validator.
2. **Safe Fat JARs**: Pre-configured `provided` scopes prevent packaging `flink-core` or `log4j` inside your application JAR, avoiding classloader linkage errors on TaskManagers.
3. **Painless Upgrades**: Upgrading your Flink or Flinkboot version only requires changing a single property: `<flinkboot.version>`.
