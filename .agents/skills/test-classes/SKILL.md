---
name: test-classes
description: Permit to create test classes for new classes, interfaces, records, and configuration properties.
---

# Test Classes

A test class should be package private and defined in the same package as the class it tests (under `src/test/java`).
A test class should use JUnit 5 (Jupiter) and AssertJ / JUnit assertions.

## JUnit Best Practices

- Use `assertAll` when possible to test multiple conditions in a single assertion block.
- Use `@DisplayName` for all test classes, nested classes, and test methods with human-readable descriptions.
- Use `@Nested` for grouping related tests by class features/lifecycle.
- Test public API only, not private methods.
- Test edge cases (e.g. null values, empty lists, boundary limits, invalid combinations).
- Always import classes, interfaces, and static members (like `assertThat`, `assertThrows`, `assertAll`). Do not use fully qualified package/class names directly in test code.

## Testing Configuration Properties DTOs

When writing unit tests for `*Properties` classes, organize into structured `@Nested` classes:

1. **`@Nested @DisplayName("Constructor")`**:
   - Verify successful instantiation with valid arguments.
   - Verify `NullPointerException` on mandatory parameters via `Objects.requireNonNull`.
2. **`@Nested @DisplayName("Validation")`**:
   - Verify cross-field validation exceptions (e.g. invalid combinations throwing domain exceptions).
   - Verify Jakarta Bean Validation violations using `Validation.buildDefaultValidatorFactory().getValidator()`.
3. **`@Nested @DisplayName("Getters")`**:
   - Verify all field accessors return expected values.
   - Verify `Optional.empty()` / `OptionalLong.empty()` when fields are null vs `Optional.of(...)` when present.
   - Verify collections return unmodifiable defensive copies.
4. **`@Nested @DisplayName("Deserialization")`**:
   - Verify YAML/JSON parsing with Jackson `ObjectMapper` (full config, minimal config, default values).
5. **`@Nested @DisplayName("Equals and HashCode")`**:
   - Verify reflexive, symmetric equality and unequal cases for every single field.
   - Verify `hashCode()` consistency and `toString()` representation.

**Follow the Examples**:
Review `examples/` to see valid test class examples:
- `examples/ArrayListTest.java` (General class test example)