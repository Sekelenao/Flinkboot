---
name: java-targeted-testing
description: "Run fast, surgical local Java builds and test executions with Maven or Gradle by targeting touched modules and test classes instead of running full repository suites on every iteration. Use when executing Java builds or tests locally."
---

# Java Targeted Testing

Follow these rules when running Java builds or tests locally with Maven or Gradle.

## Fast Iterative Feedback

- During iterative development, run only the affected test class or method rather than the full repository test suite.
- In Maven, target the module and test with `-pl <module> -Dtest=MyTest` or `-Dtest=MyTest#myMethod`.
- In Gradle, target the module and test with `:<module>:test --tests "MyTest"` or `--tests "MyTest.myMethod"`.
- Avoid running `clean` on every build so incremental compilation and build caches keep feedback loops fast; reserve `clean` for stale bytecode issues or major structural refactors.

## Multi-Module and Final Verification

- When modifying a shared or core module in a multi-module project, run tests for the modified module first, then run tests only for the directly affected downstream modules.
- When the repository has a CI pipeline that executes the full test matrix, keep local verification scoped to the touched modules and delegate full repository runs to CI.
- When no CI pipeline exists, run the full test suite once at the end of the task rather than during intermediate iterations.
