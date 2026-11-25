---
name: git-commits
description: "Write atomic, Conventional Commits messages with clear intent, why-focused bodies, breaking-change footers, and safe Git hygiene. Use when staging changes, creating commits, or writing commit messages."
---

# Git Commits

Follow these rules whenever staging changes, drafting commit messages, or creating Git commits.

## Conventional Commit Header

- Format every commit header as `<type>[optional scope]: <description>`.
- Use one of the allowed types:
  - `feat`: new user-facing or API capability
  - `fix`: bug fix restoring correct behavior
  - `perf`: performance, memory, or latency optimization preserving behavior
  - `refactor`: production code restructuring without changing external behavior
  - `test`: adding or updating tests only, with no production code changes
  - `docs`: documentation-only changes
  - `chore`: maintenance, dependency updates, tooling, or repository housekeeping
  - `ci`: continuous integration and delivery workflow changes
- Include a concise lowercase scope when it clarifies the impacted module or domain.
- Write the description in English, imperative mood, lowercase start, at most 72 characters, and without a trailing period.

## Body and Footers

- Omit the body for trivial or self-explanatory commits.
- For non-trivial changes, add a body separated by a blank line explaining why the change was made (root cause, domain constraint, or trade-off) rather than paraphrasing the file diff.
- Mark breaking changes with `!` immediately before the colon (`feat(api)!: ...`) and add a `BREAKING CHANGE:` footer explaining what breaks and how to migrate.
- Reference related issues or tickets in footers when applicable (such as `Fixes #123` or `Refs #123`).

## Atomicity and Git Hygiene

- Keep each commit atomic: one logical change per commit so `git revert` and `git bisect` remain reliable and every commit builds cleanly.
- Inspect staged changes (`git diff --staged`) before committing to ensure no unrelated edits, secrets, or temporary artifacts are included.
- Never rewrite already-pushed history (`--amend`, `--force`, or interactive rebase on shared branches) unless explicitly requested.
