---
name: skill-authoring
description: Rules and checklist for creating, updating, or renaming workspace skills in .agents/skills/. Use when the user asks to create or modify a skill.
---

# Skill Authoring

## 1. Naming & Frontmatter (Progressive Disclosure)

- **Directory Alignment**: The folder path `.agents/skills/<name>/SKILL.md` must match the `name:` field in the YAML frontmatter (`kebab-case`).
- **Trigger-Oriented Description**: Only `name` and `description` are loaded in context initially. Write a concise `description` stating **what** the skill covers and **when** the agent must activate it.

## 2. Conciseness & Single Responsibility

- **Imperative Rules Only**: Write direct `Do` / `Do NOT` rules.
- **No Philosophical Bloat**: Do NOT include "Rationale", "Why", or "Philosophy" sections. Only add a brief inline clause when the agent needs criteria to evaluate a legitimate exception.
- **Single Scope**: Each skill owns one distinct domain. Never duplicate or contradict rules owned by another skill.

## 3. Examples (`examples/`)

- **No Inline Code Examples in `SKILL.md`**: Never embed multi-line code blocks inside `SKILL.md`. Keep `SKILL.md` strictly for rules and place all code examples inside the `examples/` subfolder (referencing them from `SKILL.md`) to preserve progressive disclosure.
- **Strict Compliance**: Every file in `examples/` must strictly obey all existing Flinkboot skills.

## 4. Subagent Synchronization (`.agents/agents/`)

Whenever creating, renaming, or deleting a skill, check whether it must be added, updated, or removed in both the `skills:` frontmatter list and the system prompt instructions of:
- `.agents/agents/module-scanner/agent.md`
- `.agents/agents/test-auditor/agent.md`
- `.agents/agents/pr-reviewer/agent.md`
