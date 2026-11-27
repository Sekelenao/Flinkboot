# Refused Past Issues — `flinkboot-test`

Module-specific design trade-offs evaluated and rejected in `flinkboot-test`. Do not report findings matching these patterns unless you have concrete evidence of a runtime crash or broken contract.

## Global Purge (`clearAll()`) in `CollectingSink` / `CollectingSinkRegistry`
- **Pattern**: Introducing a global `clearAll()` or `reset()` method to wipe all sinks from static memory.
- **Status**: **REJECTED**
- **Rationale**: `CollectingSink` implements `AutoCloseable` for scoped cleanup, and each sink uses a unique `UUID` for isolation under parallel JUnit execution (`junit.jupiter.execution.parallel.enabled=true`). A global `clearAll()` would cause cross-test interference and expand the public API unnecessarily.

## Ordering & Redundant Assertions in `CollectingSinkAssert`
- **Pattern**: Requesting `containsExactly(...)`, `containsExactlyInAnyOrder(...)`, `doesNotContain(...)`, or `satisfies(Consumer<List<T>>)` on `CollectingSinkAssert` (or flagging `contains(T...)` for using a `HashSet`).
- **Status**: **REJECTED**
- **Rationale**: In Flink pipelines with `parallelism > 1`, parallel subtasks emit records in non-deterministic order; strict order assertions promote flaky tests. `contains(T...)` intentionally checks set membership, while negative and custom assertions are covered by `noneMatch(...)` and `sink.elements()`.

## Per-Branch Ancestor Tracking in `PojoValidator`
- **Pattern**: Replacing the global `visited` `Set<TypeInformation<?>>` in `PojoValidator` with per-branch ancestor tracking to report all distinct field paths sharing the same invalid `TypeInformation`.
- **Status**: **REJECTED**
- **Rationale**: Per-branch tracking would re-traverse valid complex subtrees repeatedly whenever a POJO has multiple fields of the same type. Keeping the global `visited` set already reports every distinct `TypeInformation` violation in one test run.

## Custom `ClassLoader` Resolution in `SerializationValidator`
- **Pattern**: Subclassing `ObjectInputStream` in `SerializationValidator` to resolve classes via `actual.getClass().getClassLoader()` instead of `latestUserDefinedLoader()`.
- **Status**: **REJECTED (Defer Until Needed)**
- **Rationale**: Standard JUnit tests run under the application classloader where `ObjectInputStream` resolves classes normally. Custom `ObjectInputStream` subclassing can be added if an isolated-classloader issue is ever reported.

## Supporting `null` Elements in `CollectingSinkRegistry`
- **Pattern**: Allowing `null` records in `CollectingSinkRegistry.append(UUID, Object)` and `CollectingSinkRegistry.elements(UUID)`.
- **Status**: **REJECTED**
- **Rationale**: Flink's `DataStream` runtime and native serializers generally forbid `null` records during serialization and shuffling. Rejecting `null` via `Objects.requireNonNull` aligns with Flink's contract and Flinkboot's nullability rules.

## `RowTypeInfo` Field Name Formatting in `CompositeTypeHandler`
- **Pattern**: Special-casing `RowTypeInfo` in `CompositeTypeHandler` to format child paths using `getFieldNames()` instead of `[i]`.
- **Status**: **REJECTED**
- **Rationale**: `TypeExtractor.createTypeInfo` never produces `RowTypeInfo` (`Row` becomes `GenericTypeInfo<Row>`), unnamed `RowTypeInfo` falls back to `f0`, `f1`, and `instanceof RowTypeInfo` couples a generic `CompositeType<?>` handler to a concrete subclass for a cosmetic path change.

## Deferring `TypeInformation.of(TypeHint)` Resolution to `isPojo()` in `TypeInformationAssert`
- **Pattern**: Storing `TypeHint<T>` lazily in `TypeInformationAssert(TypeHint<T>)` and deferring `TypeInformation.of(typeHint)` to `isPojo()` to catch `FlinkRuntimeException` / `InvalidTypesException` and rethrow `AssertionFailedError`.
- **Status**: **REJECTED**
- **Rationale**: Flink's `TypeHint()` constructor already calls `TypeExtractor.createTypeInfo(...)` before `TypeInformationAssert` is invoked, and `TypeInformation.of(typeHint)` merely returns `typeHint.getTypeInfo()`.

## Diagnostic Message Collection Order & Deduplication in `CollectingSinkAssert`
- **Pattern**: Replacing `Collectors.toSet()` with `Collectors.toList()` or `LinkedHashSet` in `CollectingSinkAssert` (`contains`, `allMatch`, `noneMatch`) to preserve duplicates or deterministic order in failure messages.
- **Status**: **REJECTED**
- **Rationale**: Failure messages in `CollectingSinkAssert` are human-readable diagnostics for test debugging, not a structured contract requiring strict ordering or multiplicity preservation.
