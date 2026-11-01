---
name: nullability
description: Strict rules for null safety, constructor argument validation, Optionals, and collection returns in Flinkboot. Use when writing or reviewing Java classes, methods, constructors, or getters.
---

# Nullability

## 1. Public Return Values

- **Never Return `null`**: Never return `null` from a public method. Avoid `null` values as much as possible.
- **Object Optionals (`Optional<T>`)**: Use `Optional.ofNullable(field)` for nullable object fields.
- **Primitive Optionals (`OptionalLong`, `OptionalInt`)**: Use an explicit `if (field == null)` check returning `.empty()`, otherwise `.of(field)`.
- **Collections & Maps**: Never return `null`. Return `Collections.emptyList()` / `Collections.emptyMap()` when `null`, or `Collections.unmodifiableList(list)` / `Collections.unmodifiableMap(map)` otherwise.

## 2. Constructor Arguments

- **Mandatory Arguments**: Validate mandatory constructor arguments using `Objects.requireNonNull(arg, "arg must not be null")`.
  - *Exception*: Do NOT use `Objects.requireNonNull(...)` in Jackson `@JsonCreator` constructors of `*Properties` DTOs (Jakarta Bean Validation handles null checks across the object graph).

## 3. Reference Example

Review `examples/NullabilityExample.java` for a reference implementation.
