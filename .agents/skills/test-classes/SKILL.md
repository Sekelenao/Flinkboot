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
- Use `@Nested` for grouping related tests by class features/lifecycle. Nested class names must NOT include the `Test` suffix (e.g. `@Nested @DisplayName("Validation") class Validation`, not `class ValidationTest`).
- Test public API only, not private methods. Always test the real public constructor/entry point in addition to any `@VisibleForTesting` constructors.
- Utility class constructors: For static utility classes with a private constructor throwing an exception (e.g. `AssertionError`), testing via reflection to verify it throws an `AssertionError` is sufficient. Do not assert the exception message string.
- Test edge cases (e.g. null values, empty lists, boundary limits, invalid combinations).
- Always import classes, interfaces, and static members (like `assertThat`, `assertThrows`, `assertAll`). Do not use fully qualified package/class names directly in test code.

### Parameterized Tests & Pragmatic Consolidation (The Goldilocks Rule)
- Consolidate repetitive test methods testing the exact same invariant with different inputs using `@ParameterizedTest` with `@ValueSource`, `@CsvSource`, or `@NullAndEmptySource` (e.g. invalid boundaries `-1`, `0`, or corrupted strings `"invalid"`, `"12.5"`, `"   "`).
- **Separation of Distinct Business Concepts (Anti-CsvSource Bloat)**:
  - Never merge distinct business concepts, intentions, or outcomes into a single generic parameterized test via `@CsvSource` merely for the sake of code compaction (e.g. conflating `true` validation and `false` validation in an artificial `"true, true", "false, false"` CSV table).
  - Each distinct business idea deserves its own dedicated test method: one for `true` representations with `@ValueSource` and `assertTrue(...)`, another for `false` representations with `@ValueSource` and `assertFalse(...)`.
  - Forcing distinct semantic paths into a multi-column `@CsvSource` destroys the test's role as living documentation, introduces indirection, and weakens diagnostic readability.
  - Reserve `@CsvSource` exclusively for cases where inputs and expected outputs naturally express a single, multi-variable business formula belonging to the exact same behavioral invariant.
- **Avoid Over-Engineering**: Never write "mega-parameterized tests" containing conditional logic (`if (shouldFail) ...`), excessive arguments, or heterogeneous assertions. A parameterized test must verify ONE single invariant at a glance.

### Mutation Testing & Invariant Sensitivity
- Design assertions that strictly fail if a production condition is inverted, swapped, or omitted.
- Avoid symmetry blindspots: when testing multiple flags or options, test asymmetric combinations (e.g. `flagA=true` with `flagB=false`), not only lockstep combinations (`true/true` and `false/false`).
- For any collection returned by getters or methods, explicitly verify immutability by asserting `assertThrows(UnsupportedOperationException.class, () -> collection.add(...))`.

## Testing Configuration Properties DTOs

When writing unit tests for `*Properties` classes, organize into structured `@Nested` classes:

1. **`@Nested @DisplayName("Constructor")`**:
   - Verify successful instantiation with valid arguments.
2. **`@Nested @DisplayName("Validation")`**:
   - Verify cross-field invariants via Jakarta Bean Validation (classes implementing `ValidatableProperties`).
   - Verify Jakarta Bean Validation violations using `Validation.buildDefaultValidatorFactory().getValidator()`.
   - **Deterministic Violation Assertions**: Never use `violations.iterator().next()` (anti-pattern: `Set` has no guaranteed iteration order and throws `NoSuchElementException` if empty). Always assert `assertEquals(expectedCount, violations.size())` and verify field targeting via `assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fieldName")), "...")`.
   - Verify single-field constraints (`@NotBlank`, `@NotEmpty`, `@NotNull`, `@PositiveOrZero`, `@Positive`).
   - Verify container element validation (e.g. lists containing `null`, empty `""`, or blank `"   "` elements fail validation).
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