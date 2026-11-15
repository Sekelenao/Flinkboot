# Refused Past Issues & Intentional Design Choices

This document catalogs architectural and design decisions, edge-case trade-offs, and static-analysis findings that have been consciously evaluated and rejected by the Flinkboot maintainers.

All audit subagents (including `module-scanner` and `pr-reviewer`) must consult this document before flagging issues. Do not report findings matching these patterns unless you can provide concrete, novel evidence of an actual runtime crash or broken contract not accounted for by the recorded rationale.

---

## Global & Cross-Module

### 1. Single-Use Internal Processor State
- **Pattern**: Instance collections (`ArrayDeque`, `HashSet`) in internal processors (e.g. `MergeProcessor`, `PojoValidator`).
- **Status**: **REJECTED**
- **Rationale**: These internal machinery classes are instantiated fresh per invocation (e.g. `new MergeProcessor().merge()`, `new PojoValidator().validate()`). They are never shared across threads or reused across calls. Forcing them to be stateless by passing queues as method parameters adds ceremony and degrades readability with zero practical reliability gain.

### 3. Dogmatic `final` on Third-Party Framework SPI Implementations
- **Pattern**: Marking classes implementing Apache Flink SPIs (`TypeInformation`, `TypeSerializer`, or type factories) as `final`.
- **Status**: **REJECTED**
- **Rationale**: While internal self-contained classes without valid extension reasons must be declared `final` (per `classes-and-records` skill), framework integration classes implementing third-party SPIs must not be dogmatically locked, preserving downstream adaptability.

### 5. Defensive `.orElseThrow()` Custom Suppliers in Internal Connector Factories
- **Pattern**: Requiring descriptive exception suppliers for `.orElseThrow()` inside internal factories (e.g. `KafkaSourceFactory`, `FlussSourceFactory`).
- **Status**: **REJECTED**
- **Rationale**: Jakarta Bean Validation is Flinkboot's front-door barrier. If validation passes at startup, invalid or contradictory states never reach internal factories. Adding redundant defensive guards clutters internal machinery for states that are impossible during normal execution.

### 6. Replacing `Optional.ofNullable` with `if (val == null) return Optional.empty()`
- **Pattern**: Demanding verbose `if == null` branching instead of `Optional.ofNullable(field)`.
- **Status**: **BANNED**
- **Rationale**: `Optional.ofNullable(field)` is the clean, concise standard for object getters across Flinkboot. Explicit `if == null` is reserved exclusively for primitive wrappers (`OptionalLong` / `OptionalInt`) where `ofNullable` does not exist in standard Java.

### 8. Java Records in Java 11
- **Pattern**: Suggesting conversion of DTOs or carrier classes to Java `record`.
- **Status**: **REJECTED**
- **Rationale**: Flinkboot targets Java 11 (`<java.version>11</java.version>`). Java records are a Java 14+ feature finalized in Java 16 and do not compile under Java 11.

### 9. Null Preconditions in Cross-Field Validators (`if (mode == null) return true;`)
- **Pattern**: Flagging `return true;` when a prerequisite field is `null` in cross-field validators (`*PropertiesValidator`).
- **Status**: **REJECTED**
- **Rationale**: Cross-field validators check interdependencies between present fields. Missing fields are the exclusive responsibility of single-field `@NotNull` annotations. Returning `true` prevents duplicate errors and confusing cascaded messages.

### 10. Generic Properties Map Precedence (Escape Hatch Override)
- **Pattern**: Reordering connector factory setters or `ExecutionEnvironmentFactory` customizers to force typed fields over `properties: Map<String, String>`.
- **Status**: **REJECTED**
- **Rationale**: In Flinkboot, `properties:` is consciously designed as the universal escape hatch. Applying generic properties last allows advanced users to deliberately override lower-level driver or runtime settings if required by non-standard operational environments, without framework interference.

### 11. Boxed Return Types in DTO Getters
- **Pattern**: Demanding that getters return boxed wrapper types (`Integer`, `Long`, `Boolean`) instead of unboxed primitives or `OptionalInt` / `OptionalLong` to avoid auto-unboxing NPE risks.
- **Status**: **BANNED**
- **Rationale**: Flinkboot strictly forbids returning boxed object types (`Integer`, `Long`, `Boolean`) from public getters. Mandatory non-null properties must return primitive types (`int`, `long`, `boolean`). Optional properties must return `OptionalInt`, `OptionalLong`, or `Optional<T>`. Jakarta Bean Validation guarantees that mandatory properties are non-null before runtime consumers access the getters.

