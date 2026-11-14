# Refused Past Issues & Intentional Design Choices

This document catalogs architectural and design decisions, edge-case trade-offs, and static-analysis findings that have been consciously evaluated and rejected by the Flinkboot maintainers.

All audit subagents (including `module-scanner` and `pr-reviewer`) must consult this document before flagging issues. Do not report findings matching these patterns unless you can provide concrete, novel evidence of an actual runtime crash or broken contract not accounted for by the recorded rationale.

---

## 1. Single-Use Internal Processor State
- **Pattern**: Instance collections (`ArrayDeque`, `HashSet`) in internal processors (e.g. `MergeProcessor`, `PojoValidator`).
- **Status**: **REJECTED**
- **Rationale**: These internal machinery classes are instantiated fresh per invocation (e.g. `new MergeProcessor().merge()`, `new PojoValidator().validate()`). They are never shared across threads or reused across calls. Forcing them to be stateless by passing queues as method parameters adds ceremony and degrades readability with zero practical reliability gain.

## 2. Empty String Fallback in Startup Environment
- **Pattern**: Silently falling back to default values when empty strings (`""`) are supplied via CLI options or environment variables in `StartupEnvironment`.
- **Status**: **REJECTED**
- **Rationale**: An explicitly provided empty value (e.g. `--opt ""` or `OPT=""`) must not silently fall back to a default value. Silent fallback is deceptive and masks configuration or deployment mistakes.

## 3. Dogmatic `final` on Third-Party Framework SPI Implementations
- **Pattern**: Marking classes implementing Apache Flink SPIs (`TypeInformation`, `TypeSerializer`, or type factories) as `final`.
- **Status**: **REJECTED**
- **Rationale**: While internal self-contained classes without valid extension reasons must be declared `final` (per `classes-and-records` skill), framework integration classes implementing third-party SPIs must not be dogmatically locked, preserving downstream adaptability.

## 4. Configuration-Level Regex Compilation & Partition Uniqueness
- **Pattern**: Enforcing `Pattern.compile()` syntax checks or duplicate `(topic, partition)` checks inside `KafkaSourcePropertiesValidator`.
- **Status**: **REJECTED**
- **Rationale**: This is redundant over-validation. Flink and client libraries natively validate and compile regular expressions and handle partition assignments upon builder initialization.

## 5. Defensive `.orElseThrow()` Custom Suppliers in Internal Connector Factories
- **Pattern**: Requiring descriptive exception suppliers for `.orElseThrow()` inside internal factories (e.g. `KafkaSourceFactory`, `FlussSourceFactory`).
- **Status**: **REJECTED**
- **Rationale**: Jakarta Bean Validation is Flinkboot's front-door barrier. If validation passes at startup, invalid or contradictory states never reach internal factories. Adding redundant defensive guards clutters internal machinery for states that are impossible during normal execution.

## 6. Replacing `Optional.ofNullable` with `if (val == null) return Optional.empty()`
- **Pattern**: Demanding verbose `if == null` branching instead of `Optional.ofNullable(field)`.
- **Status**: **BANNED**
- **Rationale**: `Optional.ofNullable(field)` is the clean, concise standard for object getters across Flinkboot. Explicit `if == null` is reserved exclusively for primitive wrappers (`OptionalLong` / `OptionalInt`) where `ofNullable` does not exist in standard Java.

