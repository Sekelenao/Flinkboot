---
name: ticket-authoring
description: Strict guidelines and adaptive principles for drafting concise, robust, and properly sized GitHub issues in Flinkboot. Use when creating, drafting, or refining GitHub issues or tickets.
---

# Ticket Authoring

## 1. Adaptive Sizing & Proportionality

Scale the issue depth strictly to the concrete scope of the work:

- **Targeted Scope (Bugs, Null Preconditions, Minor Refactors, Single Fields)**:
  - Keep the issue brief and directly actionable (< 30 lines).
  - State the problem in 1-2 sentences, specify the exact target location, describe the expected observable behavior (exception type + message), and define Out of Scope.
  - Do NOT write multi-step Given/When/Then scenarios for self-evident behavior (such as `Objects.requireNonNull`).
- **Substantial Scope (New Components, New Connectors, Major Features)**:
  - Provide a thorough architectural overview, surgical API signatures, nominal and boundary acceptance scenarios, and technical constraints.

## 2. Anti-Patterns to Strictly Avoid

- **No Irrelevant YAML**: Only include a `Configuration Example (YAML)` section if user-facing YAML configuration or properties DTOs are actually added or modified. Never invent dummy YAML for internal refactors.
- **No Class Dumping**: Only show the exact public method or field signatures being added or altered. Never duplicate entire classes or unchanged methods.
- **No Implementation Spoon-Feeding**: Specify public interfaces, annotations, and contracts, but never write full method bodies or complete test implementations. The contributor is responsible for writing their own implementation.
- **No Fake Checkboxes**: Never use interactive markdown checkboxes (`- [ ]`) in acceptance criteria. Use bulleted behavioral descriptions.

## 3. Project Invariants

- **Java Version**: Strict Java 11 compatibility (`release 11`). Never specify Java 14+ records, Java 16+ `Stream.toList()`, or unsupported APIs.
- **Immutability & Safety**: Enforce `private final` fields, defensive copies, and unmodifiable views.
- **Changelog**: Require updating `CHANGELOG.md` under the unreleased version section for any user-facing change.
- **Out of Scope**: Explicitly define what must NOT be touched to prevent scope creep.
