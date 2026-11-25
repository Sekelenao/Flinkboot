---
name: oop-software-design
description: "Apply pragmatic object-oriented software design with single responsibility, strict composition over class inheritance, Tell Don't Ask encapsulation, and composable business rule modeling. Use when designing, implementing, or refactoring domain models, services, validators, or class architectures."
---

# Object-Oriented Software Design

Follow these principles when designing, implementing, or refactoring object-oriented domain models, services, and application components.

## Single Responsibility and I/O Separation

- Give each class one cohesive responsibility and a single axis of change.
- Separate pure domain logic from I/O and infrastructure concerns (HTTP, databases, files, system clocks, messaging) so core logic remains testable without heavy mocking.
- Treat excessive constructor arity, multi-purpose services, or a proliferation of private helper methods as a design smell indicating hidden responsibilities that should be extracted into focused collaborators.

## Encapsulation and Tell, Don't Ask

- Co-locate domain state with the behavior that enforces its invariants; avoid anemic data holders paired with procedural `*Manager`, `*Helper`, or `*Utils` classes that inspect getters to make decisions externally.
- Follow Tell, Don't Ask and the Law of Demeter: call intention-revealing domain methods on direct collaborators instead of chaining getters across nested object graphs.
- Enforce invariants at construction time and make invalid states unrepresentable with domain types and value objects rather than scattering defensive checks across callers.

## Composition, Interfaces, and Composable Rules

- Never use class-to-class inheritance (`extends`) in domain or application code; achieve code reuse through composition and polymorphism exclusively through interfaces (the only exceptions are language- or framework-mandated base types such as `Exception` or `Error`).
- Introduce interfaces only at genuine architectural boundaries (I/O, external integrations) or where multiple implementations exist; never create redundant one-to-one interfaces for internal classes with a single implementation.
- Never name implementations with an `Impl` suffix; if an interface is warranted, name each implementation after its concrete technology, strategy, or behavior.
- Keep interfaces narrow and client-focused rather than defining broad, multi-purpose contracts.
- When a domain accumulates many independent business rules (such as validation, eligibility, pricing, or filtering), model each rule as a small, focused class behind a shared contract (Specification or Strategy-Composite) and compose them in an orchestrator instead of stacking conditionals in a monolithic service.
- Replace growing multi-branch dispatch chains (`if` / `else if` / `instanceof` ladders) with an ordered list of polymorphic handlers exposing applicability and execution (`supports` + `handle`, Chain of Responsibility / Strategy Dispatch) so adding a new case never modifies the orchestrator.

## Readable Control Flow

- Prefer guard clauses and early returns over deeply nested conditionals.
- Keep methods short and operating at a single level of abstraction.
