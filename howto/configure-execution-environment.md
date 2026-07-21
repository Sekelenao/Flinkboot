# How to Configure the Execution Environment

Flinkboot provides strongly typed configuration models and an automated factory to easily configure and obtain Apache Flink's `StreamExecutionEnvironment`.

## Maven Dependency

To use this feature, import the core Flinkboot dependency:

```xml
<dependency>
    <groupId>io.github.sekelenao</groupId>
    <artifactId>flinkboot-core</artifactId>
</dependency>
```

---

## 1. YAML Configuration Structure

The execution environment configuration is structured around `JobConfiguration` and `ExecutionEnvironmentConfiguration`.

```yaml
name: "my-streaming-job"
environment:
  execution:
    runtime-mode: "STREAMING"
    parallelism: 8
    max-parallelism: 128
    buffer-timeout-ms: 100
    auto-watermark-interval-ms: 200
    object-reuse: true
  checkpointing:
    enabled: true
    interval-ms: 10000
    mode: "EXACTLY_ONCE"
    timeout-ms: 60000
    min-pause-between-checkpoints-ms: 5000
    max-concurrent-checkpoints: 1
    externalized-checkpoint-cleanup: "RETAIN_ON_CANCELLATION"
    unaligned-checkpoints: true
    aligned-checkpoint-timeout-ms: 1000
    storage-uri: "s3://my-flink-bucket/checkpoints"
```

---

## 2. Configuration Parameters Reference

### Job Level (`JobConfiguration`)

| Property Key  | Type   | Required | Description                                                          |
|:--------------|:-------|:---------|:---------------------------------------------------------------------|
| `name`        | String | **Yes**  | Canonical job name registered with Flink (`PipelineOptions.NAME`).    |
| `environment` | Object | No       | Execution environment settings (`ExecutionEnvironmentConfiguration`). |

### Execution Settings (`ExecutionConfiguration`)

| Property Key                 | Type    | Required | Validation        | Description                                                                                          |
|:-----------------------------|:--------|:---------|:------------------|:-----------------------------------------------------------------------------------------------------|
| `runtime-mode`               | Enum    | No       | Enum              | Execution runtime mode: `STREAMING`, `BATCH`, or `AUTOMATIC` (`ExecutionOptions.RUNTIME_MODE`).      |
| `parallelism`                | Integer | No       | `@Positive`       | Default execution parallelism (`CoreOptions.DEFAULT_PARALLELISM`). Must be > 0.                      |
| `max-parallelism`            | Integer | No       | `@Positive`       | Maximum parallelism for key groups rescale (`PipelineOptions.MAX_PARALLELISM`). Must be > 0.         |
| `buffer-timeout-ms`          | Long    | No       | `@PositiveOrZero` | Buffer timeout in milliseconds (`ExecutionOptions.BUFFER_TIMEOUT`). Trade-off latency vs throughput. |
| `auto-watermark-interval-ms` | Long    | No       | `@PositiveOrZero` | Periodic watermark emission interval in ms (`PipelineOptions.AUTO_WATERMARK_INTERVAL`).              |
| `object-reuse`               | Boolean | No       | Boolean           | Enable object reuse optimization (`PipelineOptions.OBJECT_REUSE`). Defaults to false in Flink.       |

### Checkpointing Settings (`CheckpointingConfiguration`)

| Property Key                       | Type    | Required | Validation        | Description                                                                                                                           |
|:-----------------------------------|:--------|:---------|:------------------|:--------------------------------------------------------------------------------------------------------------------------------------|
| `enabled`                          | Boolean | No       | Boolean           | Enable checkpointing (`CheckpointingOptions.CHECKPOINTING_INTERVAL`).                                                                 |
| `interval-ms`                      | Long    | No       | `@Positive`       | Time interval between checkpoints in ms (`CheckpointingOptions.CHECKPOINTING_INTERVAL`). Must be > 0.                                  |
| `mode`                             | Enum    | No       | Enum              | Checkpointing consistency mode: `EXACTLY_ONCE` or `AT_LEAST_ONCE` (`CheckpointingOptions.CHECKPOINTING_CONSISTENCY_MODE`).             |
| `timeout-ms`                       | Long    | No       | `@Positive`       | Maximum duration for a checkpoint before aborting (`CheckpointingOptions.CHECKPOINTING_TIMEOUT`). Must be > 0.                        |
| `min-pause-between-checkpoints-ms` | Long    | No       | `@PositiveOrZero` | Minimum rest duration between consecutive checkpoints (`CheckpointingOptions.MIN_PAUSE_BETWEEN_CHECKPOINTS`).                         |
| `max-concurrent-checkpoints`       | Integer | No       | `@Positive`       | Maximum concurrent checkpoints allowed (`CheckpointingOptions.MAX_CONCURRENT_CHECKPOINTS`). Must be > 0.                             |
| `externalized-checkpoint-cleanup`  | Enum    | No       | Enum              | Cleanup retention mode on cancellation: `RETAIN_ON_CANCELLATION`, `DELETE_ON_CANCELLATION`, or `NO_EXTERNALIZED_CHECKPOINTS`.        |
| `unaligned-checkpoints`            | Boolean | No       | Boolean           | Enable unaligned checkpoints (`CheckpointingOptions.ENABLE_UNALIGNED`).                                                               |
| `aligned-checkpoint-timeout-ms`    | Long    | No       | `@PositiveOrZero` | Timeout before switching to unaligned checkpoints (`CheckpointingOptions.ALIGNED_CHECKPOINT_TIMEOUT`).                                |
| `storage-uri`                      | String  | No       | String            | Target checkpoint storage directory URI, e.g. `s3://bucket/checkpoints` (`CheckpointingOptions.CHECKPOINTS_DIRECTORY`).              |

---

## 3. Java Integration & Usage

### Step 1: Load Configuration and Instantiate Environment

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.core.api.configuration.JobConfiguration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class MyFlinkJob {
    public static void main(String[] args) throws Exception {
        // 1. Initialize Flinkboot with CLI arguments
        Flinkboot boot = Flinkboot.initialize(args);

        // 2. Load the typed JobConfiguration
        JobConfiguration jobConfig = boot.configuration(JobConfiguration.class);

        // 3. Obtain the pre-configured StreamExecutionEnvironment
        StreamExecutionEnvironment env = boot.executionEnvironment(jobConfig);

        // 4. Build your Flink pipeline
        env.fromData("Hello", "Flinkboot")
           .print();

        env.execute(jobConfig.name());
    }
}
```

---

## 4. Validation & Exception Handling

All configuration models enforce Jakarta Bean Validation rules at startup. If an invalid value is supplied (e.g. negative interval or invalid checkpointing mode), a `ConfigurationValidationException` is thrown before starting the Flink environment.
