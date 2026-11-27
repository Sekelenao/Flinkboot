# Refused Past Issues — `flinkboot-core`

Module-specific design trade-offs evaluated and rejected in `flinkboot-core`. Do not report findings matching these patterns unless you have concrete evidence of a runtime crash or broken contract.

## Empty String Fallback in Startup Environment
- **Pattern**: Silently falling back to default values when empty strings (`""`) are supplied via CLI options or environment variables in `StartupEnvironment`.
- **Status**: **REJECTED**
- **Rationale**: An explicitly provided empty value (`--opt ""` or `OPT=""`) must not silently fall back to a default, as that masks configuration or deployment mistakes.

## Mutable Configuration in Single-Use `ExecutionEnvironmentFactory`
- **Pattern**: Flagging instance `Configuration` or customizer lists in `ExecutionEnvironmentFactory` as a thread-safety or state accumulation hazard.
- **Status**: **REJECTED**
- **Rationale**: `ExecutionEnvironmentFactory` is instantiated fresh on every `Flinkboot.java` invocation and is never reused, stored statically, or shared across threads.

## Default Fallback Value Syntax in `PlaceholderResolver` (`${VAR:default}`)
- **Pattern**: Supporting `${VAR:default}` or `${VAR:-default}` in `PlaceholderResolver`.
- **Status**: **REJECTED**
- **Rationale**: Placeholders are restricted to strict `${VARIABLE_NAME}` lookups against environment variables. Fallback syntax introduces ambiguity with colons in values (`host:9092`) and risks masking missing configuration in production.

## Bootstrap ClassLoader Null Fallback in `ClasspathResource`
- **Pattern**: Requiring a `ClassLoader.getSystemClassLoader()` fallback when `ClasspathResource.class.getClassLoader()` returns `null`.
- **Status**: **REJECTED**
- **Rationale**: `Class.getClassLoader()` only returns `null` for bootstrap-loaded JDK classes. Application and Flinkboot classes are never loaded by the bootstrap classloader, making this fallback dead and untestable.

## Strict Flag/Value Lookahead & `--key=value` Rejection in `CommandLine`
- **Pattern**: Flagging `CommandLine.parse` for consuming `--flag` tokens as option values when a preceding value is omitted, or for treating `--key=value` as a boolean flag name.
- **Status**: **REJECTED**
- **Rationale**: `CommandLine` intentionally keeps a minimal `--key value` / `--flag` parsing contract without `--key=value` syntax or heuristic lookahead.

## Pre-Creation `Consumer<Configuration>` Overload on `Flinkboot.executionEnvironment(JobProperties)`
- **Pattern**: Requesting `Flinkboot.executionEnvironment(JobProperties, Consumer<Configuration>)` to mutate `Configuration` before `StreamExecutionEnvironment` creation.
- **Status**: **REJECTED**
- **Rationale**: Users can already configure `StreamExecutionEnvironment` post-creation via `StreamExecutionEnvironment#configure(ReadableConfig, ClassLoader)` or `env.getConfig()`.

## CLI Argument Resolution in `PlaceholderResolver` (`YamlParser`)
- **Pattern**: Wiring `StartupEnvironment::get` into `PlaceholderResolver` / `YamlParser` so `${VAR}` YAML placeholders can also be resolved from CLI arguments.
- **Status**: **REJECTED**
- **Rationale**: YAML placeholders often hold secrets (passwords, tokens, SASL credentials) that must not be exposed in CLI arguments (`ps`, `/proc/<pid>/cmdline`, job logs). `EnvVarResolver` also normalizes keys to `UPPER_SNAKE_CASE`, whereas `CommandLine` expects `kebab-case`.

## Relaxing `CheckpointingIntervalRule` for Cluster-Inherited `interval`
- **Pattern**: Allowing `checkpointing` sub-properties (`storage`, `storage-uri`) without `interval` so `execution.checkpointing.interval` can be inherited from `flink-conf.yaml`.
- **Status**: **REJECTED**
- **Rationale**: Flinkboot intentionally enforces explicit `interval` configuration whenever `checkpointing` is declared rather than partial cluster-config inheritance.

## Failing Fast on Empty `flinkboot-configurations` in `StartupEnvironment`
- **Pattern**: Throwing an exception in `StartupEnvironment.configurationResourceLocations()` when `flinkboot-configurations` is blank or comma-only and resolves to an empty list.
- **Status**: **REJECTED**
- **Rationale**: Bean Validation already fails fast when required fields are missing. When all fields are optional, an empty `flinkboot-configurations` intentionally allows relying on defaults without creating a dummy empty YAML file.

## Throwaway `StreamExecutionEnvironment` Check in `ExecutionEnvironmentProvider.get(boolean)`
- **Pattern**: Flagging `ExecutionEnvironmentProvider.get(true)` for calling `StreamExecutionEnvironment.getExecutionEnvironment()` solely to verify `!(defaultEnv instanceof LocalStreamEnvironment)`.
- **Status**: **REJECTED**
- **Rationale**: Flink's `contextEnvironmentFactory` is `private static`, so this startup-only call is the only public Flink API way to detect cluster submission vs. local execution.

## Tie-Breaking Order in `JakartaConfigurationValidator` Violation Messages
- **Pattern**: Adding secondary sorting (e.g. `.thenComparing(ConstraintViolation::getMessage)`) in `JakartaConfigurationValidator` to make multi-violation order on the same property path deterministic.
- **Status**: **REJECTED**
- **Rationale**: The formatted string is a human-readable startup error message, not a structured contract meant to be parsed or asserted on by exact ordering.

