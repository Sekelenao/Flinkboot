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
- **Relocate Checkpoint Storage Configuration to CheckpointingProperties (#246)**: Decoupled checkpoint storage from `StateBackendProperties` (`state-backend.checkpoint-storage`) and relocated `CheckpointStorageType` (`storage: JOBMANAGER | FILESYSTEM`) into `CheckpointingProperties` (`checkpointing.storage`), aligning configuration structure with Flink 1.13+ runtime concerns (`CheckpointConfig`). Applications configuring `state-backend.checkpoint-storage` must migrate to `checkpointing.storage`. In Java, `CheckpointStorageType` is moved from package `com.flinkboot.core.api.properties.state` to `com.flinkboot.core.api.properties.checkpointing`.
- **Removal of Invalid `CHANGELOG` State Backend Type**: Removed `StateBackendType.CHANGELOG` to prevent runtime crashes (`DynamicCodeLoadingException`) caused by Flink attempting to load class `'changelog'`. The changelog feature is now configured as a dedicated boolean flag via `state-backend.changelog: true | false` layered on top of a base state backend (`HASHMAP` or `ROCKSDB`).
- **Savepoint Restore Mode Compatibility**: Removed `RestoreMode.LEGACY`. Applications configuring `restore-mode: LEGACY` or referencing `RestoreMode.LEGACY` in Java must migrate to the supported `CLAIM` or `NO_CLAIM` mode according to the intended savepoint ownership behavior.

#### flinkboot-kafka
- **Migration of `starting-offsets` to Nested Object**: In both YAML configuration and the Java API, `starting-offsets` is no longer a scalar value but a structured nested object requiring a `strategy` key (e.g. `starting-offsets: { strategy: EARLIEST }`). Flat configuration keys `starting-offsets-timestamp` and `starting-offsets-partition-offsets` have been removed in favor of `timestamp` and `partitions` within this nested block. In Java, flattened accessors on `KafkaSourceProperties` are replaced by `config.startingOffsets().strategy()`, `.timestamp()`, and `.partitions()`.

#### Global & Namespace
- **Canonical Namespace Migration to `com.flinkboot`**:
  - **Maven Coordinates**: Relocated root and all modules (`flinkboot-core`, `flinkboot-test`, `flinkboot-kafka`, `flinkboot-fluss`) from `io.github.sekelenao` to `com.flinkboot`.
  - **Java Package Hierarchy**: Migrated all classes, records, and interfaces from `io.github.sekelenao.flinkboot.*` to `com.flinkboot.*` across all modules.
  - **JPMS Module Descriptors**: Updated module names to `com.flinkboot.core`, `com.flinkboot.test`, `com.flinkboot.kafka`, and `com.flinkboot.fluss`.
  - **Migration Path**: Downstream applications update their dependency `<groupId>` to `com.flinkboot` (in `pom.xml` or `build.gradle`) and replace package imports `io.github.sekelenao.flinkboot` with `com.flinkboot` (see [flinkboot.com](https://flinkboot.com) for full release notes).

### 🟢 Features & Enhancements

#### flinkboot-core
- **Configurable Tolerable Checkpoint Failures (#330)**: Added optional `tolerable-checkpoint-failures` property to `CheckpointingProperties` (`checkpointing.tolerable-checkpoint-failures`) and wired it to Flink's `CheckpointingOptions.TOLERABLE_FAILURE_NUMBER`, allowing applications to tolerate transient checkpoint timeouts without failing the streaming job.
- **Configurable Exponential Delay Restart Attempts (#328)**: Added optional `attempts` property to `ExponentialDelayRestartProperties` (`restart-strategy.exponential-delay.attempts`) and wired it to Flink's `RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_ATTEMPTS`, allowing operators to declaratively bound restart attempts instead of defaulting to infinite retries.
- **Configurable Savepoint Directory (#329)**: Added the optional `directory` property to `SavepointRestoreProperties`, mapped to Flink's `state.savepoints.dir` configuration key, allowing target savepoint directories to be configured independently without requiring a mandatory `savepoint-path`.
- **Optional Local Web UI Enablement**: Removed `@NotNull` constraint on `LocalWebUiProperties.enabled` and aligned default behavior with `CheckpointingProperties`, activating the local Web UI by default when `local-web-ui` is declared without requiring an explicit `enabled: true`.
- **Configurable State Changelog Flag (`state-backend.changelog`)**: Added optional `changelog` boolean property to `StateBackendProperties` (`state-backend.changelog: true | false`) and wired it to Flink's `state.backend.changelog.enabled` (`StateChangelogOptions.ENABLE_STATE_CHANGE_LOG`), allowing users to toggle durable short-interval changelog checkpointing over HashMap or RocksDB backends.

#### flinkboot-kafka
- **Configurable Kafka Source Client ID Prefix**: Added optional `client-id-prefix` configuration and wired it to Flink's `KafkaSourceBuilder.setClientIdPrefix`, allowing parallel source subtasks to use distinct Kafka client IDs.
- **Dual-Mode Boundedness & Stopping Offsets Support (`boundedness` & `stopping-offsets`)**:
  - Added `boundedness: BOUNDED | UNBOUNDED` to switch Kafka ingestion between continuous streaming and Flink's native batch execution mode.
  - Added `stopping-offsets` configuration to automatically terminate ingestion upon reaching target offsets (specific timestamp, partition offsets, or current log end). Supports finite streaming when `boundedness: UNBOUNDED` and finite batch backfills when `boundedness: BOUNDED`.
- **Simplified Kafka Source Deserialization**: Added `KafkaSourceFactory` overloads accepting Flink's `DeserializationSchema<T>`, automatically adapting value-only consumers through `KafkaRecordDeserializationSchema.valueOnly(...)`. Added an optional `KafkaSourceBuilder<T>` customizer overload for inline source tuning before construction.
- **Configurable Reset Strategy for Explicit Partition Offsets (`reset-strategy`)**: Added `reset-strategy: EARLIEST | LATEST | NONE` (`KafkaOffsetResetStrategy`) to `KafkaOffsetProperties`, enforced when `strategy: OFFSETS`. Configures the underlying Flink `OffsetsInitializer.offsets(offsets, resetStrategy)` for unmapped partitions, preventing silent implicit fallback to `EARLIEST` and supporting strict failure with `NONE`.
- **Partition Subscription Mode (`partitions`)**: Added native partition-pinned consumption via `partitions` (`TopicPartitionProperties`), allowing Kafka sources to consume from explicit topic partitions in YAML (mutually exclusive with `topics` and `topic-pattern`) and wired directly to Flink's `KafkaSourceBuilder.setPartitions`.
- **Simplified Kafka Sink Serialization & Schema Customizers**: Added `KafkaSinkFactory.supplyFor` and `supplyBuilderFor` overloads accepting standard `SerializationSchema<T>` (automatically binding to `config.topic()`) and schema customizers `Consumer<KafkaRecordSerializationSchemaBuilder<T>>` for inline schema tuning (keys, partitioners, headers).
- **Customizable Kafka Sink Builder**: Added a `KafkaSinkFactory.supplyFor` overload accepting a `Consumer<KafkaSinkBuilder<T>>`, allowing sink builder options to be customized before the sink is built.
- **Schema & Sink Builder Customizers Combined**: Added a `KafkaSinkFactory.supplyFor` overload accepting both a `Consumer<KafkaRecordSerializationSchemaBuilder<T>>` schema customizer and a `Consumer<KafkaSinkBuilder<T>>` builder customizer, mirroring `KafkaSourceFactory`.
- **Value Serialization Schema & Sink Builder Customizer**: Added a `KafkaSinkFactory.supplyFor` overload accepting `SerializationSchema<T>` alongside a `Consumer<KafkaSinkBuilder<T>>` builder customizer, mirroring `KafkaSourceFactory`.
- **Customizable Kafka Source Builder with Full Record Deserializer**: Added a `KafkaSourceFactory.supplyFor` overload accepting `KafkaRecordDeserializationSchema<T>` alongside a `Consumer<KafkaSourceBuilder<T>>`, allowing full record deserialization sources to be customized before construction.

#### flinkboot-fluss
- **Customizable Fluss Source Builder**: Added a `FlussSourceFactory.supplyFor` overload accepting a `Consumer<FlussSourceBuilder<T>>`, allowing source builder options to be customized before the source is built.
- **Customizable Fluss Sink Builder**: Added a `FlussSinkFactory.supplyFor` overload accepting a builder customizer.

#### flinkboot-test
- **Aggregated Type Violations in POJO Assertions**: Consolidates all unsupported and missing type violations detected across complex domain models during BFS traversal into a single diagnostic `AssertionFailedError`, allowing developers to address all serialization violations in one pass rather than resolving them iteratively.
- **Fluent CollectingSink Assertions & Zero-Copy Accessors**: Added dedicated `FlinkbootAssertions.assertThat(CollectingSink<T>)` returning `CollectingSinkAssert<T>` for concise fluent testing of emitted records (`.hasSize(...)`, `.isEmpty()`, `.isNotEmpty()`, `.contains(...)`, `.anyMatch(...)`, `.allMatch(...)`, `.noneMatch(...)`), alongside direct zero-copy `size()` and `isEmpty()` accessors on `CollectingSink` and `CollectingSinkRegistry`.

#### Configuration & Cross-Cutting
- **Automatic YAML Kebab-Case Mapping & Unified Validation Paths**:
  - **Zero-Boilerplate Kebab-Case Deserialization**: `YamlParser` now configures `PropertyNamingStrategies.KEBAB_CASE` by default, automatically binding camelCase Java fields to kebab-case YAML keys without requiring manual `@JsonProperty` annotations on every field of custom configuration classes and records.
  - **Unified Bean Validation Diagnostics**: Jakarta Bean Validation violation paths reported during configuration validation across all modules and connectors (core, Kafka, Fluss) are systematically unified to YAML/JSON kebab-case (e.g. `bootstrap-servers`, `group-id`, `min-pause-between-checkpoints`, `storage-uri`, `startup-timestamp`, `transactional-id-prefix`), matching configuration keys rather than Java internal camelCase field names.
  - **Escape Hatch & Rollback**: If an application requires camelCase property binding for custom models in YAML, developers can either annotate specific fields with `@JsonProperty("camelCase")` or revert the naming strategy using the builder customizer: `boot.configuration(MyConfig.class, builder -> builder.propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE))`.

### 🔵 Fixes & Diagnostics

#### flinkboot-core
- **Checkpoint Storage Filesystem URI Validation (#246)**: Enforces that `storage-uri` is provided (not null and not blank) whenever `storage` is configured as `FILESYSTEM`, failing fast during Bean Validation with diagnostic message `"storage-uri must be specified when checkpoint storage is FILESYSTEM"` on property path `"storage-uri"`.
- **Canonical State Backend Custom Class Diagnostic Path**: Binds cross-field validation violations in `StateBackendProperties` (`custom-class must be specified...` and `custom-class can only be specified...`) to the canonical kebab-case configuration key `custom-class` instead of the internal Java field name `customClass`.
- **Checkpoint Concurrency Validation**: Rejects configurations combining a positive `min-pause-between-checkpoints` with `max-concurrent-checkpoints` greater than `1`, preventing Flink startup failures.
- **Interdependent Exponential Backoff Configuration**: Rejects an `exponential-delay` restart strategy that specifies only one of `initial-backoff` or `max-backoff`, preventing the Flink runtime failure (`initialBackoff cannot be greater than maxBackoff`) caused by upstream default fallbacks, and reports the violation against the canonical `exponential-delay` configuration key.
- **Exponential Backoff Multiplier Validation**: Rejects `backoff-multiplier: 1.0` during configuration validation, matching Flink's requirement that the multiplier be strictly greater than `1.0`.
- **Buffer Timeout Maximum Throughput Support**: Allows `buffer-timeout: -1` (or `-1ms`) to disable periodic buffer flushing (`BUFFER_TIMEOUT_ENABLED = false`) for maximum throughput in streaming pipelines, resolving validation failures caused by `@DurationMin(millis = 0)`.

#### flinkboot-kafka
- **Fail Fast on Duplicate Topic-Partition Offsets (#361)**: Detects duplicate `(topic, partition)` entries in `OffsetInitializerMapper` when configuring per-partition offsets and throws `ConfigurationValidationException` identifying the duplicate `TopicPartition` instead of silently overwriting earlier values.
- **Require Explicit Boundedness with Stopping Offsets (#327)**: Enforces that `boundedness` is explicitly specified (`BOUNDED` or `UNBOUNDED`) when `stopping-offsets` is configured in `KafkaSourceProperties`, preventing implicit fallback to `UNBOUNDED` from failing Flink batch job submissions with `IllegalArgumentException`.
- **Properties Escape Hatch Application Order (#321)**: Applies client configuration from `properties` last in `KafkaSourceFactory` and `KafkaSinkFactory`, ensuring lower-level driver configurations provided via the universal properties escape hatch take precedence over typed defaults and options.
- **Null Delivery Guarantee Validation**: Prevents misleading cross-field validation errors when `delivery-guarantee` is missing, leaving the missing-value error to the field-level `@NotNull` constraint.

#### flinkboot-fluss
- **Explicit Null Preconditions in Factory Methods (#305)**: Enforces explicit `Objects.requireNonNull` validation on `config` and `schema` parameters within `FlussSourceFactory.supplyFor` and `FlussSinkFactory.supplyFor`, maintaining API symmetry with `flinkboot-kafka` and failing fast at public boundaries.

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