---

## flinkboot-core

### 2. Empty String Fallback in Startup Environment
- **Pattern**: Silently falling back to default values when empty strings (`""`) are supplied via CLI options or environment variables in `StartupEnvironment`.
- **Status**: **REJECTED**
- **Rationale**: An explicitly provided empty value (e.g. `--opt ""` or `OPT=""`) must not silently fall back to a default value. Silent fallback is deceptive and masks configuration or deployment mistakes.

### 13. Mutable Configuration in Single-Use ExecutionEnvironmentFactory
- **Pattern**: Flagging instance `Configuration` or customizer lists in `ExecutionEnvironmentFactory` as a thread-safety or state accumulation hazard.
- **Status**: **REJECTED**
- **Rationale**: `ExecutionEnvironmentFactory` is an internal machinery class instantiated fresh on every invocation (`new ExecutionEnvironmentFactory().create(jobProperties)` in `Flinkboot.java`). It is never reused across multiple invocations, stored in static state, or shared across threads. Forcing it to be stateless by passing configuration across internal methods adds ceremony with zero practical reliability gain, directly adhering to Rule 1 (*Single-Use Internal Processor State*).

### 14. Default Fallback Value Syntax in PlaceholderResolver (`${VAR:default}`)
- **Pattern**: Supporting default fallback value syntax `${VAR:default}` or `${VAR:-default}` in `PlaceholderResolver`.
- **Status**: **REJECTED**
- **Rationale**: `PlaceholderResolver` intentionally restricts placeholders to strict `${VARIABLE_NAME}` lookups against environment variables. Introducing fallback syntax adds parsing complexity, ambiguity around colons in values (such as Kafka endpoints or URLs `host:9092`), and risks masking missing configuration in production. Missing placeholders must fail fast.

### 16. Bootstrap ClassLoader Null Fallback in `ClasspathResource`
- **Pattern**: Requiring a `ClassLoader.getSystemClassLoader()` fallback in `ClasspathResource` when `ClasspathResource.class.getClassLoader()` returns `null`.
- **Status**: **REJECTED**
- **Rationale**: `Class.getClassLoader()` only returns `null` for classes loaded by the JVM bootstrap classloader (JDK classes or `-Xbootclasspath`). Application and Flinkboot library classes are never loaded by the bootstrap classloader, making this fallback dead/untestable defensive code.

### 17. Strict Flag/Value Lookahead & `--key=value` Rejection in `CommandLine`
- **Pattern**: Flagging `CommandLine.parse` for consuming `--flag` tokens as option values when a preceding option value is omitted, or for treating `--key=value` as a boolean flag name.
- **Status**: **REJECTED**
- **Rationale**: `CommandLine` intentionally keeps a minimal `--key value` / `--flag` parsing contract without `--key=value` syntax or heuristic lookahead rejecting `--` prefixes in option values.

### 18. Pre-Creation `Consumer<Configuration>` Overload on `Flinkboot.executionEnvironment(JobProperties)`
- **Pattern**: Requesting a `Flinkboot.executionEnvironment(JobProperties, Consumer<Configuration>)` overload to mutate `Configuration` before `StreamExecutionEnvironment` creation.
- **Status**: **REJECTED**
- **Rationale**: Users can already obtain and configure `StreamExecutionEnvironment` post-creation via `StreamExecutionEnvironment#configure(ReadableConfig, ClassLoader)` or `env.getConfig()`. Adding a `Consumer<Configuration>` overload expands the `Flinkboot` public API surface unnecessarily.

### 25. CLI Argument Resolution in `PlaceholderResolver` (`YamlParser`)
- **Pattern**: Wiring `StartupEnvironment::get` into `PlaceholderResolver` / `YamlParser` so `${VAR}` YAML placeholders can also be resolved from CLI arguments.
- **Status**: **REJECTED**
- **Rationale**: YAML placeholders are frequently used for secrets (passwords, tokens, SASL credentials); encouraging users to pass them as CLI arguments risks exposing secrets in `ps`, `/proc/<pid>/cmdline`, and job logs. Additionally, `EnvVarResolver` normalizes keys to `UPPER_SNAKE_CASE` for `System::getenv`, whereas `CommandLine` expects `kebab-case` options. Keeping `PlaceholderResolver` strictly backed by `System::getenv` is safer and cleaner.

