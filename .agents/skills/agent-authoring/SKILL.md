---
name: agent-authoring
description: Rules and guidelines for creating, updating, or maintaining specialized subagents in .agents/agents/. Use when defining or modifying subagent prompts.
---

# Agent Authoring

## 1. Directory Structure & Frontmatter

- **Location**: Define each subagent in `.agents/agents/<agent-name>/agent.md`.
- **Frontmatter**: Specify `name`, `description`, `tools`, `skills`, `subagent: true`, and `mainAgent: true`.
- **Progressive Disclosure**: Keep `description` action-oriented, stating what the subagent does and the exact trigger conditions for invoking it.

## 2. Skill Delegation & Anti-Duplication

- **No Domain Rules in Agents**: Never embed domain rules, coding standards, language idioms, or code snippets inside `agent.md`.
- **Dynamic Skill Consultation**: Subagents define their operational role, inspection workflow, and output format; all coding and architecture rules must be delegated exclusively to the workspace skills listed under `skills:` and linked in `.agents/skills/`.

## 3. Maintainer Tone & Anti-Sycophancy

- **Direct & Sober**: Maintain a concise, professional maintainer tone.
- **Zero Flattery**: Never praise, flatter, or congratulate. Never summarize or paraphrase the prompt, issue, or PR description.
- **Zero Emojis**: Do NOT use emojis anywhere in subagent output, logs, or reports.

## 4. Actionable Issue Reporting (The 1-Sentence Rule)

- **Target**: Specify exact location (`<file_path>:<line_number>` or `<component>`).
- **Defect in One Sentence**: State the concrete flaw, missing invariant, or violated skill in **exactly one concise sentence**.
- **Fix vs Missing Elements**:
  - **Existing Code Flaws in Diff**: Follow the sentence with a minimal, surgical code suggestion (using GitHub ````suggestion` syntax for PR reviews).
  - **Missing Elements (Tests, Scenarios, Validation)**: Provide a clear, actionable directive describing the expected scenario or boundary condition in one sentence. Do **NOT** flood reports with multi-page boilerplate code or complete test implementations. The author is responsible for implementing the missing logic.
- **No Philosophical Bloat**: Exclude theoretical essays, multi-paragraph justifications, or speculative commentary.

## 5. Context-Adaptive Outputs (No Monolithic Boilerplate)

- **Outcome-Specific Formats**: Never force clean states into templates designed for bug reports. Avoid empty sections ("None") or placeholder tables filled with `0`.
- **Clean State**: When all checks pass, output a concise verdict confirming what was audited and declaring the clean status.
- **Action Required State**: When flaws exist, output exclusively the numbered list of actionable findings with their 1-sentence explanations and drop-in fixes.
- **No Redundant Wrappers**: Never duplicate findings into secondary code blocks or simulated comments. The output must be the feedback itself.

## 6. Workspace Hygiene & Safe Inspection

- **Non-Destructive Inspection**: Use native read tools and non-mutating inspection commands.
- **No Working Tree Churn**: Subagents must never switch branches (`git checkout`) or modify working trees in shared workspaces without explicit instruction.

## 7. Diff Scope & Technical Debt Isolation

- **Review Agents Diff Isolation**: Agents performing pull request or code change reviews (`pr-reviewer`, `test-auditor`, etc.) must audit **exclusively** the lines added or modified in the diff.
- **Zero Blame for Pre-existing Debt**: Never request changes or block a PR for legacy code, pre-existing naming conventions, or untouched assertions that were present before the PR. Pre-existing debt must be handled in separate dedicated issues or refactorings.
