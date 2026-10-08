<p align="center">
  <img src=".github/assets/icon.svg" width="200" alt="logo">
</p>

<h1 align="center">
Flinkboot
</h1>

> **The Bootstrapping & Reliability Framework for Apache Flink.** Fail fast on configuration, serialize natively without Kryo, and bootstrap stream pipelines with zero boilerplate.
>
> **Built by developers who felt the pain.**  
> *We’ve lived through the midnight outages, fragile boilerplate, and endless configuration headaches so you don't have to. Flinkboot is our free, open-source gift to the Flink community, crafted with care to make stream processing enjoyable again.*

[![Java](https://img.shields.io/badge/Java_11%2B-%23ED8B00.svg?logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)
[![Flink](https://img.shields.io/badge/Flink_1.20-%23E6526F.svg?logo=apacheflink&logoColor=white)](https://flink.apache.org/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sekelenao/flinkboot-core?label=Maven%20central&logo=apachemaven&logoColor=white&color=C71A36&labelColor=C71A36)](https://central.sonatype.com/artifact/io.github.sekelenao/flinkboot-core)
[![Documentation](https://img.shields.io/badge/Documentation-flinkboot.com-%230288D1.svg?logo=docusaurus&logoColor=white&color=0288D1&labelColor=0288D1)](https://flinkboot.com)
![Tests](https://raw.githubusercontent.com/Sekelenao/Flinkboot/badges/Tests.svg)
![Coverage](https://raw.githubusercontent.com/Sekelenao/Flinkboot/badges/Coverage.svg)
![Branches](https://raw.githubusercontent.com/Sekelenao/Flinkboot/badges/Branches.svg)

---

## What is Flinkboot?

**Flinkboot** is a comprehensive, production-grade development and reliability framework designed to bootstrap, configure, and secure Apache Flink applications with **zero boilerplate**.

In standard Flink deployments, misconfigurations, missing parameters, state backend errors, and silent fallbacks to slow Kryo serialization often go unnoticed until runtime, leading to costly cluster failures or degraded pipeline throughput. Flinkboot eliminates these risks before your code ever reaches the TaskManagers:

### 🔴 Before

```java
public static void main(String[] args) throws Exception {
    // 1. Manual YAML parsing with Jackson (untyped tree navigation, zero validation, fails on first missing key)
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    JsonNode yaml = mapper.readTree(new File("job-configuration.yaml"));
    String brokers = yaml.get("kafka").get("brokers").asText();
    String topic = yaml.get("kafka").get("topic").asText();
    long checkpointInterval = yaml.get("checkpoint").get("interval").asLong();

    // 2. Imperative environment setup (hardcoded: adding Web UI, latency tracking, or unaligned checkpoints requires code change & redeployment)
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.enableCheckpointing(checkpointInterval);
    env.getCheckpointConfig().setCheckpointTimeout(60000L);
    env.setStateBackend(new EmbeddedRocksDBStateBackend(true));
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.seconds(10)));

    // 3. Rigid manual connector builder (unverified OrderEvent could silently fall back to slow Kryo)
    KafkaSource<OrderEvent> source = KafkaSource.<OrderEvent>builder()
            .setBootstrapServers(brokers)
            .setTopics(topic)
            .setGroupId("order-service")
            .setStartingOffsets(OffsetsInitializer.latest())
            .setDeserializer(new OrderEventDeserializationSchema())
            .build();

    env.fromSource(source, WatermarkStrategy.noWatermarks(), "kafka-source").print();
    env.execute("LegacyJob");

    // ... and it would take 400+ lines of imperative boilerplate just to cover all execution environment settings
    // (RocksDB tuning, restart strategies, unaligned checkpoints, latency metrics, savepoint restore, local web UI...)
    // Not to mention hundreds more for multi-source YAML merging, ${ENV} interpolation, and fail-fast validation.
}
```

### 🟢 With Flinkboot

```java
// 1. Build-time guarantee in unit tests: fails build if OrderEvent could fall back to slow Kryo
@Test
void verifyPojoCompliance() {
    FlinkbootAssertions.assertThat(OrderEvent.class).isPojo();
}

// 2. User-defined composed configuration (record or class): assemble independent blocks like Legos
public record AppConfiguration(
    // 100% validated fail-fast & covers every Flink environment setting (RocksDB, restart, checkpoints...)
    @Valid @NotNull @JsonProperty("job") JobProperties job,

    // 100% validated fail-fast & exhaustively covers the connector options with raw escape hatch
    @Valid @NotNull @JsonProperty("kafka-source") KafkaSourceProperties kafkaSource
) {}

public class MyFlinkJob {

    public static void main(String[] args) throws Exception {
        // Loads & merges multi-source YAML with full fail-fast Jakarta validation (catches all errors at once)
        Flinkboot boot = Flinkboot.initialize(args);
        AppConfiguration config = boot.configuration(AppConfiguration.class);

        // 1-line setup fully configuring RocksDB, checkpointing, restart strategy, local Web UI, and latency tracking
        StreamExecutionEnvironment env = boot.executionEnvironment(config.job());

        // Pre-configured, production-ready Kafka source with operator-level properties escape hatch
        KafkaSource<OrderEvent> source = KafkaSourceFactory.supplyFor(
            config.kafkaSource(), 
            new OrderEventDeserializationSchema()
        );

        env.fromSource(source, WatermarkStrategy.noWatermarks(), config.kafkaSource().name()).print();
        env.execute(config.job().name());
    }
}
```

---

## Documentation & References

* **[Documentation](https://flinkboot.com)** — Official documentation.
* **[Contributing Guide](CONTRIBUTING.md)** — Guidelines for reporting issues, submitting pull requests, and coding standards.
* **[Changelog](CHANGELOG.md)** — Release notes and user-facing change history.

---

## Contributors

Every bug fix, test addition, and architectural improvement from the community makes Flinkboot more reliable for everyone running Flink in production.

Sincere appreciation to all contributors who have shaped and strengthened this codebase.

<!-- CONTRIBUTORS-START -->
<p align="left">
  <a href="https://github.com/Sekelenao"><img src="https://github.com/Sekelenao.png?size=64" width="64" height="64" alt="Sekelenao" title="Sekelenao (65 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/LouisDeconinck"><img src="https://github.com/LouisDeconinck.png?size=64" width="64" height="64" alt="LouisDeconinck" title="LouisDeconinck (8 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/be-student"><img src="https://github.com/be-student.png?size=64" width="64" height="64" alt="be-student" title="be-student (4 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/bkalika"><img src="https://github.com/bkalika.png?size=64" width="64" height="64" alt="bkalika" title="bkalika (4 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/kasapdev"><img src="https://github.com/kasapdev.png?size=64" width="64" height="64" alt="kasapdev" title="kasapdev (3 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/timothytkim"><img src="https://github.com/timothytkim.png?size=64" width="64" height="64" alt="timothytkim" title="timothytkim (3 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/Aaqibhafeezkhan"><img src="https://github.com/Aaqibhafeezkhan.png?size=64" width="64" height="64" alt="Aaqibhafeezkhan" title="Aaqibhafeezkhan (2 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/FrodyGr"><img src="https://github.com/FrodyGr.png?size=64" width="64" height="64" alt="FrodyGr" title="FrodyGr (2 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/Pallavi-p-h"><img src="https://github.com/Pallavi-p-h.png?size=64" width="64" height="64" alt="Pallavi-p-h" title="Pallavi-p-h (2 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/PHJ2000"><img src="https://github.com/PHJ2000.png?size=64" width="64" height="64" alt="PHJ2000" title="PHJ2000 (2 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/Renan-Bacheschi"><img src="https://github.com/Renan-Bacheschi.png?size=64" width="64" height="64" alt="Renan-Bacheschi" title="Renan-Bacheschi (2 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/yunaremaia"><img src="https://github.com/yunaremaia.png?size=64" width="64" height="64" alt="yunaremaia" title="yunaremaia (2 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
</p>
<!-- CONTRIBUTORS-END -->

---

*Apache®, Apache Flink®, Apache Kafka®, and Apache Fluss™ are trademarks of the Apache Software Foundation. Flinkboot is an independent open-source project and is not affiliated with, endorsed by, or sponsored by the Apache Software Foundation.*