### 26. Relaxing `CheckpointingIntervalRule` for Cluster-Inherited `interval`
- **Pattern**: Allowing `checkpointing` sub-properties (such as `storage` or `storage-uri`) to be configured without `interval` so `execution.checkpointing.interval` can be inherited from `flink-conf.yaml`.
- **Status**: **REJECTED**
- **Rationale**: Flinkboot intentionally enforces explicit configuration rather than implicit guessing or partial cluster-config inheritance for `checkpointing`. Requiring `interval` whenever `checkpointing` is configured is an intentional validation invariant.

### 28. Failing Fast on Empty `flinkboot-configurations` in `StartupEnvironment`
- **Pattern**: Throwing an exception in `StartupEnvironment.configurationResourceLocations()` when `flinkboot-configurations` is explicitly provided as blank or comma-only and resolves to an empty list.
- **Status**: **REJECTED**
- **Rationale**: When a configuration DTO has required fields, Jakarta Bean Validation already fails fast with `ConfigurationValidationException`. When all fields are optional, passing an empty `flinkboot-configurations` intentionally allows loading zero YAML files and relying on defaults without forcing users to create a dummy empty YAML file.

### 29. Throwaway `StreamExecutionEnvironment` Check in `ExecutionEnvironmentProvider.get(boolean)`
- **Pattern**: Flagging `ExecutionEnvironmentProvider.get(true)` for calling `StreamExecutionEnvironment.getExecutionEnvironment()` solely to verify `!(defaultEnv instanceof LocalStreamEnvironment)`.
- **Status**: **REJECTED**
- **Rationale**: Flink's `contextEnvironmentFactory` is `private static`, so calling `StreamExecutionEnvironment.getExecutionEnvironment()` is the only public Flink API way to detect whether the job is running in a cluster submission context vs. a local environment. This check runs once at startup.

### 30. Tie-Breaking Order in `JakartaConfigurationValidator` Violation Messages
- **Pattern**: Adding secondary sorting (e.g. `.thenComparing(ConstraintViolation::getMessage)`) in `JakartaConfigurationValidator` to make multi-violation order on the same property path deterministic.
- **Status**: **REJECTED**
- **Rationale**: The formatted string is a human-readable startup error message, not a structured contract meant to be parsed or asserted on by exact ordering.

### 36. Double Backslash Escaping Before Placeholders in `PlaceholderResolver` (`\\${VAR}`)
- **Pattern**: Treating `\\${VAR}` as an escaped backslash followed by a resolved placeholder in `PlaceholderResolver`.
- **Status**: **REJECTED**
- **Rationale**: `PlaceholderResolver` intentionally supports only single-backslash escaping of placeholder syntax (`\${literal}` -> `${literal}`) and does not implement general `\\` backslash escaping elsewhere in strings. Treating `\\` as an escaped backslash only before `${...}` would be inconsistent with the rest of string resolution.

### 37. Resolving `${...}` Placeholders in YAML Map / Object Keys
- **Pattern**: Resolving `${ENV_VAR}` placeholders in YAML object/map keys inside `PlaceholderResolver.resolveObjectNode`.
- **Status**: **REJECTED**
- **Rationale**: YAML keys represent configuration schema paths or driver property names that should remain static, explicit, and readable. Only scalar values and array elements are subject to environment variable interpolation.

---

## flinkboot-test

