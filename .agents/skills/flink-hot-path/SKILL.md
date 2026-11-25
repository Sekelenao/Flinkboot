---
name: flink-hot-path
description: "Enforce low-allocation, GC-friendly Java coding practices on Apache Flink per-record hot paths. Use when writing or reviewing Flink operators, functions, or custom serializers."
---

# Flink Hot Path Performance

Apply these rules strictly inside per-record Flink operator, function, and serializer execution paths. Keep job bootstrap, configuration, lifecycle setup, and tests idiomatic and readable.

## Loops and Iteration

- Never use the Java Stream API (`Collection.stream()`, `Arrays.stream()`) on the hot path; streams allocate spliterators, pipeline stages, and intermediate collections per record.
- Prefer indexed loops (`for (int i = 0, n = list.size(); i < n; i++)`) over enhanced `for-each` loops on `RandomAccess` lists to avoid allocating an `Iterator` on every record.
- Prefer primitive arrays or direct array iteration over boxed collections when iterating fixed or dense numeric data on the hot path.

## Allocation and GC Hygiene

- Hoist reusable, expensive, or thread-safe objects (such as compiled `Pattern`, `DateTimeFormatter`, or mappers) to `static final` constants or `transient` fields initialized once in `open`.
- Avoid unnecessary primitive boxing and unboxing on the hot path.
- Avoid `String.format` and regex compilation per record; prefer direct `StringBuilder` construction or pre-compiled patterns when string manipulation is required.
- Pre-size collections (`new ArrayList<>(expectedSize)`, `HashMap.newHashMap(expectedSize)`) whenever a collection must be allocated per record.

## Logging on the Hot Path

- Avoid `INFO` or `DEBUG` logging on every record; guard or sample diagnostic logs so per-record execution never allocates formatted log messages or saturates I/O.
