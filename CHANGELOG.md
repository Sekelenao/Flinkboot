# Changelog

All notable user-facing changes to this project are documented in this file.

> [!NOTE]
> This changelog strictly documents changes that directly impact the end user (public APIs, configuration schemas, CLI options, runtime behavior, and bug fixes). Pure internal refactorings, private class movements, and minor documentation or typo fixes are intentionally omitted.

### Legend
- 🔴 **Breaking Change**: Incompatible API, configuration schema, or behavioral change requiring migration.
- 🟢 **Feature / Added**: New capability, public API, configuration property, or testing utility.
- 🔵 **Fix / Robustness**: Bug fix, boundary safeguard, or improved diagnostic message impacting user experience.

---

## [0.5.0-1.20]

### 🔴 Breaking Changes
- **[flinkboot-core] Single Checkpoint Storage Path**: Removed `state-backend.storage-path`. Checkpoint storage location must be configured exclusively with `checkpointing.storage-uri`.
- **[flinkboot-core] Relocate Parsing Exceptions**: Relocated `YamlParsingException` and `UnresolvedPropertyPlaceholderException` to package `io.github.sekelenao.flinkboot.core.api.exception.parsing`.
- **[flinkboot-core] Cluster WebUI Runtime Incompatibility**: Replaced `InvalidLocalWebUiPropertiesException` with `UnsupportedExecutionEnvironmentException` in package `io.github.sekelenao.flinkboot.core.api.exception.execution`.
- **[flinkboot-kafka] Unified `KafkaSourceProperties` DTO**: Consolidated `KafkaSourceTopicListProperties` and `KafkaSourceTopicPatternProperties` into a single unified `KafkaSourceProperties` DTO with mutually exclusive `topics` and `topic-pattern`.
- **[flinkboot-fluss] Streamlined `FlussSinkProperties` & Relocate Client Tuning**: Removed top-level `batch-size` and `batch-timeout` fields from `FlussSinkProperties`. Vendor client tuning must now be configured directly under `properties: Map<String, String>`.
- **[flinkboot-fluss] Modernized `FlussStartupMode.offsetsInitializer()` Contract**: Returns `Optional<OffsetsInitializer>` instead of a nullable instance (`Optional.empty()` for `TIMESTAMP` mode), eliminating NPE hazards.
- **[connectors] Final Configuration DTO Classes**: Enforced `final` class modifier on all connector configuration DTOs (`KafkaSourceProperties`, `KafkaSinkProperties`, `FlussSourceProperties`, `FlussSinkProperties`).
- **[configuration] Removal of Domain Validation Exceptions**: Removed DTO-specific validation exceptions (`InvalidExecutionPropertiesException`, `InvalidRestartStrategyPropertiesException`, `InvalidStateBackendPropertiesException`, `InvalidFlussSourcePropertiesException`, `InvalidKafkaSinkPropertiesException`, `InvalidKafkaSourcePropertiesException`). Cross-field validations are now evaluated uniformly via Jakarta Bean Validation and reported through `ConfigurationValidationException`.
- **[flinkboot-kafka & fluss] Removed Empty Exception Packages**: Removed packages `io.github.sekelenao.flinkboot.kafka.api.exception` and `io.github.sekelenao.flinkboot.fluss.api.exception` following the elimination of connector domain validation exceptions.
- **[flinkboot-test] Removal of `FlinkbootTest`**: Removed the `FlinkbootTest` utility class. Configuration loading in tests is now performed directly via `Flinkboot.initialize(String... args)` in `flinkboot-core`, providing full parity with production runtime and supporting arbitrary CLI options, parameters, and flags.

