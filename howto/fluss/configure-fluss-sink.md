# How to Configure a Fluss Sink

Flinkboot provides typed configuration models and a factory to easily initialize Apache Flink's `FlussSink` directly from YAML configuration files.

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

Define your Fluss Sink properties in your YAML file:

```yaml
name: "my-fluss-sink"
bootstrap-servers:
  - "localhost:9123"
database: "analytics_db"
table: "user_aggregates"
batch-size: 1048576
batch-timeout: "PT0.05S"
properties:
  client.writer.bucket.batch.size: "1048576"
```

---

## 2. Configuration Parameters Reference

| Property Key        | Type                  | Required | Validation         | Description                                                     |
|:--------------------|:----------------------|:---------|:-------------------|:----------------------------------------------------------------|
| `name`              | `String`              | **Yes**  | `@NotBlank`        | Unique operator identifier in the Flink DAG execution graph.    |
| `bootstrap-servers` | `List<String>`        | **Yes**  | `@NotEmpty`        | List of Fluss coordinator addresses (e.g. `localhost:9123`).    |
| `database`          | `String`              | **Yes**  | `@NotBlank`        | Target Fluss database name.                                     |
| `table`             | `String`              | **Yes**  | `@NotBlank`        | Target Fluss table name.                                        |
| `batch-size`        | `Long`                | No       | `@PositiveOrZero`  | Writer bucket batch size in bytes.                              |
| `batch-timeout`     | `Duration`            | No       | ISO-8601           | Writer bucket batch timeout, e.g. `"PT0.05S"`.                  |
| `properties`        | `Map<String, String>` | No       | `@NotNull` entries | Additional custom Fluss client/writer configuration properties. |


---

## 3. Configuration Model in Java

Bind the properties directly into your application configuration record or class:

```java
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.fluss.api.properties.sink.FlussSinkProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AppConfiguration(
    @Valid
    @NotNull
    @JsonProperty("fluss-sink")
    FlussSinkProperties flussSink
) {}
```

---

## 4. Creating the Sink via `FlussSinkFactory`

Use `FlussSinkFactory` to create the sink and attach it to your Flink stream:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.fluss.api.sink.FlussSinkFactory;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.RowData;
import org.apache.fluss.flink.sink.FlussSink;
import org.apache.fluss.flink.sink.serializer.RowDataSerializationSchema;

public class MyJob {
    public static void main(String[] args) throws Exception {
        AppConfiguration config = Flinkboot.parse(AppConfiguration.class, args);
        StreamExecutionEnvironment env = Flinkboot.getExecutionEnvironment(args);

        DataStream<RowData> stream = ...; // Your processed stream
        RowDataSerializationSchema serializer = ...; // Your Fluss serialization schema

        FlussSink<RowData> sink = FlussSinkFactory.supplyFor(
            config.flussSink(),
            serializer
        );

        stream.sinkTo(sink).name(config.flussSink().name());

        env.execute(config.flussSink().name());
    }
}
```
