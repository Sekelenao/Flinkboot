# Flinkboot Core

Core configuration parsing and validation engine for Flinkboot.

This module provides the core config models, parser implementation, CLI command line parsing utilities, and standard exception definitions.

## Key Features

- 📄 **YAML Configuration Parser** — Type-safe YAML loading powered by Jackson.
- 🛡️ **Jakarta Bean Validation** — Robust config schema constraint verification utilizing Hibernate Validator.
- ⚡ **Fail-Fast Bootstrapping** — Prevents pipelines from launching if configurations are missing or invalid.