### 🟢 Features & Enhancements
- **[flinkboot-core] Varargs `Flinkboot.initialize(String... args)`**: Updated `Flinkboot.initialize` to accept varargs, enabling zero-boilerplate initialization (`Flinkboot.initialize()`) in tests and programmatic setups while remaining 100% binary and source compatible with `main(String[] args)`.
- **[flinkboot-core] Modern `SerializerConfig` SPI in `DurationTypeInfo`**: Implemented modern `createSerializer(SerializerConfig)` on `DurationTypeInfo` aligned with Apache Flink 1.20+ (FLIP-398), deprecating legacy `createSerializer(ExecutionConfig)` for removal in Flink 2.0.
- **[flinkboot-core] Disable Configuration Validation Flag**: Added `--flinkboot-configuration-disable-validation` CLI flag and `FLINKBOOT_CONFIGURATION_DISABLE_VALIDATION` environment variable to bypass Jakarta Bean Validation during configuration deserialization.
- **[flinkboot-core] Self-Validating Configuration Contract (`@ValidConfiguration` & `ValidatableProperties`)**: Added `ValidConfiguration` constraint annotation and `ValidatableProperties` interface in package `io.github.sekelenao.flinkboot.core.api.validation` allowing configuration DTOs to declare cross-field Bean Validation rules evaluated during configuration loading.
- **[flinkboot-core] Execution Parallelism Validation**: Enforces that `parallelism` cannot exceed `max-parallelism` when both are defined in `ExecutionProperties`.
- **[flinkboot-core] Windows Path Normalization**: Seamless support for RFC 8089 file URIs (`file:///C:/...`), leading slashes, and UNC network shares on Windows without altering POSIX behavior.
- **[flinkboot-test] Thread-Safe Collecting Sink**: Added `CollectingSink<T>` in package `io.github.sekelenao.flinkboot.test.api.sink` to collect elements emitted by Flink streams during local tests via Flink's modern SinkV2 API (`sinkTo(sink)`), with non-blocking concurrent storage and support for parallel subtasks.
- **[flinkboot-test] Generic & Custom Type Assertions**: Added `TypeInformationAssert<T>`, `assertThat(TypeHint<T>)`, and `assertThat(TypeInformation<T>)` to verify types whose generic parameters are erased by a `Class` literal.
- **[configuration] Strict Duration Validation (`@DurationMin`)**: Validates `Duration` configuration properties across all modules, rejecting negative and zero values on strict intervals and timeouts.
- **[configuration] Container Element Validation**: Enforces `@NotBlank` on collection elements (`bootstrap-servers`, `topics`) and `@NotNull` on properties map keys and values.
- **[configuration] Local Web UI Port Range Validation (`@Range`)**: Enforces `@Range(min = 0, max = 65535)` on `LocalWebUiProperties.port`, allowing port `0` for dynamic ephemeral port allocation.
- **[configuration] Local Web UI Mandatory Enablement (`@NotNull`)**: Enforces `@NotNull` on `LocalWebUiProperties.enabled` to reject YAML configurations that omit `enabled`.
- **[configuration] Exhaustive Validation Diagnostics**: Configuration loading reports all missing and invalid fields simultaneously in a structured multi-line report instead of failing on the first missing field.

### 🔵 Fixes & Robustness
- **[flinkboot-core] Typed Command Line Parsing Exception**: Throws `CommandLineParsingException` instead of `NoSuchElementException` when a CLI option is missing its required value.
- **[flinkboot-core] Windows Drive Letter Normalization**: Normalizes leading slashes before drive letters (`/C:/...` -> `C:/...`) in `FileSystemResource`.
- **[flinkboot-core] Checkpointing Disable Flag**: An explicit `checkpointing.enabled: false` now strictly prevents all settings in the checkpointing block from being applied.
- **[flinkboot-core] Zero Restart Attempts Support**: Allows zero restart attempts via `@PositiveOrZero` on `FixedDelayRestartProperties.attempts` to fail immediately on first failure without retries.
- **[flinkboot-core] Configurable Violations Log Size**: Validates positive values for `--flinkboot-configuration-violations-log-size` while preserving the default limit of 10.
- **[flinkboot-core] Uniform Parsing Exception Diagnostic**: Wraps all Jackson conversion errors in `YamlParsingException` and displays Fully Qualified Class Names (FQCN) in mapping error messages.
- **[flinkboot-core] Supported URI Schemes in Error Message**: Clarified supported URI prefixes (`classpath:`, `file:`) in `UnrecognizedResourceException` detail messages.
- **[configuration] Unified Cross-Field Error Reporting**: Cross-field configuration constraints are now collected and reported alongside field-level validation errors in a single diagnostic report instead of interrupting deserialization prematurely.

