---
name: java-nullability
description: "Enforce Java null safety across public return values, Optionals, collections, and constructor argument validation. Use when writing or reviewing Java classes, records, methods, constructors, or getters."
---

# Java Nullability

Follow these null-safety rules in Java code unless overridden by a framework, serialization contract, or external validation layer.

## Public Return Values

- Never return `null` from public methods in application or domain code, except where a serialization or framework contract requires raw nullable getters.
- Object Optionals (`Optional<T>`): return `Optional.ofNullable(field)` for nullable object fields.
- Primitive Optionals (`OptionalInt`, `OptionalLong`, `OptionalDouble`): check explicitly for `null` and return `.empty()`, otherwise `.of(field)`.
- Collections and Maps: never return `null`. Return an empty or unmodifiable collection/map (such as `Collections.emptyList()` / `Collections.unmodifiableList(...)` or `List.of()` / `List.copyOf(...)` matching the project's Java version and null-element needs).

## Fields and Parameters

- No `Optional` fields or parameters: reserve `Optional` strictly for return types. Store nullable state as plain fields and handle absence at method boundaries.
- Explicit nullability contracts: use the project's nullability annotations when available to document nullable fields and parameters.

## Constructor Arguments

- Validate mandatory arguments: enforce non-null invariants on required constructor arguments with `Objects.requireNonNull(arg, "arg must not be null")`.
- Framework and serialization exceptions: skip manual `Objects.requireNonNull(...)` checks in constructors managed by deserialization or external validation frameworks when that framework owns validation or requires no-arg/raw construction.
