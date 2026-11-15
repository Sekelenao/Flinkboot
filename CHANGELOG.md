# Changelog

All notable user-facing changes to this project are documented in this file.

> [!NOTE]
> This changelog strictly documents changes that directly impact the end user (public APIs, configuration schemas, CLI options, runtime behavior, and bug fixes). Pure internal refactorings, private class movements, and minor documentation or typo fixes are intentionally omitted.

### Legend
- 🔴 **Breaking Change**: Incompatible API, configuration schema, or behavioral change requiring migration.
- 🟢 **Feature / Added**: New capability, public API, configuration property, or testing utility.
- 🔵 **Fix / Robustness**: Bug fix, boundary safeguard, or improved diagnostic message impacting user experience.

---

## [0.6.0-1.20]

### 🔴 Breaking Changes

#### flinkboot-core
- **Relocate Checkpoint Storage Configuration to CheckpointingProperties (#246)**: Moved checkpoint storage from `StateBackendProperties` (`state-backend.checkpoint-storage`) to `CheckpointingProperties` (`checkpointing.storage: JOBMANAGER | FILESYSTEM`), aligning with Flink's `CheckpointConfig`. In Java, `CheckpointStorageType` moved from `com.flinkboot.core.api.properties.state` to `com.flinkboot.core.api.properties.checkpointing`.
- **Removal of Invalid `CHANGELOG` State Backend Type**: Removed `StateBackendType.CHANGELOG` to prevent runtime `DynamicCodeLoadingException` failures. State changelogging is now configured via `state-backend.changelog: true | false` on top of `HASHMAP` or `ROCKSDB`.
- **Savepoint Restore Mode Compatibility**: Removed `RestoreMode.LEGACY`. Applications must migrate to `CLAIM` or `NO_CLAIM`.

#### flinkboot-kafka
- **Optional `group-id` in `KafkaSourceProperties` (#362)**: `KafkaSourceProperties#groupId()` now returns `Optional<String>` instead of `String`, allowing ephemeral or read-only sources without a consumer group. `group-id` remains required when `starting-offsets` or `stopping-offsets` uses a `COMMITTED*` strategy (`COMMITTED`, `COMMITTED_EARLIEST`, `COMMITTED_LATEST`).
- **Migration of `starting-offsets` to Nested Object**: `starting-offsets` is now a nested object requiring a `strategy` key (e.g. `starting-offsets: { strategy: EARLIEST }`). Flat keys `starting-offsets-timestamp` and `starting-offsets-partition-offsets` are replaced by `timestamp` and `partitions` inside this block, with matching Java accessors `config.startingOffsets().strategy()`, `.timestamp()`, and `.partitions()`.

#### Global & Namespace
- **Canonical Namespace Migration to `com.flinkboot`**:
  - **Maven Coordinates**: Relocated root and all modules (`flinkboot-core`, `flinkboot-test`, `flinkboot-kafka`, `flinkboot-fluss`) from `io.github.sekelenao` to `com.flinkboot`.
  - **Java Package Hierarchy**: Migrated all public and internal types from `io.github.sekelenao.flinkboot.*` to `com.flinkboot.*`.
  - **JPMS Module Descriptors**: Renamed modules to `com.flinkboot.core`, `com.flinkboot.test`, `com.flinkboot.kafka`, and `com.flinkboot.fluss`.
  - **Migration Path**: Update dependency `<groupId>` to `com.flinkboot` and replace `io.github.sekelenao.flinkboot` imports with `com.flinkboot` (see [flinkboot.com](https://flinkboot.com) for full release notes).

### 🟢 Features & Enhancements

#### flinkboot-core
- **Configurable Tolerable Checkpoint Failures (#330)**: Added optional `checkpointing.tolerable-checkpoint-failures` (`CheckpointingProperties`), wired to Flink's `CheckpointingOptions.TOLERABLE_FAILURE_NUMBER`.
- **Configurable Exponential Delay Restart Attempts (#328)**: Added optional `restart-strategy.exponential-delay.attempts` (`ExponentialDelayRestartProperties`), wired to Flink's `RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_ATTEMPTS`.
- **Configurable Savepoint Directory (#329)**: Added optional `directory` to `SavepointRestoreProperties` (`state.savepoints.dir`), allowing savepoint target directories to be configured without requiring `savepoint-path`.
- **Optional Local Web UI Enablement**: Made `LocalWebUiProperties.enabled` optional, enabling the local Web UI by default whenever `local-web-ui` is declared without requiring an explicit `enabled: true`.
- **Configurable State Changelog Flag (`state-backend.changelog`)**: Added optional `changelog` boolean property to `StateBackendProperties`, wired to Flink's `state.backend.changelog.enabled`.

#### flinkboot-kafka
- **Configurable Kafka Source Client ID Prefix**: Added optional `client-id-prefix` wired to `KafkaSourceBuilder.setClientIdPrefix` so parallel source subtasks use distinct Kafka client IDs.
- **Dual-Mode Boundedness & Stopping Offsets Support (`boundedness` & `stopping-offsets`) (#327)**:
  - Added `boundedness: BOUNDED | UNBOUNDED` to switch between continuous streaming and Flink's native batch execution mode.
  - Added `stopping-offsets` to terminate ingestion at target offsets (timestamp, partition offsets, or current log end). Requires `boundedness` to be explicitly specified to prevent implicit fallback failures in batch jobs.
- **Simplified Kafka Source & Sink Serialization APIs**:
  - Added `KafkaSourceFactory` overloads accepting Flink's `DeserializationSchema<T>` for value-only consumers.
  - Added `KafkaSinkFactory.supplyFor` and `supplyBuilderFor` overloads accepting `SerializationSchema<T>` (bound to `config.topic()`) and `Consumer<KafkaRecordSerializationSchemaBuilder<T>>` customizers for keys, partitioners, and headers.
- **Configurable Reset Strategy for Explicit Partition Offsets (`reset-strategy`)**: Added `reset-strategy: EARLIEST | LATEST | NONE` (`KafkaOffsetResetStrategy`) to `KafkaOffsetProperties` when `strategy: OFFSETS`, configuring `OffsetsInitializer.offsets(offsets, resetStrategy)` for unmapped partitions instead of silently falling back to `EARLIEST`.
- **Partition Subscription Mode (`partitions`)**: Added explicit topic-partition subscription via `partitions` (`TopicPartitionProperties`), mutually exclusive with `topics` and `topic-pattern`, wired to `KafkaSourceBuilder.setPartitions`.

#### flinkboot-test
- **Aggregated Type Violations in POJO Assertions**: Collects all unsupported and missing type violations across complex domain models into a single `AssertionFailedError` so developers can fix all serialization issues in one pass.
- **Fluent CollectingSink Assertions & Zero-Copy Accessors**: Added `FlinkbootAssertions.assertThat(CollectingSink<T>)` (`CollectingSinkAssert<T>`) with `.hasSize(...)`, `.isEmpty()`, `.isNotEmpty()`, `.contains(...)`, `.anyMatch(...)`, `.allMatch(...)`, and `.noneMatch(...)`, plus direct `size()` and `isEmpty()` accessors on `CollectingSink` and `CollectingSinkRegistry`.

#### Configuration & Cross-Cutting
- **Automatic YAML Kebab-Case Mapping & Unified Validation Paths**:
  - **Zero-Boilerplate Kebab-Case Deserialization**: `YamlParser` now uses `PropertyNamingStrategies.KEBAB_CASE` by default, binding camelCase Java fields to kebab-case YAML keys without manual `@JsonProperty` annotations.
  - **Unified Bean Validation Diagnostics**: Bean Validation violation paths across all modules are systematically reported using canonical YAML/JSON kebab-case keys (e.g. `bootstrap-servers`, `group-id`, `storage-uri`, `custom-class`) rather than Java field names.
  - **Escape Hatch**: Custom models requiring camelCase in YAML can use `@JsonProperty("camelCase")` or revert the strategy via `boot.configuration(MyConfig.class, builder -> builder.propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE))`.

### 🔵 Fixes & Diagnostics

#### flinkboot-core
- **Unaligned Checkpoints Mode Validation (#391)**: Rejects `checkpointing.unaligned-checkpoints: true` during Bean Validation when `checkpointing.mode` is `AT_LEAST_ONCE`, preventing runtime failures since Flink only supports unaligned checkpoints with `EXACTLY_ONCE`.
- **Safe `null` Handling in `DurationComparator` (#390)**: Handled `null` `Duration` values consistently across `hash`, `equalToReference`, `compareToReference`, and `compare` (using `null < non-null` ordering respecting sort direction), preventing `NullPointerException` when comparing or hashing nullable `Duration` records.
- **Aligned Checkpoint Timeout Validation (#370)**: Rejects `checkpointing.aligned-checkpoint-timeout` during Bean Validation when `checkpointing.unaligned-checkpoints` is explicitly `false`, preventing configurations that Flink would silently ignore at runtime.
- **Disable Checkpointing When `checkpointing.enabled` Is `false` (#368)**: Calls `disableCheckpointing()` on the created `StreamExecutionEnvironment` when `checkpointing.enabled` is explicitly `false`, preventing cluster-level `execution.checkpointing.interval` defaults from keeping checkpointing active.
- **Support `Duration` Keys in `.keyBy(...)` (#365)**: `DurationTypeInfo` now implements `AtomicType<Duration>`, resolving runtime failures when using `Duration` fields or streams as keys in Flink `.keyBy(...)` operations.
- **Checkpoint Storage URI Validation (#246, #369)**: Enforces during Bean Validation that `checkpointing.storage-uri` is non-blank when `storage` is `FILESYSTEM`, and rejects configurations that set `storage-uri` when `storage` is `JOBMANAGER`.
- **Exponential Backoff Configuration Validation**: Rejects `exponential-delay` configurations specifying only one of `initial-backoff` or `max-backoff` (preventing Flink runtime failures from upstream default fallbacks) and rejects `backoff-multiplier: 1.0` (must be strictly greater than `1.0`).
- **Checkpoint Concurrency Validation**: Rejects configurations combining a positive `min-pause-between-checkpoints` with `max-concurrent-checkpoints` greater than `1`, preventing Flink startup failures.
- **Buffer Timeout Maximum Throughput Support**: Allows `buffer-timeout: -1` (or `-1ms`) to disable periodic buffer flushing (`BUFFER_TIMEOUT_ENABLED = false`) for maximum throughput.

#### flinkboot-kafka
- **Fail Fast on Duplicate Topic-Partition Offsets (#361)**: Detects duplicate `(topic, partition)` entries in per-partition offset configurations and throws `ConfigurationValidationException` instead of silently overwriting earlier values.
- **Properties Escape Hatch Application Order (#321)**: Applies client configuration from `properties` last in `KafkaSourceFactory` and `KafkaSinkFactory`, ensuring driver-level properties override typed defaults and options.

---

## [0.5.0-1.20]

### 🔴 Breaking Changes

#### flinkboot-core
- **Single Checkpoint Storage Path**: Removed `state-backend.storage-path`. Checkpoint storage location must be configured exclusively with `checkpointing.storage-uri`.
- **Relocate Parsing Exceptions**: Relocated `YamlParsingException` and `UnresolvedPropertyPlaceholderException` to package `io.github.sekelenao.flinkboot.core.api.exception.parsing`.
- **Cluster WebUI Runtime Incompatibility**: Replaced `InvalidLocalWebUiPropertiesException` with `UnsupportedExecutionEnvironmentException` in package `io.github.sekelenao.flinkboot.core.api.exception.execution`.

#### flinkboot-kafka
- **Unified `KafkaSourceProperties` DTO**: Consolidated `KafkaSourceTopicListProperties` and `KafkaSourceTopicPatternProperties` into a single unified `KafkaSourceProperties` DTO with mutually exclusive `topics` and `topic-pattern`.
- **Mandatory `KafkaSinkProperties.deliveryGuarantee`**: Enforced `@NotNull` on `KafkaSinkProperties.deliveryGuarantee` and updated its getter to return `KafkaDeliveryGuarantee` directly, eliminating silent fallbacks to Flink's `DeliveryGuarantee.NONE` and ensuring fail-fast validation against accidental data loss in streaming pipelines.

#### flinkboot-fluss
- **Streamlined `FlussSinkProperties` & Relocate Client Tuning**: Removed top-level `batch-size` and `batch-timeout` fields from `FlussSinkProperties`. Vendor client tuning must now be configured directly under `properties: Map<String, String>`.
- **Modernized `FlussStartupMode.offsetsInitializer()` Contract**: Returns `Optional<OffsetsInitializer>` instead of a nullable instance (`Optional.empty()` for `TIMESTAMP` mode), eliminating NPE hazards.

#### flinkboot-test
- **Removal of `FlinkbootTest`**: Removed the `FlinkbootTest` utility class. Configuration loading in tests is now performed directly via `Flinkboot.initialize(String... args)` in `flinkboot-core`, providing full parity with production runtime and supporting arbitrary CLI options, parameters, and flags.

#### Connectors & Cross-Cutting
- **Final Configuration DTO Classes**: Enforced `final` class modifier on all connector configuration DTOs (`KafkaSourceProperties`, `KafkaSinkProperties`, `FlussSourceProperties`, `FlussSinkProperties`).
- **Removal of Domain Validation Exceptions**: Removed DTO-specific validation exceptions (`InvalidExecutionPropertiesException`, `InvalidRestartStrategyPropertiesException`, `InvalidStateBackendPropertiesException`, `InvalidFlussSourcePropertiesException`, `InvalidKafkaSinkPropertiesException`, `InvalidKafkaSourcePropertiesException`). Cross-field validations are now evaluated uniformly via Jakarta Bean Validation and reported through `ConfigurationValidationException`.
- **Removed Empty Exception Packages**: Removed packages `io.github.sekelenao.flinkboot.kafka.api.exception` and `io.github.sekelenao.flinkboot.fluss.api.exception` following the elimination of connector domain validation exceptions.

### 🟢 Features & Enhancements

#### flinkboot-core
- **Varargs `Flinkboot.initialize(String... args)`**: Updated `Flinkboot.initialize` to accept varargs, enabling zero-boilerplate initialization (`Flinkboot.initialize()`) in tests and programmatic setups while remaining 100% binary and source compatible with `main(String[] args)`.
- **Modern `SerializerConfig` SPI in `DurationTypeInfo`**: Implemented modern `createSerializer(SerializerConfig)` on `DurationTypeInfo` aligned with Apache Flink 1.20+ (FLIP-398), deprecating legacy `createSerializer(ExecutionConfig)` for removal in Flink 2.0.
- **Disable Configuration Validation Flag**: Added `--flinkboot-configuration-disable-validation` CLI flag and `FLINKBOOT_CONFIGURATION_DISABLE_VALIDATION` environment variable to bypass Jakarta Bean Validation during configuration deserialization.
- **Self-Validating Configuration Contract (`@ValidConfiguration` & `ValidatableProperties`)**: Added `ValidConfiguration` constraint annotation and `ValidatableProperties` interface in package `io.github.sekelenao.flinkboot.core.api.validation` allowing configuration DTOs to declare cross-field Bean Validation rules evaluated during configuration loading.
- **Execution Parallelism Validation**: Enforces that `parallelism` cannot exceed `max-parallelism` when both are defined in `ExecutionProperties`.
- **Windows Path Normalization**: Seamless support for RFC 8089 file URIs (`file:///C:/...`), leading slashes, and UNC network shares on Windows without altering POSIX behavior.

#### flinkboot-test
- **Thread-Safe Collecting Sink**: Added `CollectingSink<T>` in package `io.github.sekelenao.flinkboot.test.api.sink` to collect elements emitted by Flink streams during local tests via Flink's modern SinkV2 API (`sinkTo(sink)`), with non-blocking concurrent storage and support for parallel subtasks.
- **Generic & Custom Type Assertions**: Added `TypeInformationAssert<T>`, `assertThat(TypeHint<T>)`, and `assertThat(TypeInformation<T>)` to verify types whose generic parameters are erased by a `Class` literal.

#### Configuration & Cross-Cutting
- **Strict Duration Validation (`@DurationMin`)**: Validates `Duration` configuration properties across all modules, rejecting negative and zero values on strict intervals and timeouts.
- **Container Element Validation**: Enforces `@NotBlank` on collection elements (`bootstrap-servers`, `topics`) and `@NotNull` on properties map keys and values.
- **Local Web UI Port Range Validation (`@Range`)**: Enforces `@Range(min = 0, max = 65535)` on `LocalWebUiProperties.port`, allowing port `0` for dynamic ephemeral port allocation.
- **Local Web UI Mandatory Enablement (`@NotNull`)**: Enforces `@NotNull` on `LocalWebUiProperties.enabled` to reject YAML configurations that omit `enabled`.
- **Exhaustive Validation Diagnostics**: Configuration loading reports all missing and invalid fields simultaneously in a structured multi-line report instead of failing on the first missing field.

#### BOM & Dependencies
- **RocksDB State Backend Dependency**: Added `org.apache.flink:flink-statebackend-rocksdb` (scope `provided`) to BOM `dependencyManagement` for managed state storage.
- **Apache Fluss 1.0.0**: Upgraded Apache Fluss from `0.9.1-incubating` to official `1.0.0` release.

### 🔵 Fixes & Diagnostics

#### flinkboot-core
- **Typed Command Line Parsing Exception**: Throws `CommandLineParsingException` instead of `NoSuchElementException` when a CLI option is missing its required value.
- **Windows Drive Letter Normalization**: Normalizes leading slashes before drive letters (`/C:/...` -> `C:/...`) in `FileSystemResource`.
- **Checkpointing Disable Flag**: An explicit `checkpointing.enabled: false` now strictly prevents all settings in the checkpointing block from being applied.
- **Zero Restart Attempts Support**: Allows zero restart attempts via `@PositiveOrZero` on `FixedDelayRestartProperties.attempts` to fail immediately on first failure without retries.
- **Configurable Violations Log Size**: Validates positive values for `--flinkboot-configuration-violations-log-size` while preserving the default limit of 10.
- **Uniform Parsing Exception Diagnostic**: Wraps all Jackson conversion errors in `YamlParsingException` and displays Fully Qualified Class Names (FQCN) in mapping error messages.
- **Supported URI Schemes in Error Message**: Clarified supported URI prefixes (`classpath:`, `file:`) in `UnrecognizedResourceException` detail messages.
- **Typed Integer Parsing Exception**: Throws dedicated `IntegerParsingException` (inheriting from `FlinkbootException`) instead of raw `IllegalArgumentException` when parsing invalid or non-strictly-positive startup integer parameters (such as `flinkboot-configuration-violations-log-size`).

#### flinkboot-kafka
- **Non-Short-Circuiting Source Validation**: Evaluates topic subscription and starting offsets constraints unconditionally in `KafkaSourcePropertiesValidator`, collecting and reporting all configuration violations simultaneously in a single diagnostic report.

#### flinkboot-test
- **POJO Assertion Failure on Type Extraction Error**: Catches Flink's raw `InvalidTypesException` during POJO type extraction in `ClassAssert.isPojo()` and rethrows it as an `AssertionFailedError` with the original exception attached as cause, ensuring consistent test failure behavior.

#### Configuration & Cross-Cutting
- **Unified Cross-Field Error Reporting**: Cross-field configuration constraints are now collected and reported alongside field-level validation errors in a single diagnostic report instead of interrupting deserialization prematurely.

---

## [0.4.0-1.20]

### 🔴 Breaking Changes

#### flinkboot-core & flinkboot-fluss
- **Native `Duration` Support in Properties**: Replaced raw millisecond numerical fields with `java.time.Duration` across `CheckpointingProperties`, `ExecutionProperties`, restart strategies, and `FlussSinkProperties`, standardizing property keys by removing `-ms` suffixes.

### 🟢 Features & Enhancements

#### flinkboot-core
- **Environment Variable Placeholder Interpolation**: Strict `${VARIABLE_NAME}` placeholder interpolation across all YAML properties, scalar fields, array elements, and nested objects with case normalization and escaping.

#### flinkboot-fluss
- **Apache Fluss Connector Module**: Real-time streaming storage Source and Sink connectors on Apache Flink 1.20 (`FlussSourceFactory`, `FlussSinkFactory`, `FlussStartupMode`).

#### flinkboot-test
- **Fluent Assertion API**: Introduced `io.github.sekelenao.flinkboot.test.api.assertion.FlinkbootAssertions` with `ClassAssert.isPojo()` for recursive POJO compliance verification with zero Kryo fallback.

#### BOM & Dependencies
- **Web Dashboard Dependency**: Added `org.apache.flink:flink-runtime-web` (scope `provided`) to BOM `dependencyManagement` for local Web Dashboard debugging.

### 🔵 Fixes & Diagnostics

#### flinkboot-core
- **Multi-Line Validation Error Reporting**: Formats Bean Validation errors as structured multi-line lists with deterministic sorting and configurable log size limits.
- **CLI Argument Syntax in Javadoc**: Corrected CLI parameter syntax examples in `Flinkboot` Javadoc.

#### flinkboot-test
- **Flexible Zero-Kryo POJO Assertion**: Enhanced `PojoValidator` to validate zero-Kryo safety across compliant Flink types, custom factories, and composite types.

---

## [0.3.0-1.20]

### 🔴 Breaking Changes

#### flinkboot-core
- **Default Configuration Location to Classpath**: Changed default configuration lookup location from `file:job-configuration.yaml` (local filesystem) to `classpath:job-configuration.yaml` (JAR classpath resources).

---

## [0.2.0-1.20]

### 🟢 Features & Enhancements

#### flinkboot-core
- **Unified Resource API (`Resource.of`)**: Public `Resource` interface supporting `classpath:`, `resource:`, and `file:` URI schemes with multi-platform path normalization.
- **Native JDK Type Serialization**: `@TypeInfo` factories for `Duration`, `LocalDate`, `LocalDateTime`, `LocalTime`, `List<E>`, `Map<K, V>` avoiding Kryo fallbacks.

#### flinkboot-kafka
- **Operator Naming**: Added `name` configuration property to `KafkaSourceProperties` and `KafkaSinkProperties`.

#### flinkboot-test
- **Deep Recursive POJO Validation**: `FlinkbootTest.assertPojo` validating complex structures recursively (nested POJOs, inheritance hierarchies, generic collections) against Kryo fallback.
- **Test Configuration Helper**: `FlinkbootTest.configuration` to load and merge YAML configurations in unit and integration tests.

### 🔵 Fixes & Diagnostics

#### flinkboot-core
- **Java 8 Date/Time Deserialization**: Bundled `jackson-datatype-jsr310` and registered `JavaTimeModule` in `YamlParser` to guarantee out-of-the-box support for `Duration` and Java 8 date/time types.

---

## [0.1.0-1.20]

### 🟢 Features & Enhancements

#### flinkboot-core
- **Framework Inception**: Core startup environment, command-line arguments, environment variable resolution, YAML configuration loading, and multi-file merging.
- **Declarative Environments**: Declarative `StreamExecutionEnvironment` creation (checkpointing, state backends, restart strategies, savepoints, local Web UI).

#### flinkboot-kafka
- **Kafka Connector**: Kafka source and sink factory helpers (`flinkboot-kafka`).

#### flinkboot-test
- **POJO Testing**: Basic POJO compliance assertions.
