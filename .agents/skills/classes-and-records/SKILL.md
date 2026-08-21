---
name: java-classes-and-records
description: Best practices for creating general Java classes and records in Flinkboot.
---

# Forbidden practices (Does not apply to JPA entities as it's not real code)

- Never return null from a public method. Avoid null values as much as possible.
- Never use Lombok.
- Never use public setters.
- Never use ternary operators `? :` in getters.

# How to choose between Class and Record

- **Record**: Use a record if all values are final, immutable, and there is no need for specialized getter logic (e.g. returning `Optional` or defensive copies).
- **Class**: Use a class when custom getter logic, cross-field validation, or specialized optional wrapping is required.
- **Configuration DTOs**: For classes mapped to YAML/JSON configuration files, follow the dedicated `configuration-properties` skill.

**Follow the Examples**:
Review `examples/` to see valid examples:
- `examples/ClassDTO.java` (Class)
- `examples/RecordDTO.java` (Record)

## Best practices

### Prefer immutable objects

- Use `final` class declaration when inheritance is not required.
- Prefer `private final` fields.
- Use Optionals (`Optional<T>`, `OptionalLong`, `OptionalInt`) for methods that can return empty values.
- Always check for nullability on mandatory constructor arguments with `Objects.requireNonNull()`.
- Getters should never have "get" prefix (e.g. use `name()` instead of `getName()`).
- Always annotate generated boilerplate (`equals`, `hashCode`, `toString`) with `@Generated`.
