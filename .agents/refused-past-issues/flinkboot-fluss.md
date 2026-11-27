# Refused Past Issues — `flinkboot-fluss`

Module-specific design trade-offs evaluated and rejected in `flinkboot-fluss`. Do not report findings matching these patterns unless you have concrete evidence of a runtime crash or broken contract.

## Duplicate `Objects.requireNonNull` Preconditions in `FlussSourceFactory` / `FlussSinkFactory`
- **Pattern**: Flagging `Objects.requireNonNull(...)` in `supplyFor(...)` as redundant because `supplyBuilderFor(...)` repeats the same null checks.
- **Status**: **REJECTED**
- **Rationale**: Public API entry points intentionally enforce fail-early `Objects.requireNonNull` preconditions at their boundary.

## Declarative Bounded / Stopping Offsets Support in `FlussSourceProperties`
- **Pattern**: Requesting `bounded` / `stopping-offsets` YAML properties in `FlussSourceProperties` to mirror `FlussSourceBuilder#setBounded` / `setStoppingOffsets`.
- **Status**: **REJECTED (Defer Until Needed)**
- **Rationale**: Not needed at this time; users who require bounded Fluss sources can configure the builder returned by `FlussSourceFactory.supplyBuilderFor(...)`.

## `Consumer<Builder>` Overloads on `FlussSourceFactory` / `FlussSinkFactory`
- **Pattern**: Adding `supplyFor(..., Consumer<Builder>)` overloads on `FlussSourceFactory` or `FlussSinkFactory`.
- **Status**: **REJECTED**
- **Rationale**: `supplyBuilderFor(...)` is already exposed for custom builder configuration before calling `.build()`, so `Consumer<Builder>` overloads would expand the public API surface unnecessarily.
