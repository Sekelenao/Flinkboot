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
```

---

## 2. Configuration Parameters Reference

### Job Level (`JobConfiguration`)

| Property Key  | Type   | Required | Description                                                           |
|:--------------|:-------|:---------|:----------------------------------------------------------------------|
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

All configuration models enforce Jakarta Bean Validation rules at startup. If an invalid value is supplied (e.g. negative parallelism or a blank job name), a `ConfigurationValidationException` is thrown before starting the Flink environment.
