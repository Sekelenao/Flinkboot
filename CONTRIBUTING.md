# Contributing to Flinkboot

Thank you for your interest in contributing to Flinkboot.

---

## Code of Conduct

We are committed to providing a friendly and respectful environment for everyone.
- Be constructive, open to feedback, and respectful.
- For any issues or questions, contact the maintainer (@Sekelenao).

---

## Contribution Workflow

**Every Pull Request must be linked to an existing Issue.**

1. **Open an Issue First**: Open an Issue (Bug Report, Feature Request, or Documentation) to discuss the proposal with the maintainer.
2. **Submit your PR**: Once the issue is opened and aligned, create a Pull Request linking to the issue (`Closes #<issue_number>`).

---

## Core Principles & Code Standards

- **Simplicity First (KISS)**: Write simple, explicit, and readable code. Avoid unnecessary abstractions.
- **No Lombok**: Lombok is strictly forbidden to ensure clean JPMS modularity and inspectable bytecode.

> **Detailed Guidelines**: Refer to [.agents/skills/](.agents/skills/) for detailed rules on properties DTOs, package architecture, and testing patterns.

---

## Generative AI Usage

The use of Generative AI tools (ChatGPT, Claude, Gemini, GitHub Copilot, Cursor, Antigravity, etc.) is welcome.
- **Compliance Check Recommended**: Using an AI assistant to verify code and test compliance against the guidelines in [.agents/skills/](.agents/skills/) before submitting is strongly recommended.
- **Transparency & Responsibility**: Contributors must disclose AI usage in the Pull Request template and remain fully responsible for reviewing and verifying all submitted code.

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
