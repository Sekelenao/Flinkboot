# Changelog

All notable user-facing changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

---

## [0.1.0-1.20]

### Added
- Core startup environment, command-line arguments, and environment variable resolution.
- YAML configuration loading and multi-file merging.
- Declarative `StreamExecutionEnvironment` creation (checkpointing, state backends, restart strategies, savepoints, local Web UI).
- Kafka source and sink factory helpers (`flinkboot-kafka`).
- Basic POJO compliance assertions (`flinkboot-test`).
