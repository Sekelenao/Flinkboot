# How to Configure a Kafka Sink

Flinkboot provides typed configuration models and a factory to easily initialize Apache Flink's `KafkaSink` directly from YAML configuration files.

---

## 1. YAML Configuration Properties & Structure

By default, Flinkboot loads configuration properties from `file:job-configuration.yaml` in the current working directory (or custom paths specified via `-flinkboot-configurations`).

Define your Kafka Sink properties in your YAML file (`job-configuration.yaml`):

```yaml
bootstrap-servers:
  - "localhost:9092"
topic: "users-sink"
delivery-guarantee: "EXACTLY_ONCE"
transactional-id-prefix: "my-transactional-prefix"
properties:
  acks: "all"
```

---

## 2. Configuration Parameters Reference

| Property Key              | Type            | Required | Validation                    | Description                                                                                                                                                                           |
|:--------------------------|:----------------|:---------|:------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `bootstrap-servers`       | List of Strings | **Yes**  | `@NotEmpty`, items `@NotBlank` | Kafka bootstrap broker hosts/ports (e.g. `localhost:9092`).                                                                                                                           |
| `topic`                   | String          | **Yes**  | `@NotBlank`                   | Target Kafka topic to write events to.                                                                                                                                                |
| `delivery-guarantee`      | Enum            | No       | Enum                          | Delivery guarantee. Supported values: `NONE`, `AT_LEAST_ONCE`, `EXACTLY_ONCE`. Defaults to Flink default if omitted.                                                                  |
| `transactional-id-prefix` | String          | No       | String                        | Transactional ID prefix. **Mandatory** only if `delivery-guarantee` is set to `EXACTLY_ONCE`. Must be blank/absent for other delivery guarantees (causes fail-fast crash if present). |
| `properties`              | Map             | No       | Keys/values `@NotBlank`       | Custom Kafka client producer properties (e.g. `acks: all`). Keys and values must be non-null.                                                                                         |

---

## 3. Delivery Guarantees & Strict Rules

Flinkboot supports Flink's delivery guarantee strategies via the `delivery-guarantee` property:

| Value            | Description                                                                | Required Extra Configuration |
|:-----------------|:---------------------------------------------------------------------------|:-----------------------------|
| `EXACTLY_ONCE`   | Exactly-once delivery semantics.                                           | `transactional-id-prefix`    |
| `AT_LEAST_ONCE`  | At-least-once delivery semantics.                                          | None                         |
| `NONE`           | Best-effort delivery semantics.                                            | None                         |
| *Omitted (null)* | Let Flink apply its own defaults (leaves configuration builder untouched). | None                         |

### Fail-Fast Rules for Delivery Guarantees
To prevent misconfigurations at startup:
1. If `delivery-guarantee` is set to `EXACTLY_ONCE`, a non-blank `transactional-id-prefix` **must** be provided.
2. If `delivery-guarantee` is set to `AT_LEAST_ONCE` or `NONE` (or omitted), specifying `transactional-id-prefix` will cause a **fail-fast startup crash** throwing `InvalidKafkaSinkPropertiesException`.

---

## 4. Java Integration

Embed `KafkaSinkProperties` inside your application's root configuration class:

### Step 1: Define Root Configuration POJO

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public final class MyJobConfig {

    @Valid
    @NotNull
    private final KafkaSinkProperties kafkaSink;

    @JsonCreator
    public MyJobConfig(@JsonProperty("kafkaSink") KafkaSinkProperties kafkaSink) {
        this.kafkaSink = kafkaSink;
    }

    public KafkaSinkProperties kafkaSink() { return kafkaSink; }
}
```

### Step 2: Build KafkaSink in Flink Application

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.kafka.api.sink.KafkaSinkFactory;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

public class KafkaProducerJob {
    public static void main(String[] args) throws Exception {
        Flinkboot boot = Flinkboot.initialize(args);
        
        // 1. Load configuration from job-configuration.yaml
        MyJobConfig config = boot.configuration(MyJobConfig.class);
        
        // 2. Define your serialization schema
        KafkaRecordSerializationSchema<String> schema = 
            KafkaRecordSerializationSchema.builder()
                .setTopic(config.kafkaSink().topic())
                .setValueSerializationSchema(new SimpleStringSchema())
                .build();
        
        // 3. Instantiate Flink Kafka Sink from properties
        KafkaSink<String> kafkaSink = KafkaSinkFactory.supplyFor(config.kafkaSink(), schema);
        
        // ... build your Flink pipeline
    }
}
```

### Programmatic Customization
If you need to customize Flink's builder (e.g. custom properties, producer configs) before building:

```java
KafkaSink<String> customKafkaSink = KafkaSinkFactory.supplyBuilderFor(config.kafkaSink(), schema)
    .setKafkaProducerConfig(customProps)
    .build();
```

---

## 5. Fail-Fast Validation & Exceptions

- **Bean Validation:** If any property violates constraints (e.g. blank topic, or null keys/values in `properties`), a `PropertiesValidationException` is thrown at startup.
- **Invalid Delivery Guarantee:** If `transactional-id-prefix` is provided without `EXACTLY_ONCE`, or omitted when `EXACTLY_ONCE` is configured, Flinkboot fails fast with an `InvalidKafkaSinkPropertiesException`.
