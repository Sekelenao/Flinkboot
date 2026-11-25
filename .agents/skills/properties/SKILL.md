---
name: properties
description: "Enforce Flinkboot configuration properties DTO standards for Jackson binding, Jakarta Bean Validation, composable cross-field rules, and generated boilerplate. Use when creating, modifying, or reviewing Flinkboot *Properties DTOs or their validators."
---

# Configuration Properties DTOs

Follow these rules in addition to `java-classes-and-records` and `java-nullability` whenever creating, modifying, or reviewing Flinkboot `*Properties` DTOs.

## Declaration and Jackson Binding

- End every configuration DTO class name with `Properties`, implement `java.io.Serializable`, and declare `private static final long serialVersionUID = 1L;`.
- Define a single canonical constructor annotated with `@JsonCreator` and annotate every parameter with `@JsonProperty("kebab-case-name")`.
- Never add backward-compatibility constructor overloads or telescoping constructors chaining `null` values; update the canonical constructor and callers directly.
- Never call `Objects.requireNonNull(...)` or duplicate Jakarta constraint checks inside the `@JsonCreator` constructor; allow Jackson to build the full object graph so Jakarta Bean Validation reports all missing or invalid keys together.

## Validation

- Annotate single-field constraints with Jakarta Bean Validation (`@NotBlank`, `@NotEmpty`, `@NotNull`, `@PositiveOrZero`, `@Positive`, `@Pattern`, `@Valid`).
- Always validate generic container elements explicitly (`List<@NotBlank String>`, `Map<@NotNull String, @NotNull String>`, `List<@NotNull @Valid NestedProperties>`).
- For cross-field invariants, implement `ValidatableProperties` on the DTO and delegate `validate(ConstraintValidatorContext context)` in one line to a static `*PropertiesValidator.validate(this, context)` facade in `internal.validation.properties.<domain>`.
- In `internal.validation.properties.<domain>`, implement each cross-field invariant as a dedicated `public final` `ValidationRule<T>` class returning `Optional<PropertyViolation>` with no dependency on `ConstraintValidatorContext`.
- In the `*PropertiesValidator` facade, declare `private static final List<ValidationRule<T>> RULES = List.of(...)`, guard instantiation with a private constructor throwing `AssertionError`, and delegate to `PropertiesValidator.compute(properties, context, RULES)` so all rules evaluate without short-circuiting.

## Generated Equality and Formatting

- Annotate `equals(Object other)`, `hashCode()`, and `toString()` with `com.flinkboot.core.internal.annotation.Generated`.
- Name the `equals` parameter `other`, check `!(other instanceof ClassName)`, cast with `var o = (ClassName) other;`, and compare objects/collections with `Objects.equals(...)` and primitives/enums with `==`.
- Implement `hashCode()` with `Objects.hash(...)` and `toString()` returning `ClassName{field1=..., ...}`.
- Consult `examples/StandardProperties.java` when verifying the exact canonical DTO layout.
