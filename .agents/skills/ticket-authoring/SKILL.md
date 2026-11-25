---
name: ticket-authoring
description: "Draft concise, properly scoped GitHub issues for Flinkboot with adaptive sizing, API-first specifications, and repository label conventions. Use when creating, drafting, or refining GitHub issues or tickets in Flinkboot."
---

# Ticket Authoring

Follow these rules when drafting or refining GitHub issues in Flinkboot.

## Adaptive Sizing

- Scale issue depth strictly to the concrete scope of the work.
- For targeted changes (bugs, null preconditions, minor refactors, single fields), keep the issue under 30 lines: state the problem in 1-2 sentences, identify the exact target location, describe expected observable behavior (including exception type and message), and define Out of Scope. Omit Given/When/Then scenarios for self-evident preconditions.
- For substantial changes (new components, connectors, major features), provide an architectural overview, exact public API signatures, nominal and boundary acceptance criteria, and technical constraints.

## Anti-Patterns to Avoid

- Include a YAML configuration section only when user-facing YAML properties are actually added or modified.
- Show only the public signatures being added or altered; never dump entire existing classes or unchanged methods.
- Specify contracts, interfaces, and annotations without writing full method implementations or test boilerplate.
- Use plain bulleted behavioral criteria rather than markdown checkboxes (`- [ ]`).

## Flinkboot Invariants

- Enforce strict Java 11 compatibility (`release 11`); never specify Java 14+ records, Java 16+ `Stream.toList()`, or newer APIs.
- Require a `CHANGELOG.md` update under the unreleased section for any user-facing change.
- Always include an explicit Out of Scope section to prevent scope creep.

## Title and Label Conventions

- Prefix issue titles with `[Feature]: `, `[Bug]: `, `[Refactor]: `, or `[Documentation]: `.
- Apply `bug` for `[Bug]`, `feature` for `[Feature]`, `dependencies` for dependency updates, and `github_actions` for CI workflows; omit type labels for pure `[Refactor]` or `[Documentation]` issues.
- Apply `java` whenever the issue modifies `.java` source, test, or Javadoc files.
- Apply readiness labels as appropriate: `accepted` (validated and ready), `good first issue` (well-scoped for external contributors), or `needs-triage` / `draft` (scope not yet finalized), and assign the target milestone when specified.
