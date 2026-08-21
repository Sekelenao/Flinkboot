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
- **Constructor Null Checks**: Use `Objects.requireNonNull(...)` on mandatory fields (fields that must not be null at construction time).
- **Cross-Field Validation**: Call a private `validate()` method in the constructor when fields have interdependent requirements (e.g. `mode == TIMESTAMP` requires `timestamp != null`).

## 3. Validation Guidelines (Jakarta vs Constructor)

- **Single-Field Constraints** $\rightarrow$ **Jakarta Bean Validation**:
  - Place constraints on fields: `@NotBlank`, `@NotEmpty`, `@NotNull`, `@PositiveOrZero`, `@Positive`, `@Pattern`, `@Valid`.
  - **NEVER** write duplicate manual checks in the constructor (e.g. do **NOT** write `if (batchSize < 0)` in constructor if `@PositiveOrZero` is present).
- **Cross-Field Interdependencies** $\rightarrow$ **Constructor `validate()`**:
  - Put cross-field validation in `private void validate()`.
  - Throw a dedicated domain exception (e.g. `InvalidFlussSourcePropertiesException`).

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
  - `if (this == other) { return true; }`
  - `if (!(other instanceof ClassName)) { return false; }`
  - `var o = (ClassName) other;`
  - Use `Objects.equals(...)` for objects/collections and `==` for enums and primitive values.
- **`hashCode` Pattern**: Use `Objects.hash(field1, field2, ...);`.
- **`toString` Pattern**: Return `"ClassName{" + "field1=" + field1 + ... + '}';`.

## 6. Reference Example

Review `examples/StandardProperties.java` for a complete, compilable reference implementation.
