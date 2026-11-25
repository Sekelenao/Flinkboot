---
name: agent-authoring
description: "Design, write, and review specialized AI agents with focused roles, least-privilege capabilities, clear delegation triggers, and structured workflows. Use when creating, updating, or auditing an AI agent or custom mode."
---

# Agent Authoring

Follow these principles when creating, updating, or reviewing a specialized AI agent or custom mode.

## Role Definition and Naming

- Give each agent a narrow, well-bounded mission rather than a generic assistant role.
- Use a lowercase `kebab-case` identifier and a clear, human-readable display name.
- Ensure the agent ID does not collide with other agents that may be combined in the same project.

## Clear Delegation Guidance

- Summarize the agent's core responsibility concisely in its metadata.
- Explicitly state when a user or orchestrator should invoke this agent—and when it should not be used—in a dedicated trigger field if supported, or directly within the description otherwise.
- Declare the agent's execution mode (primary, subagent, or both) when the target format distinguishes standalone primary agents from delegated subagents.

## Least-Privilege Capabilities

- Explicitly grant only the minimum capabilities the role requires.
- Apply file-path scoping when the agent is only meant to inspect or edit a specific subset of the repository.

## Prompt Structure

Organize the agent prompt around behavior rather than duplicated standards:

1. Mission: state the agent's goal, perspective, and quality bar in a few lines.
2. Inputs and Context: specify what the agent should inspect first before forming conclusions or making changes.
3. Workflow: define an ordered, repeatable sequence of steps.
4. Constraints and Boundaries: state what the agent must avoid, when to escalate or ask for clarification, and how to handle uncertainty.
5. Output Format: define the exact deliverable structure.

## Delegate Standards to Skills

- Do not copy lengthy coding guidelines, checklists, or domain specifications into an agent prompt.
- Reference reusable skills for detailed standards so multiple agents and workflows share a single source of truth, and declare explicit skill IDs in agent metadata when the agent depends on specific skills and the target format supports it.
- Avoid illustrative examples that narrow or anchor the agent's judgment toward specific cases; use minimal structural templates only when output format ambiguity is costly.
- Avoid decorative Markdown emphasis (bold/italic asterisks); it wastes tokens without adding semantic value for an LLM.

## Harness-Agnostic Portability

- Avoid hardcoding harness-specific directories, proprietary tool names, or platform-only syntax inside the agent prompt.
- Keep prompt content in clean Markdown and let the target repository or tooling handle native metadata formatting.
