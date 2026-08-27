# How to Configure a Fluss Source

Flinkboot provides typed configuration models and a factory to easily initialize Apache Flink's `FlussSource` directly from YAML configuration files.

---

## Maven Dependencies

Import the Flinkboot BOM in your `<dependencyManagement>` and add the Flinkboot Fluss module along with Flink APIs:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.sekelenao</groupId>
            <artifactId>flinkboot</artifactId>
            <version>${flinkboot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Flinkboot Fluss -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-fluss</artifactId>
    </dependency>

    <!-- Flink Streaming API (Provided by Flink cluster) -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-java</artifactId>
    </dependency>

    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-table-common</artifactId>
    </dependency>
</dependencies>
```

---

## 1. YAML Configuration Properties & Structure

You can configure your Fluss Source with startup strategies and custom client options:

### Standard Configuration (`FlussSourceProperties`)

```yaml
name: "my-fluss-source"
bootstrap-servers:
  - "localhost:9123"
database: "analytics_db"
table: "user_events"
startup-mode: "EARLIEST"
properties:
  client.scanner.fetch.max-bytes: "1048576"
```

### Configuration with Timestamp Startup Mode

```yaml
name: "my-timestamp-source"
bootstrap-servers:
  - "localhost:9123"
database: "analytics_db"
table: "user_events"
startup-mode: "TIMESTAMP"
startup-timestamp: 1700000000000
```

---

## 2. Configuration Parameters Reference

| Property Key        | Type                  | Required    | Validation         | Description                                                                                            |
|:--------------------|:----------------------|:------------|:-------------------|:-------------------------------------------------------------------------------------------------------|
| `name`              | `String`              | **Yes**     | `@NotBlank`        | Unique operator identifier in the Flink DAG execution graph.                                           |
| `bootstrap-servers` | `List<String>`        | **Yes**     | `@NotEmpty`        | List of Fluss coordinator addresses (e.g. `localhost:9123`).                                           |
| `database`          | `String`              | **Yes**     | `@NotBlank`        | Target Fluss database name.                                                                            |
| `table`             | `String`              | **Yes**     | `@NotBlank`        | Target Fluss table name.                                                                               |
| `startup-mode`      | `FlussStartupMode`    | **Yes**     | `@NotNull`         | Startup strategy (`EARLIEST`, `LATEST`, `FULL`, `TIMESTAMP`).                                          |
| `startup-timestamp` | `Long`                | Conditional | `@PositiveOrZero`  | Timestamp in epoch milliseconds (**mandatory** if `startup-mode` is `TIMESTAMP`, forbidden otherwise). |
| `properties`        | `Map<String, String>` | No          | `@NotNull` entries | Additional custom Fluss client/scanner configuration properties.                                       |

---

## 3. Supported Startup Modes (`FlussStartupMode`)

| Mode        | Description                                                                               |
|:------------|:------------------------------------------------------------------------------------------|
| `EARLIEST`  | Start reading from the earliest available offset / snapshot in the table.                 |
| `LATEST`    | Start reading from the latest available offset.                                           |
| `FULL`      | Perform a full snapshot scan followed by continuous log reading (for Primary Key tables). |
| `TIMESTAMP` | Start reading from a specific timestamp in milliseconds (requires `startup-timestamp`).   |

---

## 4. Configuration Model in Java

Bind the properties directly in your custom application configuration model:

```java
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AppConfiguration(
    @Valid
    @NotNull
    @JsonProperty("fluss-source")
    FlussSourceProperties flussSource
) {}
```

---

## 5. Creating the Source via `FlussSourceFactory`

Use `FlussSourceFactory` to build the source and add it to your Flink `StreamExecutionEnvironment`:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.fluss.api.source.FlussSourceFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.RowData;
import org.apache.fluss.flink.source.FlussSource;
import org.apache.fluss.flink.source.deserializer.RowDataDeserializationSchema;

public class MyJob {
    public static void main(String[] args) throws Exception {
        AppConfiguration config = Flinkboot.parse(AppConfiguration.class, args);
        StreamExecutionEnvironment env = Flinkboot.getExecutionEnvironment(args);

        RowDataDeserializationSchema deserializer = ...; // Your Fluss deserialization schema

        FlussSource<RowData> source = FlussSourceFactory.supplyFor(
            config.flussSource(),
            deserializer
        );

        env.fromSource(source, WatermarkStrategy.noWatermarks(), config.flussSource().name())
            .print();

        env.execute(config.flussSource().name());
    }
}
```

---

## 6. Java 17+ & Apache Arrow JVM Options

Apache Fluss utilizes **Apache Arrow** for high-performance off-heap direct buffer memory management. On Java 17 and later (Java 17, Java 21+), Java's strong encapsulation of JDK internals requires opening `java.nio` to unnamed modules.

### Local Execution & IDE (VM Options)

When running or debugging your Flink job locally from an IDE (IntelliJ IDEA, Eclipse, VS Code) or via `java -jar`, add the following JVM option to your **Run / Debug VM Options**:

```bash
--add-opens=java.base/java.nio=ALL-UNNAMED
```

Or configure it in your terminal environment:

```bash
export JAVA_TOOL_OPTIONS="--add-opens=java.base/java.nio=ALL-UNNAMED"
```

### Unit & Integration Testing (Maven Surefire)

When writing integration tests with Fluss or running test suites on Java 17+, configure the `maven-surefire-plugin` in your application `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>--add-opens=java.base/java.nio=ALL-UNNAMED</argLine>
    </configuration>
</plugin>
```

### Production Flink Cluster (Kubernetes & Standalone)

Apache Flink 1.20+ official startup scripts and container images automatically inject required `--add-opens` flags. If you are deploying via custom base images or configuring `flink-conf.yaml` / `config.yaml`, ensure the JVM options include:

```yaml
env.java.opts.all: "--add-opens=java.base/java.nio=ALL-UNNAMED"
```
```
