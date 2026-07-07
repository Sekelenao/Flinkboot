# Flinkboot Test

Testing utilities and integration test support for Flinkboot applications.

This module provides testing utilities designed to simplify test development for Flink pipelines.

## Key Features

- 🧩 **Serialization Verification** — Helper methods to programmatically check if configuration classes or payload classes resolve to `PojoSerializer` instead of falling back to slower serialization (like Kryo).
- 🧪 **Local Cluster Test Helpers** — Integrations to help mock, validate, and execute Flink configurations smoothly on local execution environments and test mini-clusters.