## Double Backslash Escaping Before Placeholders in `PlaceholderResolver` (`\\${VAR}`)
- **Pattern**: Treating `\\${VAR}` as an escaped backslash followed by a resolved placeholder.
- **Status**: **REJECTED**
- **Rationale**: `PlaceholderResolver` only supports single-backslash escaping (`\${literal}` -> `${literal}`) and does not implement general `\\` backslash escaping elsewhere in strings.

## Resolving `${...}` Placeholders in YAML Map / Object Keys
- **Pattern**: Resolving `${ENV_VAR}` placeholders in YAML object/map keys inside `PlaceholderResolver.resolveObjectNode`.
- **Status**: **REJECTED**
- **Rationale**: YAML keys represent static schema paths or driver property names; only scalar values and array elements are interpolated.

## Rejecting Unclosed `${...` Placeholders in `PlaceholderResolver`
- **Pattern**: Throwing `UnresolvedPropertyPlaceholderException` when a YAML string value contains an unclosed `${` without a matching `}`.
- **Status**: **REJECTED**
- **Rationale**: Unclosed `${...` sequences are intentionally treated as literal text to avoid breaking strings that happen to contain `${`.

## Adding `InstantTypeInfoFactory` for `java.time.Instant`
- **Pattern**: Adding `InstantTypeInfoFactory` returning `Types.INSTANT` under `api.typing.time`.
- **Status**: **REJECTED**
- **Rationale**: Flink's `BasicTypeInfo` already registers `java.time.Instant` natively (`BasicTypeInfo.INSTANT_TYPE_INFO`), so `Instant` POJO fields do not fall back to Kryo.

## Rejecting YAML `.inf` (`Double.POSITIVE_INFINITY`) on `ExponentialDelayRestartProperties.backoffMultiplier`
- **Pattern**: Adding a custom constraint or rule to reject `Double.POSITIVE_INFINITY` / `Double.isNaN` on `backoffMultiplier` because `@DecimalMin(value = "1.0", inclusive = false)` permits `+Infinity`.
- **Status**: **REJECTED**
- **Rationale**: Writing `.inf` in YAML for a restart backoff multiplier is a contrived edge case that does not warrant a custom Bean Validation constraint or rule (YAGNI).

## Jakarta EL Injection in `PropertiesValidator.compute` (`buildConstraintViolationWithTemplate`)
- **Pattern**: Flagging `PropertiesValidator.compute` for passing `PropertyViolation.message()` directly to `context.buildConstraintViolationWithTemplate(...)`, or recommending `ParameterMessageInterpolator`.
- **Status**: **REJECTED**
- **Rationale**: Since Hibernate Validator 6.2+ (Flinkboot uses 9.x), EL evaluation on custom constraint violations is disabled by default (`ExpressionLanguageFeatureLevel.NONE`). Switching to `ParameterMessageInterpolator` would also break built-in messages for `@DecimalMin(inclusive = false)` and `@DurationMin`.

## Rejecting Dormant `port` / `bind-address` When `LocalWebUiProperties.enabled` Is `false`
- **Pattern**: Adding a cross-field rule on `LocalWebUiProperties` to reject `port` or `bind-address` when `enabled` is `false`.
- **Status**: **REJECTED**
- **Rationale**: Toggle-based DTOs (`LocalWebUiProperties`, `CheckpointingProperties`) intentionally allow dormant sub-options when `enabled: false` so multi-file overrides (`base.yml` + `prod.yml`) work cleanly.

## Wrapping Missing `flink-runtime-web` Runtime Exceptions in `LocalWebUiExecutionEnvironmentProvider`
- **Pattern**: Catching Flink's `RuntimeException` from `createLocalEnvironmentWithWebUI` and message-matching to rethrow `UnsupportedExecutionEnvironmentException` when `flink-runtime-web` is absent.
- **Status**: **REJECTED**
- **Rationale**: Flink already throws an explicit exception when `flink-runtime-web` is missing; string-matching Flink's internal error message is brittle across Flink versions.

## Adding `readResolve()` to Stateless Flink SPI Singletons (`DurationSerializer`, `DurationTypeInfo`)
- **Pattern**: Requiring `private Object readResolve() { return INSTANCE; }` on `DurationSerializer` and `DurationTypeInfo`.
- **Status**: **REJECTED**
- **Rationale**: Both classes are stateless with value-based `equals`/`hashCode`, deserialization occurs once per task deployment, and Flink's own built-in serializers omit `readResolve()`.

## Disabling Environment-Variable-Enabled Boolean Flags via CLI in `StartupEnvironment`
- **Pattern**: Adding CLI negation syntax (`--no-<flag>` or `--flag=false`) to override a boolean flag enabled via environment variables.
- **Status**: **REJECTED**
- **Rationale**: `CommandLine` intentionally keeps a minimal presence-only `--flag` contract; adding negation syntax for a rare override scenario is unnecessary complexity (YAGNI).

## Removing Explicit `JavaTimeModule` Registration Alongside `findAndAddModules()` in `YamlParser`
- **Pattern**: Flagging `addModule(new JavaTimeModule())` followed by `findAndAddModules()` as redundant.
- **Status**: **REJECTED**
- **Rationale**: `addModule(new JavaTimeModule())` guarantees Java time support even when `ServiceLoader` discovery is restricted or shaded, while `findAndAddModules()` allows users to register extra Jackson modules from their classpath.
