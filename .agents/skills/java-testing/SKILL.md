---
name: java-testing
description: "Write and review idiomatic JUnit 5 Java unit tests with high regression sensitivity, anti-tautology discipline, and pragmatic parameterized test design. Use when creating, updating, or reviewing Java test classes."
---

# Java Testing

Follow these rules when creating, updating, or reviewing Java unit tests.

## Structure and JUnit 5 Conventions

- Place each test class in the same package as the class under test under `src/test/java` and declare it package-private.
- Use JUnit 5 (Jupiter) with AssertJ or JUnit assertions.
- Add `@DisplayName` with concise, readable descriptions on test classes, nested classes, and test methods.
- Group related tests by feature or lifecycle with `@Nested`, and omit the `Test` suffix from nested class names.
- Use `assertAll` when verifying multiple related conditions in a single test.
- Import classes, interfaces, and static assertion methods instead of using fully qualified names inline.

## Behavioral Focus and Anti-Tautologies

- Test the public API and real public entry points rather than private methods.
- Reject compiler, JVM, and mock tautologies: never write tests that only verify language mechanics, trivial passthrough getters, or stubbed return values without exercising domain logic. Every test must fail if a realistic mistake or production mutation is introduced.
- Cover boundary conditions, null or empty inputs, and invalid combinations.
- For private utility-class constructors that guard against instantiation, verifying via reflection that the expected error is thrown is sufficient; do not assert the exact message string.

## Parameterized Tests (Goldilocks Rule)

- Consolidate repetitive test methods verifying the same single invariant using `@ParameterizedTest`.
- Never merge distinct business concepts, intentions, or outcomes into a single `@CsvSource` merely to reduce line count; each distinct behavioral path deserves its own test method.
- Reserve `@CsvSource` for multi-variable inputs and expected outputs that express one shared behavioral invariant.
- Never write mega-parameterized tests containing conditional branching or heterogeneous assertions; a parameterized test must verify one invariant at a glance.

## Mutation and Invariant Sensitivity

- Write assertions that strictly fail if a production condition is inverted, swapped, or omitted.
- Avoid symmetry blindspots: test asymmetric combinations of flags or options, not only uniform combinations.
- Verify immutability of returned collections and maps by asserting that mutation throws `UnsupportedOperationException`.
- Keep assertions on unordered collections deterministic: never depend on iteration order (such as taking the first element of a `Set`); assert exact counts and match elements by predicate or property.