## 7. Blank Path Checking in Test Helpers (`FlinkbootTest.configuration`)
- **Pattern**: Rejecting blank paths (`""` or `"   "`) with defensive checks in `FlinkbootTest.configuration`.
- **Status**: **HISTORICAL / REJECTED** (Note: `FlinkbootTest` was removed in #200 in favor of direct `Flinkboot.initialize(String... args)`).
- **Rationale**: Passing empty strings in unit test resource paths is non-sensical; letting it fail downstream in core is completely acceptable.

## 8. Java Records in Java 11
- **Pattern**: Suggesting conversion of DTOs or carrier classes to Java `record`.
- **Status**: **REJECTED**
- **Rationale**: Flinkboot targets Java 11 (`<java.version>11</java.version>`). Java records are a Java 14+ feature finalized in Java 16 and do not compile under Java 11.

## 9. Null Preconditions in Cross-Field Validators (`if (mode == null) return true;`)
- **Pattern**: Flagging `return true;` when a prerequisite field is `null` in cross-field validators (`*PropertiesValidator`).
- **Status**: **REJECTED**
- **Rationale**: Cross-field validators check interdependencies between present fields. Missing fields are the exclusive responsibility of single-field `@NotNull` annotations. Returning `true` prevents duplicate errors and confusing cascaded messages.

## 10. Generic Properties Map Precedence (Escape Hatch Override)
- **Pattern**: Reordering connector factory setters or `ExecutionEnvironmentFactory` customizers to force typed fields over `properties: Map<String, String>`.
- **Status**: **REJECTED**
- **Rationale**: In Flinkboot, `properties:` is consciously designed as the universal escape hatch. Applying generic properties last allows advanced users to deliberately override lower-level driver or runtime settings if required by non-standard operational environments, without framework interference.

## 11. Boxed Return Types in DTO Getters
- **Pattern**: Demanding that getters return boxed wrapper types (`Integer`, `Long`, `Boolean`) instead of unboxed primitives or `OptionalInt` / `OptionalLong` to avoid auto-unboxing NPE risks.
- **Status**: **BANNED**
- **Rationale**: Flinkboot strictly forbids returning boxed object types (`Integer`, `Long`, `Boolean`) from public getters. Mandatory non-null properties must return primitive types (`int`, `long`, `boolean`). Optional properties must return `OptionalInt`, `OptionalLong`, or `Optional<T>`. Jakarta Bean Validation guarantees that mandatory properties are non-null before runtime consumers access the getters.

## 12. Global Purge (`clearAll()`) in CollectingSink / CollectingSinkRegistry
- **Pattern**: Introducing a global `clearAll()` or `reset()` method in `CollectingSink` or `CollectingSinkRegistry` to wipe all sinks from static memory.
- **Status**: **REJECTED**
- **Rationale**: `CollectingSink` implements `AutoCloseable`, guaranteeing scoped cleanup via `try-with-resources` or `@AfterEach`. Each sink uses a unique `UUID` specifically to ensure complete data isolation during concurrent/parallel test runs (`junit.jupiter.execution.parallel.enabled=true`). A global static `clearAll()` would introduce cross-test interference, destroy test isolation in parallel suites, expand public API surface unnecessarily, and encourage sloppy lifecycle management.

## 13. Mutable Configuration in Single-Use ExecutionEnvironmentFactory
- **Pattern**: Flagging instance `Configuration` or customizer lists in `ExecutionEnvironmentFactory` as a thread-safety or state accumulation hazard.
- **Status**: **REJECTED**
- **Rationale**: `ExecutionEnvironmentFactory` is an internal machinery class instantiated fresh on every invocation (`new ExecutionEnvironmentFactory().create(jobProperties)` in `Flinkboot.java`). It is never reused across multiple invocations, stored in static state, or shared across threads. Forcing it to be stateless by passing configuration across internal methods adds ceremony with zero practical reliability gain, directly adhering to Rule 1 (*Single-Use Internal Processor State*).

## 14. Default Fallback Value Syntax in PlaceholderResolver (`${VAR:default}`)
- **Pattern**: Supporting default fallback value syntax `${VAR:default}` or `${VAR:-default}` in `PlaceholderResolver`.
- **Status**: **REJECTED**
- **Rationale**: `PlaceholderResolver` intentionally restricts placeholders to strict `${VARIABLE_NAME}` lookups against environment variables or startup arguments. Introducing fallback syntax adds parsing complexity, ambiguity around colons in values (such as Kafka endpoints or URLs `host:9092`), and risks masking missing configuration in production. Missing placeholders must fail fast.

## 15. Ordering & Redundant Assertions in `CollectingSinkAssert`
- **Pattern**: Requesting `containsExactly(...)`, `doesNotContain(...)`, or `satisfies(Consumer<List<T>>)` on `CollectingSinkAssert` (or flagging `contains(T...)` for using a `HashSet` and ignoring order/duplicates).
- **Status**: **REJECTED**
- **Rationale**: In Apache Flink pipelines with `parallelism > 1`, records are emitted across parallel subtasks in non-deterministic order; encouraging strict order assertions like `containsExactly(...)` promotes flaky tests. `contains(T...)` intentionally checks set membership (`equals`). Negative and custom assertions are already covered by `noneMatch(...)` and direct access via `sink.elements()`.

## 16. Bootstrap ClassLoader Null Fallback in `ClasspathResource`
- **Pattern**: Requiring a `ClassLoader.getSystemClassLoader()` fallback in `ClasspathResource` when `ClasspathResource.class.getClassLoader()` returns `null`.
- **Status**: **REJECTED**
- **Rationale**: `Class.getClassLoader()` only returns `null` for classes loaded by the JVM bootstrap classloader (JDK classes or `-Xbootclasspath`). Application and Flinkboot library classes are never loaded by the bootstrap classloader, making this fallback dead/untestable defensive code.

## 17. Strict Flag/Value Lookahead & `--key=value` Rejection in `CommandLine`
- **Pattern**: Flagging `CommandLine.parse` for consuming `--flag` tokens as option values when a preceding option value is omitted, or for treating `--key=value` as a boolean flag name.
- **Status**: **REJECTED**
- **Rationale**: `CommandLine` intentionally keeps a minimal `--key value` / `--flag` parsing contract without `--key=value` syntax or heuristic lookahead rejecting `--` prefixes in option values.

## 18. Pre-Creation `Consumer<Configuration>` Overload on `Flinkboot.executionEnvironment(JobProperties)`
- **Pattern**: Requesting a `Flinkboot.executionEnvironment(JobProperties, Consumer<Configuration>)` overload to mutate `Configuration` before `StreamExecutionEnvironment` creation.
- **Status**: **REJECTED**
- **Rationale**: Users can already obtain and configure `StreamExecutionEnvironment` post-creation via `StreamExecutionEnvironment#configure(ReadableConfig, ClassLoader)` or `env.getConfig()`. Adding a `Consumer<Configuration>` overload expands the `Flinkboot` public API surface unnecessarily.