### 7. Blank Path Checking in Test Helpers (`FlinkbootTest.configuration`)
- **Pattern**: Rejecting blank paths (`""` or `"   "`) with defensive checks in `FlinkbootTest.configuration`.
- **Status**: **HISTORICAL / REJECTED** (Note: `FlinkbootTest` was removed in #200 in favor of direct `Flinkboot.initialize(String... args)`).
- **Rationale**: Passing empty strings in unit test resource paths is non-sensical; letting it fail downstream in core is completely acceptable.

### 12. Global Purge (`clearAll()`) in CollectingSink / CollectingSinkRegistry
- **Pattern**: Introducing a global `clearAll()` or `reset()` method in `CollectingSink` or `CollectingSinkRegistry` to wipe all sinks from static memory.
- **Status**: **REJECTED**
- **Rationale**: `CollectingSink` implements `AutoCloseable`, guaranteeing scoped cleanup via `try-with-resources` or `@AfterEach`. Each sink uses a unique `UUID` specifically to ensure complete data isolation during concurrent/parallel test runs (`junit.jupiter.execution.parallel.enabled=true`). A global static `clearAll()` would introduce cross-test interference, destroy test isolation in parallel suites, expand public API surface unnecessarily, and encourage sloppy lifecycle management.

### 15. Ordering & Redundant Assertions in `CollectingSinkAssert`
- **Pattern**: Requesting `containsExactly(...)`, `containsExactlyInAnyOrder(...)`, `doesNotContain(...)`, or `satisfies(Consumer<List<T>>)` on `CollectingSinkAssert` (or flagging `contains(T...)` for using a `HashSet` and ignoring order/duplicates).
- **Status**: **REJECTED**
- **Rationale**: In Apache Flink pipelines with `parallelism > 1`, records are emitted across parallel subtasks in non-deterministic order; encouraging strict order assertions like `containsExactly(...)` promotes flaky tests. `contains(T...)` intentionally checks set membership (`equals`). Negative and custom assertions are already covered by `noneMatch(...)` and direct access via `sink.elements()`.

### 22. Per-Branch Ancestor Tracking in `PojoValidator`
- **Pattern**: Replacing the global `visited` `Set<TypeInformation<?>>` in `PojoValidator` with per-branch ancestor tracking to report all distinct field paths sharing the same invalid `TypeInformation`.
- **Status**: **REJECTED**
- **Rationale**: Tracking only branch ancestors would re-traverse valid complex subtrees repeatedly whenever a POJO has multiple fields of the same type, degrading performance unless full subtree result caching is added. Keeping the global `visited` set already reports every distinct `TypeInformation` violation in one test run and is the right simplicity/performance trade-off.

### 23. Custom `ClassLoader` Resolution in `SerializationValidator`
- **Pattern**: Subclassing `ObjectInputStream` in `SerializationValidator` to resolve classes via `actual.getClass().getClassLoader()` instead of the default `latestUserDefinedLoader()`.
- **Status**: **REJECTED (Defer Until Needed)**
- **Rationale**: Standard JUnit tests run under the same application classloader, where `ObjectInputStream` resolves test classes normally. Adding custom `ObjectInputStream` subclassing is unnecessary complexity for a scenario unlikely to occur; if a user ever reports a `ClassNotFoundException` under an isolated classloader, it can be addressed then.

### 24. Supporting `null` Elements in `CollectingSinkRegistry`
- **Pattern**: Allowing `null` records in `CollectingSinkRegistry.append(UUID, Object)` and `CollectingSinkRegistry.elements(UUID)`.
- **Status**: **REJECTED**
- **Rationale**: Apache Flink's `DataStream` runtime and native serializers generally forbid `null` records during serialization and shuffling. Rejecting `null` via `Objects.requireNonNull` in `CollectingSinkRegistry` aligns with Flink's non-null record contract and Flinkboot's strict nullability rules.

### 27. `RowTypeInfo` Field Name Formatting in `CompositeTypeHandler`
- **Pattern**: Special-casing `RowTypeInfo` in `CompositeTypeHandler` to format child validation paths using `getFieldNames()` (e.g. `task.path() + "." + fieldNames[i]`) instead of `[i]`.
- **Status**: **REJECTED**
- **Rationale**: `ClassAssert` uses `TypeExtractor.createTypeInfo`, which never produces `RowTypeInfo` (a `Row` field becomes `GenericTypeInfo<Row>`), so this only affects explicit `TypeInformationAssert` calls with named `RowTypeInfo`. Unnamed `RowTypeInfo` falls back to generic `f0`, `f1` names, and adding `instanceof RowTypeInfo` couples a generic `CompositeType<?>` handler to a concrete subclass for a purely cosmetic path change.

### 35. Deferring `TypeInformation.of(TypeHint)` Resolution to `isPojo()` in `TypeInformationAssert` (#402)
- **Pattern**: Storing `TypeHint<T>` lazily in `TypeInformationAssert(TypeHint<T>)` and deferring `TypeInformation.of(typeHint)` to `isPojo()` to catch `FlinkRuntimeException` / `InvalidTypesException` and rethrow `AssertionFailedError`.
- **Status**: **REJECTED**
- **Rationale**: In Apache Flink, `TypeHint()` already calls `TypeExtractor.createTypeInfo(...)` in its own constructor and throws `FlinkRuntimeException` before `TypeInformationAssert(TypeHint)` is ever invoked. `TypeInformation.of(typeHint)` merely returns `typeHint.getTypeInfo()`, so deferring it to `isPojo()` would not catch unresolvable anonymous `TypeHint` instances in practice—it would only add nullable field complexity and a `try/catch` for custom `TypeHint` subclasses overriding `getTypeInfo()`.

---

## flinkboot-kafka

### 4. Configuration-Level Regex Compilation & Partition Uniqueness
- **Pattern**: Enforcing `Pattern.compile()` syntax checks or duplicate `(topic, partition)` checks inside `KafkaSourcePropertiesValidator`.
- **Status**: **REJECTED**
- **Rationale**: This is redundant over-validation. Flink and client libraries natively validate and compile regular expressions and handle partition assignments upon builder initialization.

### 19. Making `KafkaSinkProperties.topic` Optional for `TopicSelector`
- **Pattern**: Making `topic` optional in `KafkaSinkProperties` so `KafkaSinkFactory.supplyBuilderFor(KafkaSinkProperties, Consumer<KafkaRecordSerializationSchemaBuilder<T>>)` can call `setTopicSelector(...)` without conflicting with `setTopic(config.topic())`.
- **Status**: **REJECTED**
- **Rationale**: In the vast majority of pipelines, a Kafka sink writes to a single static topic; keeping `topic` `@NotBlank` preserves strict startup validation for YAML configurations. Advanced users who need dynamic multi-topic routing can already supply a custom `KafkaRecordSerializationSchema<T>` via `KafkaSinkFactory.supplyBuilderFor(KafkaSinkProperties, KafkaRecordSerializationSchema<T>)` or use Flink's `KafkaSink.builder()` directly.

### 20. Distinguishing Explicit Empty `partitions: []` from `null` in `KafkaOffsetPropertiesValidator`
- **Pattern**: Rejecting `partitions: []` when `strategy` is not `OFFSETS` in `KafkaOffsetPropertiesValidator`.
- **Status**: **REJECTED**
- **Rationale**: Across Flinkboot DTOs, `null` and empty lists (`[]`) are intentionally normalized to `Collections.emptyList()`. Distinguishing between `null` and `[]` solely to reject `partitions: []` adds complexity without practical production value.

### 21. Duplicate `(topic, partition)` Check in `KafkaSourceFactory`
- **Pattern**: Failing fast on duplicate `(topic, partition)` entries in `KafkaSourceFactory.supplyBuilderFor` when building `Set<TopicPartition>`.
- **Status**: **REJECTED**
- **Rationale**: Unlike `OffsetInitializerMapper` (which builds a `Map<TopicPartition, Long>` where duplicate keys with potentially conflicting offset values are ambiguous and cause `Collectors.toMap` to fail), `KafkaSourceFactory` builds a value-less `Set<TopicPartition>`. Duplicate entries are harmlessly deduplicated by `Collectors.toSet()`, and validation of partition uniqueness in `KafkaSourcePropertiesValidator` is already rejected under Rule 4.

### 31. Rejecting `KafkaOffsetInitializer.EARLIEST` in `stopping-offsets`
- **Pattern**: Adding a cross-field validation rule to reject `stopping-offsets.strategy: EARLIEST` in `KafkaSourcePropertiesValidator` / `KafkaBoundednessRule`.
- **Status**: **REJECTED**
- **Rationale**: Flink's `KafkaSourceBuilder.setBounded(OffsetsInitializer)` natively permits `OffsetsInitializer.earliest()`. Avoiding unnecessary cross-field over-validation keeps Flinkboot aligned with Flink's own API contract.

---

## flinkboot-fluss

### 32. Duplicate `Objects.requireNonNull` Preconditions in `FlussSourceFactory` / `FlussSinkFactory`
- **Pattern**: Flagging `Objects.requireNonNull(...)` in `supplyFor(...)` as redundant because `supplyBuilderFor(...)` repeats the same null checks.
- **Status**: **REJECTED**
- **Rationale**: Public API entry points intentionally enforce fail-early `Objects.requireNonNull` preconditions at their boundary.

### 33. Declarative Bounded / Stopping Offsets Support in `FlussSourceProperties`
- **Pattern**: Requesting `bounded` / `stopping-offsets` YAML properties in `FlussSourceProperties` to mirror `FlussSourceBuilder#setBounded` / `setStoppingOffsets`.
- **Status**: **REJECTED (Defer Until Needed)**
- **Rationale**: Not needed at this time; users who require bounded Fluss sources can already configure the builder returned by `FlussSourceFactory.supplyBuilderFor(...)`.

### 34. `Consumer<Builder>` Overloads on `FlussSourceFactory` / `FlussSinkFactory`
- **Pattern**: Adding `supplyFor(..., Consumer<Builder>)` overloads on `FlussSourceFactory` or `FlussSinkFactory`.
- **Status**: **REJECTED**
- **Rationale**: `supplyBuilderFor(...)` is already exposed for custom builder configuration before calling `.build()`. Adding `Consumer<Builder>` overloads expands the public API surface unnecessarily.
