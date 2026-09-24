---
name: command-execution
description: Rules for shell command execution without subshells ($(...), backticks) to preserve user-whitelisted permissions.
---

# Command Execution

## Strict Rules

- **No Command Substitution**: Never use `$(...)` or backticks (`` `...` ``). Subshells invalidate security whitelists and trigger manual confirmation prompts.
- **Two-Step Resolution**:
  1. Resolve paths, IDs, or arguments first using native tools or a read command.
  2. Run the command with explicit literal arguments (e.g. `javap path/to/Class.class`).
- **Never Use `cat` (Use Antigravity Tools)**: Never use `cat`, `head`, `tail`, or `less` via `run_command` to inspect files. Always use the native `view_file` tool.
- **Never Use Shell Redirections to Write Files**: Use `write_to_file` or `replace_file_content` instead of `echo >`, `printf >`, or `cat <<EOF`.
- **Chaining with `&&` Allowed**: You may chain deterministic commands with `&&` to save execution turns and tokens (e.g. `mvn clean compile && mvn test`), provided no dynamic substitutions are used.
- **Working Directory**: Never run `cd`; always specify the target directory via the `Cwd` parameter.
