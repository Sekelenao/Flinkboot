# Changelog

All notable user-facing changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.4.0-1.20]

### Added

#### `flinkboot-core`
- **Environment Variable Placeholder Interpolation in YAML Configurations**:
  - Support for strict `${VARIABLE_NAME}` placeholder interpolation across all YAML properties, scalar fields, array elements, and nested objects.
  - Automatic case normalization (`${kafka-bootstrap-servers}` $\rightarrow$ `KAFKA_BOOTSTRAP_SERVERS`).
  - Support for escaping literal placeholders via backslash syntax (`\${placeholder}` $\rightarrow$ `${placeholder}`).
  - Strict fail-fast behavior throwing `UnresolvedPropertyPlaceholderException` if an environment variable is missing (no silent fallback).
  - Native single-pass merge and resolution during YAML tree loading.

#### `flinkboot-fluss`
- **New `flinkboot-fluss` Module for Apache Fluss**:
  - Full support for Apache Fluss (real-time sub-second streaming storage) Source and Sink connectors on Apache Flink 1.20.
  - **`FlussStartupMode`**: Declarative startup strategy supporting `EARLIEST`, `LATEST`, `FULL` (snapshot + continuous log), and `TIMESTAMP`.
  - **`FlussSourceProperties` & `FlussSinkProperties`**: Immutable, fail-fast configuration DTOs with Jakarta Bean Validation (`@NotBlank`, `@NotEmpty`, `@PositiveOrZero`).
  - **Fail-Fast Cross-Field Validation**: Strict runtime check requiring `startup-timestamp` when `startup-mode` is `TIMESTAMP`, and rejecting timestamps for other modes.
  - **`FlussSourceFactory` & `FlussSinkFactory`**: Factory utilities to build pre-configured Flink `FlussSource` and `FlussSink` instances with direct supply (`supplyFor`) and customizable builder (`supplyBuilderFor`) patterns.
  - Dedicated runtime exceptions: `InvalidFlussSourcePropertiesException` and `InvalidFlussSinkPropertiesException`.

#### `flinkboot-test`
- **Fluent Assertion API (`FlinkbootAssertions` & `ClassAssert`)**:
  - Introduced `io.github.sekelenao.flinkboot.test.api.assertion.FlinkbootAssertions` with fluent entry point `assertThat(Class<?> actual)`.
  - Added `ClassAssert.isPojo()` for recursive POJO compliance verification with zero Kryo fallback and method chaining support.

#### BOM & Dependencies
- Added `org.apache.flink:flink-runtime-web` (scope `provided`) to BOM `dependencyManagement` for local Web Dashboard debugging and version alignment.

#### Documentation
- **How-to Guides**:
  - Added `howto/fluss/configure-fluss-source.md` for configuring Fluss sources in YAML.
  - Added `howto/fluss/configure-fluss-sink.md` for configuring Fluss sinks with batch and timeout options.
  - Updated `howto/setup/avoid-dependency-conflicts.md` with recommended `maven-shade-plugin` exclusions (`module-info.class`, `META-INF/versions/**`).
  - Updated POJO validation guides to use `FlinkbootAssertions.assertThat(...).isPojo()`.
  - Corrected CLI argument syntax examples in `Flinkboot` Javadoc to reflect space-separated parameters and presence-only flags without inline `=`.


#### Contributor Tools & AI Skills
- **Standardized Developer Skills (`.agents/skills/`)**:
  - Added `configuration-properties`, `classes-and-records`, `test-classes`, and `project-architecture` guidelines for human and AI contributors.

### Changed

#### `flinkboot-core` & `flinkboot-fluss`
- **Native `java.time.Duration` Support in Configuration Properties DTOs**:
  - Replaced raw millisecond numerical fields with `java.time.Duration` across `CheckpointingProperties`, `ExecutionProperties`, `FixedDelayRestartProperties`, `FailureRateRestartProperties`, `ExponentialDelayRestartProperties`, and `FlussSinkProperties`.
  - Standardized YAML/JSON property keys by removing `-ms` suffixes (`interval`, `timeout`, `min-pause-between-checkpoints`, `aligned-checkpoint-timeout`, `buffer-timeout`, `auto-watermark-interval`, `delay`, `failure-interval`, `initial-backoff`, `max-backoff`, `reset-backoff-threshold`, `batch-timeout`), allowing expressive ISO-8601 duration strings (e.g. `"PT10S"`, `"PT1M"`, `"PT0.05S"`).
  - Customizers (`ExecutionCustomizer`, `CheckpointingCustomizer`, `RestartStrategyCustomizer`) and factories (`FlussSinkFactory`) supply `Duration` objects directly to Apache Flink `Configuration`.

