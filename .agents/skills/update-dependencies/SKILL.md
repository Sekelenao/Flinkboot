---
name: update-dependencies
description: Check for and update Maven dependencies and plugin versions.
---

# Checking for Dependency and Plugin Updates

To inspect available dependency and plugin updates, run these standard Maven commands from the project root:

## 1. Check Dependency Updates
```bash
mvn versions:display-dependency-updates
```

## 2. Check Plugin Updates
```bash
mvn versions:display-plugin-updates
```

# Update Guidelines

1. **Minor & Patch Updates** (same major version, e.g. `1.2.0` -> `1.2.1`): Can be updated directly in `pom.xml`.
2. **Major Updates** (breaking API changes, e.g. `2.x` -> `3.x`): Always ask the user for confirmation before updating.
3. **Verification**: Always run `mvn clean test` to verify that all unit tests pass after updating dependencies.
4. **Cleanup**: Remove obsolete or unused Maven properties from `pom.xml`.