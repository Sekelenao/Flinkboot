---
name: skill-authoring
description: "Design, write, and review reusable AI skills with clear trigger descriptions, focused imperative instructions, and progressive disclosure. Use when creating, updating, or auditing a skill in any AI-assisted project."
---

# Skill Authoring

Follow these principles when creating, updating, or reviewing a reusable AI skill.

## Scope and Naming

- Give each skill a single, cohesive responsibility (a specific workflow, domain standard, or recurring task).
- Use a lowercase `kebab-case` identifier that reflects the capability.
- Ensure the skill ID and trigger scope do not collide or overlap ambiguously with existing skills in the project.

## Trigger-Oriented Description

- Write a concise description that answers two questions: what the skill covers and when the AI should activate it.
- Include concrete task verbs, artifact types, or contexts that naturally appear in user requests.
- Keep the description portable across AI harnesses without referencing tool-specific UI or commands.

## Instruction Design

- Write direct, imperative rules instead of background essays or vague advice.
- Lead with non-negotiable invariants, decision criteria, and concrete workflows.
- Avoid illustrative examples that narrow or anchor the AI's behavior toward specific cases; use minimal structural templates only when format ambiguity is costly.
- Avoid decorative Markdown emphasis (bold/italic asterisks); it wastes tokens without adding semantic value for an LLM.
- Avoid duplicating project-wide conventions that already live in root instructions; focus on domain- or task-specific guidance.
- State expected edge-case behavior and failure handling explicitly when mistakes are costly.

## Progressive Disclosure

- Keep the main `SKILL.md` concise so it loads cleanly without bloating context.
- Move lengthy reference tables, checklists, templates, or helper scripts into companion subdirectories (`references/`, `scripts/`).
- Link companion files using relative paths and specify when to consult them.

## Harness-Agnostic Portability

- Do not hardcode harness-specific configuration directories or vendor-only file paths inside skill instructions.
- Follow the metadata format required by the target repository.
