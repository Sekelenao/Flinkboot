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

1. **Find or Open an Issue**:
   - **Work on an existing issue**: Comment on the issue to express your interest and **wait for a maintainer to officially assign you (`Assignee`)** before starting work or submitting a PR.
   - **Propose a new change**: Open an Issue first (Bug Report, Feature Request, or Documentation) to discuss the proposal with the maintainer.
   - **One Issue, One Assignee**: To prevent duplicated effort and race conditions, do not submit unsolicited PRs on issues that are already claimed or assigned to another contributor. PRs submitted without prior assignment may be closed in favor of the assigned contributor.
   - **Inactivity Policy**: If an assigned contributor does not provide an update or submit a PR within 7 days, the issue may be unassigned and reassigned to someone else.
2. **Submit your PR**: Once officially assigned and aligned, create a Pull Request linking to the issue (`Closes #<issue_number>`).

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
- **Link Issue in PR Body**: Explicitly include `Closes #<issue_number>` in the description body of the Pull Request so GitHub automatically links and closes the issue when merged.

---

## Submitting a Pull Request

1. Ensure you are the officially assigned contributor for the issue.
2. Make sure `mvn clean test` passes locally (100% tests green).
3. Update `CHANGELOG.md` if your change introduces user-facing changes (public APIs, configuration schemas, CLI options, runtime behavior, or bug fixes).
4. Fill out the [Pull Request Template](.github/PULL_REQUEST_TEMPLATE.md), ensuring the PR title follows `#<issue_number>: <title>` and the body contains `Closes #<issue_number>`.

Thank you for contributing to Flinkboot.
