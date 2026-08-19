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

## 2. Configuration Model in Java

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

## 3. Creating the Source via `FlussSourceFactory`

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

## 4. Supported Startup Modes (`FlussStartupMode`)

| Mode | Description |
| :--- | :--- |
| `EARLIEST` | Start reading from the earliest available offset / snapshot in the table. |
| `LATEST` | Start reading from the latest available offset. |
| `FULL` | Perform a full snapshot scan followed by continuous log reading (for Primary Key tables). |
| `TIMESTAMP` | Start reading from a specific timestamp in milliseconds (requires `startup-timestamp`). |
