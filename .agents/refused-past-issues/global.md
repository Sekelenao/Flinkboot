# Refused Past Issues — Global & Cross-Module

Architectural and design trade-offs evaluated and rejected across Flinkboot. Do not report findings matching these patterns unless you have concrete evidence of a runtime crash or broken contract.

## Single-Use Internal Processor State
- **Pattern**: Instance collections (`ArrayDeque`, `HashSet`) in internal processors (e.g. `MergeProcessor`, `PojoValidator`).
- **Status**: **REJECTED**
- **Rationale**: These internal classes are instantiated fresh per invocation and never shared across threads or reused. Forcing them to be stateless by passing collections through method parameters adds ceremony without improving reliability.

## Dogmatic `final` on Third-Party Framework SPI Implementations
- **Pattern**: Marking classes implementing Apache Flink SPIs (`TypeInformation`, `TypeSerializer`, or type factories) as `final`.
- **Status**: **REJECTED**
- **Rationale**: While self-contained internal classes without extension reasons must be `final`, framework integration classes implementing third-party SPIs must remain extensible for downstream adaptability.

## Defensive `.orElseThrow()` Custom Suppliers in Internal Connector Factories
- **Pattern**: Requiring descriptive exception suppliers for `.orElseThrow()` inside internal factories (e.g. `KafkaSourceFactory`, `FlussSourceFactory`).
- **Status**: **REJECTED**
- **Rationale**: Jakarta Bean Validation is Flinkboot's front-door barrier; invalid or contradictory states never reach internal factories once validation passes.

## Replacing `Optional.ofNullable` with `if (val == null) return Optional.empty()`
- **Pattern**: Demanding verbose `if == null` branching instead of `Optional.ofNullable(field)`.
- **Status**: **BANNED**
- **Rationale**: `Optional.ofNullable(field)` is the standard for object getters. Explicit `if == null` is reserved for primitive wrappers (`OptionalLong` / `OptionalInt`) where `ofNullable` does not exist.

## Java Records in Java 11
- **Pattern**: Suggesting conversion of DTOs or carrier classes to Java `record`.
- **Status**: **REJECTED**
- **Rationale**: Flinkboot targets Java 11 (`<java.version>11</java.version>`), where records do not compile.

## Null Preconditions in Cross-Field Validators (`if (mode == null) return true;`)
- **Pattern**: Flagging `return true;` when a prerequisite field is `null` in cross-field validators (`*PropertiesValidator`).
- **Status**: **REJECTED**
- **Rationale**: Cross-field validators check interdependencies between present fields; missing fields are handled by single-field `@NotNull` annotations. Returning `true` prevents duplicate errors.

## Generic Properties Map Precedence (Escape Hatch Override)
- **Pattern**: Reordering connector factory setters or `ExecutionEnvironmentFactory` customizers to force typed fields over `properties: Map<String, String>`.
- **Status**: **REJECTED**
- **Rationale**: `properties:` is intentionally designed as an escape hatch. Applying generic properties last allows advanced users to override lower-level driver or runtime settings when needed.

## Boxed Return Types in DTO Getters
- **Pattern**: Demanding boxed wrapper return types (`Integer`, `Long`, `Boolean`) instead of primitives or `OptionalInt` / `OptionalLong`.
- **Status**: **BANNED**
- **Rationale**: Mandatory non-null properties must return primitive types (`int`, `long`, `boolean`), and optional properties must return `OptionalInt`, `OptionalLong`, or `Optional<T>`. Bean Validation guarantees mandatory properties are non-null before getters are called.