#### `flinkboot-test`
- **Single Responsibility Separation**:
  - Extracted POJO validation assertions out of `FlinkbootTest` into `FlinkbootAssertions`, focusing `FlinkbootTest` exclusively on test configuration loading (`FlinkbootTest.configuration(...)`).


### Fixed

#### `flinkboot-core`
- **Multi-Line Validation Error Reporting & Configurable Buffer**:
  - Formats all Bean Validation errors as structured multi-line bullet lists with deterministic sorting.
  - Protects terminal and cluster logs with a default limit of 10 errors and explicit overflow summary (`- ... and X more violation(s)`).
  - Configurable violations log size via `-flinkboot-configuration-violations-log-size <number>` CLI parameter or `FLINKBOOT_CONFIGURATION_VIOLATIONS_LOG_SIZE` environment variable.

#### `flinkboot-test`
- **Flexible Zero-Kryo POJO Assertion**:
  - Enhanced `PojoValidator` to validate zero-Kryo safety across any compliant Flink type, supporting `@TypeInfo`-annotated root classes, custom factories, and composite types without rigid `PojoTypeInfo` constraints.


---

## [0.3.0-1.20]

### Changed

#### `flinkboot-core`
- **Default Configuration Location to Classpath (`classpath:job-configuration.yaml`)**:
  - Changed default configuration lookup location from `file:job-configuration.yaml` (local filesystem) to `classpath:job-configuration.yaml` (JAR classpath resources).
  - Enables self-contained Fat JARs / Shaded JARs to run out of the box on Flink clusters without requiring local filesystem mounts or explicit `-flinkboot-configurations` arguments.

---

## [0.2.0-1.20]

### Added

#### `flinkboot-core`
- **Unified Public Resource API (`Resource.of`)**:
  - Introduced public `Resource` interface in `io.github.sekelenao.flinkboot.core.api.resource` supporting `classpath:`, `resource:`, and `file:` URI schemes.
  - Fail-fast validation against empty/blank paths and malformed URI prefixes with dedicated runtime exceptions.
  - Multi-platform path normalization (Linux, macOS, and Windows drive letters / UNC shares) and stateless stream consumption.
- **Native JDK Type Serialization (`@TypeInfo` Factories)**:
  - `DurationTypeInfoFactory`: Efficient native serialization for `java.time.Duration` using an optimized 12-byte binary serializer (`DurationSerializer`).
  - `LocalDateTimeTypeInfoFactory`, `LocalDateTypeInfoFactory`, `LocalTimeTypeInfoFactory`: Native serializers for Java 8 date and time types.
  - `ListTypeInfoFactory<E>`, `MapTypeInfoFactory<K, V>`: Parameterized collection factories avoiding Kryo fallbacks for generic lists and maps.

#### `flinkboot-test`
- **Deep Recursive POJO Validation (`FlinkbootTest.assertPojo`)**:
  - Validates complex POJO structures recursively (nested POJOs, inheritance hierarchies, generic `List<T>`, `Map<K, V>`, object arrays `T[]`, tuples, and union types) to guarantee no fields fall back to Kryo serialization.
- **Test Configuration Helper (`FlinkbootTest.configuration`)**:
  - Easily load, merge, and validate YAML configurations in unit and integration tests using varargs (`classpath:` and `file:`).

#### `flinkboot-kafka`
- **Operator Naming**:
  - Added `name` configuration property to `KafkaSourceProperties` and `KafkaSinkProperties` for explicit operator naming in Flink execution graphs and metrics.

### Changed
- **JPMS Modular Reflection (`module-info.java`)**:
  - Opened all public API, properties, resource, and typing packages unconditionally across `flinkboot-core`, `flinkboot-kafka`, and `flinkboot-test` to support seamless runtime reflection, mocking, and serialization (Mockito, ByteBuddy, Spring, custom Jackson modules) in modular environments, while maintaining strict encapsulation of internal packages.

### Fixed

#### `flinkboot-core`
- **Native Java 8 Date/Time Deserialization in YAML Configuration (`JavaTimeModule`)**:
  - Bundled `jackson-datatype-jsr310` and explicitly registered `JavaTimeModule` in `YamlParser` to guarantee out-of-the-box support for `java.time.Duration`, `java.time.Instant`, `java.time.LocalDate`, etc., even when Jackson classes are relocated/shaded in fat JARs where SPI ServiceLoader auto-discovery fails.

---

## [0.1.0-1.20]

### Added
- Core startup environment, command-line arguments, and environment variable resolution.
- YAML configuration loading and multi-file merging.
- Declarative `StreamExecutionEnvironment` creation (checkpointing, state backends, restart strategies, savepoints, local Web UI).
- Kafka source and sink factory helpers (`flinkboot-kafka`).
- Basic POJO compliance assertions (`flinkboot-test`).
