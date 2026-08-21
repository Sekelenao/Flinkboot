# Contributing to Flinkboot

Thank you for your interest in contributing to Flinkboot.

---

## Code of Conduct

We are committed to providing a friendly and respectful environment for everyone.
- Be constructive, open to feedback, and respectful.
- For any issues or questions, contact the maintainer (@Sekelenao).

---

## Contribution Workflow

1. **Features (Large or Small)**:
   - An Issue is required before opening a PR. Please open an Issue first to discuss and validate the feature design with the maintainer.
2. **Bug Fixes, Typos & Documentation**:
   - You can open a Pull Request directly.

---

## Core Principles & Code Standards

- **Simplicity First (KISS)**: Write simple, explicit, and readable code. Avoid unnecessary abstractions.
- **No Lombok**: Lombok is strictly forbidden to ensure clean JPMS modularity and inspectable bytecode.

> **Detailed Guidelines**: Refer to [.agents/skills/](.agents/skills/) for detailed rules on properties DTOs, package architecture, and testing patterns.

---

## Local Development & Testing

```bash
# Build and run the test suite
mvn clean test

# Install artifacts locally
mvn clean install -DskipTests
```

---

## Git, Branches & Pull Requests

- **Branch naming**: `<issue_number>-<short-description>` (e.g. `42-add-paimon-connector`).
- **Commit messages**: Follow [Conventional Commits](https://www.conventionalcommits.org/) (e.g. `feat(fluss): add FlussSourceFactory`, `fix(core): fix yaml parsing`).
- **Pull Request Title**: Format as `#<issue_number>: <title>` (e.g. `#42: feat(fluss): add fluss source factory`).

---

## Submitting a Pull Request

1. Make sure `mvn clean test` passes locally (100% tests green).
2. Update documentation in `howto/` and `CHANGELOG.md` if your change affects user-facing APIs.
3. Fill out the [Pull Request Template](.github/PULL_REQUEST_TEMPLATE.md).

Thank you for contributing to Flinkboot.
