# Flinkboot

> **Speed & Safety by design.** Zero-boilerplate configuration that fails fast to keep your Flink pipelines running.

[![Java](https://img.shields.io/badge/Java_11-%23ED8B00.svg?logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)
[![Flink](https://img.shields.io/badge/Flink_1.20-%23E6526F.svg?logo=apacheflink&logoColor=white)](https://flink.apache.org/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sekelenao/flinkboot-core?label=Maven%20central&logo=apachemaven&logoColor=white&color=E6526F&labelColor=E6526F)](https://central.sonatype.com/artifact/io.github.sekelenao/flinkboot-core)

---

## What is Flinkboot?

**Flinkboot** is a lightweight, high-performance configuration utility designed to get your Apache Flink applications up and running in seconds. It unifies command-line arguments, environment variables, and hierarchical YAML configuration files into a single, validated Java model with **zero boilerplate**.

By combining clean Java configuration POJOs and Jakarta Bean Validation (JSR-380), Flinkboot allows you to configure your jobs instantly while guaranteeing **fail-early safety**: typos, missing keys, or out-of-range parameters are caught on the JobManager immediately at startup, preventing jobs from failing mid-execution on the cluster.

---

## Key Features

- ⚡ **Instant Setup** — Define your configuration as a simple Java class and load it with a single line of code.
- 🔒 **Fail-Fast Validation** — Catch typos and invalid values at startup, before Flink resources are allocated.
- 🔀 **Seamless Overrides** — Pass multiple configuration files sequentially to easily layer environment-specific overrides.
- ⚙️ **Flexible Sources** — Automatically resolves configuration file locations from command-line options (`-flinkboot-configurations`) or environment variables (`FLINKBOOT_CONFIGURATIONS`).

---

## Quick Start Example

Here is how to set up a multi-configuration job and configure a Flink stream in just a few lines of code.

### 1. Define your Configuration Model

Define your configuration as an immutable Java class with fluent accessor methods (without the `get` prefix) and a Jackson creator constructor:

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class JobConfig {

    @NotBlank 
    private final String jobName;

    @NotBlank 
    private final String bootstrapServers;

    @Min(1) 
    private final int parallelism;

    @JsonCreator
    public JobConfig(
        @JsonProperty("jobName") String jobName,
        @JsonProperty("bootstrapServers") String bootstrapServers,
        @JsonProperty("parallelism") int parallelism
    ) {
        this.jobName = jobName;
        this.bootstrapServers = bootstrapServers;
        this.parallelism = parallelism;
    }

    public String jobName() { return jobName; }
    public String bootstrapServers() { return bootstrapServers; }
    public int parallelism() { return parallelism; }
}
```

### 2. Prepare Configuration Files

**`base-config.yaml`** (Common settings)
```yaml
jobName: "UserActivityProcessor"
parallelism: 4
```

**`prod-config.yaml`** (Production overrides)
```yaml
bootstrapServers: "kafka-prod-1:9092,kafka-prod-2:9092"
parallelism: 16
```

### 3. Load Configurations & Run the Flink Stream

Initialize [Flinkboot](file:///home/haine/Documents/Programmation/Flinkboot/flinkboot-core/src/main/java/io/github/sekelenao/flinkboot/core/api/Flinkboot.java) in your main method. Loading, merging, and validation are handled automatically in one step:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.common.serialization.SimpleStringSchema;

public class UserActivityJob {

    public static void main(String[] args) throws Exception {
        // 1. Initialize Flinkboot and load/validate configurations in one go
        JobConfig config = Flinkboot.initialize(args).configuration(JobConfig.class);

        // 2. Set up Flink Environment
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(config.parallelism());

        // 3. Effortlessly build your stream using the validated configuration
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
            .setBootstrapServers(config.bootstrapServers())
            .setTopics("user-activities")
            .setGroupId("activity-group")
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();

        env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source")
            .map(String::toUpperCase)
            .print();

        // 4. Execute Flink Job
        env.execute(config.jobName());
    }
}
```

---

## Command Line Usage

To run the job above, specify the locations of your configurations using the `-flinkboot-configurations` argument (separated by commas):

```bash
flink run -c MyJobJar.jar \
  -flinkboot-configurations file:/etc/configs/base-config.yaml,file:/etc/configs/prod-config.yaml
```

Flinkboot will load `base-config.yaml` first, then merge the values from `prod-config.yaml` (overriding `parallelism` to `16` and populating `bootstrapServers`), and validate the final merged state before starting the job.

Alternatively, you can specify configurations via the environment variable:
```bash
export FLINKBOOT_CONFIGURATIONS="file:/etc/configs/base-config.yaml,file:/etc/configs/prod-config.yaml"
```

---

## Contributing & Development

Flinkboot is built using Maven. To run tests locally:
```bash
mvn clean test
```

*Not affiliated with the Apache Software Foundation or the Apache Flink project.*