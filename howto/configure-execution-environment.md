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
  restart-strategy:
    type: "EXPONENTIAL_DELAY"
    exponential-delay:
      initial-backoff-ms: 1000
      max-backoff-ms: 60000
      backoff-multiplier: 2.0
      reset-backoff-threshold-ms: 3600000
      jitter-factor: 0.1
  state-backend:
    type: "ROCKSDB"
    checkpoint-storage: "FILESYSTEM"
    storage-path: "s3://my-flink-bucket/checkpoints"
    incremental: true
    latency-tracking: true
  savepoint-restore:
    savepoint-path: "/tmp/savepoints/savepoint-1"
    allow-non-restored-state: true
    restore-mode: "CLAIM"
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

### Restart Strategy Settings (`RestartStrategyConfiguration`)

The `restart-strategy` block accepts a `type` property (`NO_RESTART`, `FIXED_DELAY`, `FAILURE_RATE`, `EXPONENTIAL_DELAY`, `FALLBACK`) and at most **one** matching sub-configuration block.

#### Strategy Types Overview

| Strategy Value      | Description                                                                                     | Allowed Sub-Block                     |
|:--------------------|:------------------------------------------------------------------------------------------------|:--------------------------------------|
| `FIXED_DELAY`       | Restarts job a fixed number of times with a delay between attempts.                             | `fixed-delay`                         |
| `FAILURE_RATE`      | Restarts job if failure rate threshold is not exceeded in a time window.                       | `failure-rate`                        |
| `EXPONENTIAL_DELAY` | Restarts job with exponentially increasing backoff delays.                                       | `exponential-delay`                   |
| `NO_RESTART`        | Disables job restarts.                                                                          | *None allowed* (causes crash if set)  |
| `FALLBACK`          | Fallback to Flink cluster's global default restart strategy (default if `type` omitted).        | *None allowed* (causes crash if set)  |

#### Option Details per Strategy Block

##### 1. Fixed Delay (`type: FIXED_DELAY` $\rightarrow$ `fixed-delay:`)

| Property Key | Type    | Required | Validation        | Description                                                                                              |
|:-------------|:--------|:---------|:------------------|:---------------------------------------------------------------------------------------------------------|
| `attempts`   | Integer | No       | `@Positive`       | Number of restart attempts (`RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS`).               |
| `delay-ms`   | Long    | No       | `@PositiveOrZero` | Delay between restart attempts in ms (`RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY`).      |

##### 2. Failure Rate (`type: FAILURE_RATE` $\rightarrow$ `failure-rate:`)

| Property Key               | Type    | Required | Validation        | Description                                                                                                   |
|:---------------------------|:--------|:---------|:------------------|:--------------------------------------------------------------------------------------------------------------|
| `max-failures-per-interval`| Integer | No       | `@Positive`       | Max failures allowed within interval (`RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_MAX_FAILURES_PER_INTERVAL`).|
| `failure-interval-ms`      | Long    | No       | `@Positive`       | Time window evaluating failure rate (`RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_FAILURE_RATE_INTERVAL`).|
| `delay-ms`                 | Long    | No       | `@PositiveOrZero` | Delay between attempts in ms (`RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_DELAY`).                 |

##### 3. Exponential Delay (`type: EXPONENTIAL_DELAY` $\rightarrow$ `exponential-delay:`)

| Property Key                | Type   | Required | Validation                                    | Description                                                                                                          |
|:----------------------------|:-------|:---------|:----------------------------------------------|:---------------------------------------------------------------------------------------------------------------------|
| `initial-backoff-ms`        | Long   | No       | `@Positive`                                   | Initial backoff delay (`RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_INITIAL_BACKOFF`).                  |
| `max-backoff-ms`            | Long   | No       | `@Positive`, $\ge$ `initial-backoff-ms`       | Maximum backoff delay cap (`RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_MAX_BACKOFF`).                 |
| `backoff-multiplier`        | Double | No       | `@DecimalMin("1.0")`                          | Exponential backoff multiplier (`RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_BACKOFF_MULTIPLIER`).      |
| `reset-backoff-threshold-ms`| Long   | No       | `@Positive`                                   | Reset backoff threshold duration (`RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_RESET_BACKOFF_THRESHOLD`).|
| `jitter-factor`             | Double | No       | `@DecimalMin("0.0")`, `@DecimalMax("1.0")`    | Jitter factor for delay randomization (`RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_JITTER_FACTOR`).  |

### State Backend Settings (`StateBackendConfiguration`)

| Property Key         | Type    | Required                     | Validation | Description                                                                                                                           |
|:---------------------|:--------|:-----------------------------|:-----------|:--------------------------------------------------------------------------------------------------------------------------------------|
| `type`               | Enum    | No                           | Enum       | State backend type: `HASHMAP`, `ROCKSDB`, `CHANGELOG`, or `CUSTOM` (`StateBackendOptions.STATE_BACKEND`).                             |
| `checkpoint-storage` | Enum    | No                           | Enum       | Checkpoint storage mechanism: `JOBMANAGER` or `FILESYSTEM` (`CheckpointingOptions.CHECKPOINT_STORAGE`).                              |
| `storage-path`       | String  | No                           | String     | Base directory URI for state checkpoints, e.g. `s3://bucket/checkpoints` (`CheckpointingOptions.CHECKPOINTS_DIRECTORY`).              |
| `incremental`        | Boolean | No                           | Boolean    | Enable incremental checkpoints for RocksDB (`CheckpointingOptions.INCREMENTAL_CHECKPOINTS`).                                          |
| `latency-tracking`   | Boolean | No                           | Boolean    | Enable latency tracking metrics for state access (`StateBackendOptions.LATENCY_TRACK_ENABLED`).                                       |
| `custom-class`       | String  | **Yes** (if `type == CUSTOM`)| String     | Fully qualified class name for custom state backend. Allowed **only** when `type: CUSTOM`.                                            |

### Savepoint Restore Settings (`SavepointRestoreConfiguration`)

| Property Key               | Type    | Required | Validation  | Description                                                                                                                           |
|:---------------------------|:--------|:---------|:------------|:--------------------------------------------------------------------------------------------------------------------------------------|
| `savepoint-path`           | String  | **Yes**  | `@NotBlank` | Path to savepoint or initial checkpoint directory (`StateRecoveryOptions.SAVEPOINT_PATH`).                                            |
| `allow-non-restored-state` | Boolean | No       | Boolean     | Allow job to start even if state contains subtasks that cannot be restored (`StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE`).  |
| `restore-mode`             | Enum    | No       | Enum        | Savepoint restore mode: `CLAIM`, `NO_CLAIM`, or `LEGACY` (`StateRecoveryOptions.RESTORE_MODE`).                                        |

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

All configuration models enforce Jakarta Bean Validation rules at startup:
* `RestartStrategyConfiguration`: Validates that sub-blocks match the chosen `type` (*fail-fast* via `InvalidRestartStrategyConfigurationException`).
* `StateBackendConfiguration`: Validates that `custom-class` is provided **if and only if** `type: CUSTOM` (*fail-fast* via `InvalidStateBackendConfigurationException`).
