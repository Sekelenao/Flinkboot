## Engineering

Apply KISS by eliminating accidental complexity without sacrificing clear structure: use clean abstractions and patterns when they simplify the domain, never as gratuitous indirection.
Apply YAGNI by avoiding speculative abstractions for hypothetical requirements; introduce extension points only for concrete, known axes of variation or real architectural boundaries.
Prefer a small amount of local duplication over a premature shared helper that couples unrelated callers; apply DRY to shared business knowledge rather than coincidental code similarity.
Fail fast with explicit errors rather than masking invalid states with silent fallbacks or arbitrary defaults.
Avoid inline comments by default and express intent through naming and structure; only add a comment to explain an external constraint or non-obvious workaround.
Keep diffs strictly scoped to the requested change without unrelated reformatting or opportunistic refactors.
