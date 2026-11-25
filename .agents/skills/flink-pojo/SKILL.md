---
name: flink-pojo
description: "Design Apache Flink POJO models as pure zero-Kryo data structs for stream events and state. Use when creating, modifying, or reviewing Flink event models or state value classes."
---

# Flink POJOs

Follow these rules for data types flowing through Apache Flink streams, state backends, or checkpoints.

## Precedence Over General Java Conventions

- For Flink stream events and state models, Flink POJO serialization rules take precedence over general Java immutability and nullability conventions.

## Pure Struct POJO Design

To guarantee `PojoSerializer` selection and schema evolution without boilerplate:

- Declare the class `public` (and `public static` when nested).
- Declare all non-static, non-transient fields as `public` and non-`final`.
- Do not declare any constructor so Java provides the implicit public no-argument constructor required by Flink.
- Do not add any methods (no getters, setters, business logic, or boilerplate methods); keep POJOs as pure data structures.
- Never wrap POJO fields in `Optional`.
- Use boxed types instead of primitives when a field can legitimately be absent (`null`), because missing primitive fields default to zero values.

## Zero-Kryo and Schema Evolution

- Ensure every field type is natively supported by Flink's type extractor (primitives, boxed types, `String`, supported standard types, other compliant POJOs, or types backed by an explicit `TypeInfoFactory`) to prevent silent fallback to Kryo.
- Do not use raw Java `record` types or Flink `Tuple` types for production domain streams and state; use named POJOs so state remains readable and supports schema evolution across savepoints.

## Keying and Testing Implications

- Do not use a method-free POJO directly as a `keyBy` key (it lacks a value-based `hashCode`); extract a primitive, `String`, or dedicated key type instead.
- In unit tests, compare method-free POJOs structurally (such as AssertJ `usingRecursiveComparison`) rather than by reference equality.
