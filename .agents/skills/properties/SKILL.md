---
name: configuration-properties
description: Best practices and strict rules for creating Flinkboot configuration properties DTOs (Jackson, Jakarta, Immutability, Fail-fast).
---

# Configuration Properties DTOs

In Flinkboot, all configuration classes that bind to YAML/JSON configuration files must follow these strict rules.

## 1. Class Declaration & Immutability

- **Immutability**: Declare all fields as `private final`.
- **Serializable**: Implement `java.io.Serializable` and declare `private static final long serialVersionUID = 1L;`.
- **Naming**: Class name must end with `Properties` (e.g. `FlussSourceProperties`, `KafkaSinkProperties`).

## 2. Jackson & Constructor Mapping

- **`@JsonCreator`**: Annotate the primary constructor with `@JsonCreator`.
- **`@JsonProperty`**: Annotate **every** parameter with `@JsonProperty("kebab-case-name")`.
- **NO Constructor Null Checks**: Do **NOT** use `Objects.requireNonNull(...)` on constructor parameters. Allow Jackson to construct the DTO with `null` fields so that Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@NotEmpty`) can inspect the full object graph and report all missing/invalid keys simultaneously.
- **Cross-Field Validation**: When fields have interdependent requirements (e.g. `mode == TIMESTAMP` requires `timestamp != null`), the DTO implements `ValidatableProperties` (from `io.github.sekelenao.flinkboot.core.api.validation.ValidatableProperties`). The DTO's `validate(ConstraintValidatorContext context)` delegates in one line to a dedicated static validator in an internal package (e.g. `MyPropertiesValidator.validate(this, context)`).

## 3. Validation Guidelines (Jakarta & Self-Validating DTOs)

- **Single-Field Constraints** $\rightarrow$ **Jakarta Bean Validation**:
  - Place constraints on fields: `@NotBlank`, `@NotEmpty`, `@NotNull`, `@PositiveOrZero`, `@Positive`, `@Pattern`, `@Valid`.
  - **Container Element Validation**: Always validate elements inside generic collections:
    - **String collections** (e.g. `bootstrapServers`, `topics`): Must use `@NotEmpty private final List<@NotBlank String> items;` to strictly reject `null`, empty `""`, and blank `"   "` elements.
    - **Maps** (e.g. `properties`): Must use `private final Map<@NotNull String, @NotNull String> properties;` to reject null keys and values.
    - **Nested DTO collections**: Must use `private final List<@NotNull @Valid MyNestedProperties> items;` to reject null elements and trigger recursive Bean Validation.
  - **NEVER** write duplicate manual checks in the constructor (e.g. do **NOT** write `if (batchSize < 0)` in constructor if `@PositiveOrZero` is present).
- **Cross-Field Interdependencies** $\rightarrow$ **`ValidatableProperties` & Dedicated Static Validator**:
  - Implement `ValidatableProperties` on the DTO:
    ```java
    @Override
    public boolean validate(ConstraintValidatorContext context) {
        return MyPropertiesValidator.validate(this, context);
    }
    ```
  - Create a dedicated validator class in `internal.validation.properties`:
    - **Simple validators (single method)**:
      - Private constructor throwing `new AssertionError("You cannot instantiate this class")`.
      - Static method `public static boolean validate(MyProperties properties, ConstraintValidatorContext context)`.
    - **Complex validators (> 1 method / sub-configurations)**:
      - Store `properties` and `context` as `private final` fields.
      - Private constructor validating non-null arguments via `Objects.requireNonNull(..., "... must not be null")`.
      - Static facade `public static boolean validate(MyProperties properties, ConstraintValidatorContext context) { return new MyPropertiesValidator(properties, context).execute(); }`.
      - Instance helper `reject(propertyName, message)` delegating to `PropertiesValidator.reject(context, propertyName, message)`.
      - Format switch `case` branches on a single line (e.g. `case FIXED_DELAY: return validateFixedDelay();`).
    - Bind violations to property nodes via `PropertiesValidator.reject(context, "propertyName", "message")`.
    - Keep DTOs pure, clean data carriers without massive `if` blocks.

## 4. Getters & Accessors Rules

- **No `get` prefix**: Method names must match the field name exactly (e.g. `name()`, `bootstrapServers()`, `batchSize()`).
- **NO Ternary Operators**: **STRICTLY FORBIDDEN** to use ternary operators `? :` in getters.
- **Optional Values**: Return `Optional<T>`, `OptionalLong`, or `OptionalInt` for nullable fields using explicit `if/return`:
  ```java
  public OptionalLong startupTimestamp() {
      if (startupTimestamp == null) {
          return OptionalLong.empty();
      }
      return OptionalLong.of(startupTimestamp);
  }
  ```
- **Collections & Maps**: Return unmodifiable defensive views (`Collections.unmodifiableList(list)` or `Collections.emptyList()` if null):
  ```java
  public List<String> bootstrapServers() {
      if (bootstrapServers == null) {
          return Collections.emptyList();
      }
      return Collections.unmodifiableList(bootstrapServers);
  }
  ```

## 5. `equals()`, `hashCode()` and `toString()`

- **`@Generated`**: Always annotate `equals(Object other)`, `hashCode()`, and `toString()` with `io.github.sekelenao.flinkboot.core.internal.annotation.Generated`.
- **`equals` Pattern**:
  - Parameter named `other` (type `Object`).
  - `if (!(other instanceof ClassName)) { return false; }`
  - `var o = (ClassName) other;`
  - Use `Objects.equals(...)` for objects/collections and `==` for enums and primitive values.
- **`hashCode` Pattern**: Use `Objects.hash(field1, field2, ...);`.
- **`toString` Pattern**: Return `"ClassName{" + "field1=" + field1 + ... + '}';`.

## 6. Reference Example

Review `examples/StandardProperties.java` for a complete, compilable reference implementation.
