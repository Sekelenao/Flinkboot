---
name: java-classes-and-records
description: "Design and review immutable Java classes and records with clear record-vs-class criteria, composition over inheritance, and clean accessor conventions. Use when creating or modifying Java classes or records."
---

# Java Classes and Records

Follow these rules when designing standard Java classes and records, unless overridden by a framework, SPI, or serialization contract.

## Forbidden Practices

- Never use Lombok.
- Never expose public setters on domain or value types, except where required by a framework or serialization contract.
- Never use ternary operators (`? :`) inside getters.

## Choosing Between Record and Class

- Record: use a `record` when all components are final and immutable and no custom accessor logic (such as `Optional` wrapping or defensive copying) is needed.
- Class: use a `class` when custom accessor logic, cross-field validation, or defensive copying is required.

## Immutability and Composition

- Declare classes `final` when there is no legitimate reason to extend them, and favor composition over inheritance.
- Do not dogmatically declare classes `final` when integrating third-party framework SPIs or type systems where downstream extension is legitimately expected.
- Prefer `private final` fields.
- Omit the `get` prefix on accessors, except where a JavaBean or serialization contract requires standard getter naming.