---

## [0.4.0-1.20]

### 🔴 Breaking Changes
- **[flinkboot-core & fluss] Native `Duration` Support in Properties**: Replaced raw millisecond numerical fields with `java.time.Duration` across `CheckpointingProperties`, `ExecutionProperties`, restart strategies, and `FlussSinkProperties`, standardizing property keys by removing `-ms` suffixes.

### 🟢 Features & Enhancements
- **[flinkboot-core] Environment Variable Placeholder Interpolation**: Strict `${VARIABLE_NAME}` placeholder interpolation across all YAML properties, scalar fields, array elements, and nested objects with case normalization and escaping.
- **[flinkboot-fluss] Apache Fluss Connector Module**: Real-time streaming storage Source and Sink connectors on Apache Flink 1.20 (`FlussSourceFactory`, `FlussSinkFactory`, `FlussStartupMode`).
- **[flinkboot-test] Fluent Assertion API**: Introduced `io.github.sekelenao.flinkboot.test.api.assertion.FlinkbootAssertions` with `ClassAssert.isPojo()` for recursive POJO compliance verification with zero Kryo fallback.
- **[bom] Web Dashboard Dependency**: Added `org.apache.flink:flink-runtime-web` (scope `provided`) to BOM `dependencyManagement` for local Web Dashboard debugging.

### 🔵 Fixes & Diagnostics
- **[flinkboot-core] Multi-Line Validation Error Reporting**: Formats Bean Validation errors as structured multi-line lists with deterministic sorting and configurable log size limits.
- **[flinkboot-core] CLI Argument Syntax in Javadoc**: Corrected CLI parameter syntax examples in `Flinkboot` Javadoc.
- **[flinkboot-test] Flexible Zero-Kryo POJO Assertion**: Enhanced `PojoValidator` to validate zero-Kryo safety across compliant Flink types, custom factories, and composite types.

---

## [0.3.0-1.20]

### 🔴 Breaking Changes
- **[flinkboot-core] Default Configuration Location to Classpath**: Changed default configuration lookup location from `file:job-configuration.yaml` (local filesystem) to `classpath:job-configuration.yaml` (JAR classpath resources).

---

## [0.2.0-1.20]

### 🟢 Features & Enhancements
- **[flinkboot-core] Unified Resource API (`Resource.of`)**: Public `Resource` interface supporting `classpath:`, `resource:`, and `file:` URI schemes with multi-platform path normalization.
- **[flinkboot-core] Native JDK Type Serialization**: `@TypeInfo` factories for `Duration`, `LocalDate`, `LocalDateTime`, `LocalTime`, `List<E>`, `Map<K, V>` avoiding Kryo fallbacks.
- **[flinkboot-test] Deep Recursive POJO Validation**: `FlinkbootTest.assertPojo` validating complex structures recursively (nested POJOs, inheritance hierarchies, generic collections) against Kryo fallback.
- **[flinkboot-test] Test Configuration Helper**: `FlinkbootTest.configuration` to load and merge YAML configurations in unit and integration tests.
- **[flinkboot-kafka] Operator Naming**: Added `name` configuration property to `KafkaSourceProperties` and `KafkaSinkProperties`.

### 🔵 Fixes & Diagnostics
- **[flinkboot-core] Java 8 Date/Time Deserialization**: Bundled `jackson-datatype-jsr310` and registered `JavaTimeModule` in `YamlParser` to guarantee out-of-the-box support for `Duration` and Java 8 date/time types.

---

## [0.1.0-1.20]

### 🟢 Features & Enhancements
- **[flinkboot-core] Framework Inception**: Core startup environment, command-line arguments, environment variable resolution, YAML configuration loading, and multi-file merging.
- **[flinkboot-core] Declarative Environments**: Declarative `StreamExecutionEnvironment` creation (checkpointing, state backends, restart strategies, savepoints, local Web UI).
- **[flinkboot-kafka] Kafka Connector**: Kafka source and sink factory helpers (`flinkboot-kafka`).
- **[flinkboot-test] POJO Testing**: Basic POJO compliance assertions.
