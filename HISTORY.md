# Architectural History & Decision Log

Chronological record of architectural decisions, design tradeoffs, and KISS principles shaping Flinkboot.

For user-facing release notes, see [CHANGELOG.md](CHANGELOG.md).  
For AI agent orientation and skills index, see [AGENTS.md](AGENTS.md).

---

## Decisions

### #001 (14/08/26) — Birth of Resource API (v0.2.0)
- **Decision**: Rejection of regex and `isWindows()`. Direct delegation to `Path.of()` for a pure, fast, stateless `FileSystemResource`.

### #002 (15/08/26) — CI Coronation & DTOs (v0.2.0)
- **Decision**: Resolution of JaCoCo false positives via `@Generated` on boilerplate rather than lowering coverage thresholds. Uniform rigor across all DTOs.

### #003 (15/08/26) — Maven Central Target & JPMS Balance (v0.2.0)
- **Decision**: JPMS balance: rejection of `open module` (breaks encapsulation) and excessive locking (blocks tests). Targeted opening of public packages and DTOs.

### #004 (16/08/26) — Javadoc Standard & Apache 2.0 License (v0.2.0)
- **Decision**: Flawless Javadoc without residual FQCNs or warnings. Adoption of Apache 2.0 license aligned with the Flink ecosystem.

### #005 (18/08/26) — Default Classpath (v0.3.0)
- **Decision**: Automatic fallback to `classpath:job-configuration.yaml` to run a job locally without CLI arguments. Strict documentation centralization.

### #006 (18/08/26) — Apache Fluss Bootstrapping (v0.4.0)
- **Decision**: Native integration of Apache Fluss APIs without wrapper overhead. Focus on 1.0.0 restricted to essential connectors (Fluss and Iceberg).

### #007 (19/08/26) — Fluss JPMS Conflict (v0.4.0)
- **Decision**: Isolation of a third-party JAR polluted with a `module-info.class`. Mandatory resolution in Maven (`pom.xml`), zero local tinkering in IDE (`.idea`).

### #008 (20/08/26) — Apache Fluss Upstream Contribution (#4045) (v0.4.0)
- **Decision**: Open-source ethics: fix the bug at the source in the upstream framework (PR #4045 on Fluss) rather than accumulating local hacks.

### #009 (20/08/26) — Architectural Purity & Zero Workarounds (v0.4.0)
- **Decision**: Absolute refusal to pollute source code or `module-info.java` to mask external dependency anomalies. The domain model remains pure.

### #010 (22/08/26) — Open Source Governance & AI Skills (v0.4.0)
- **Decision**: Formalization of standards into AI skills for self-audit prior to PR. Rejection of Lombok, strict traceability via GitHub issues.

### #011 (22/08/26) — Cloud-Native Environment Variable Interpolation (v0.4.0)
- **Decision**: Dedicated `PlaceholderResolver` on the Jackson AST (SRP). Absolute fail-fast: no silent fallback and secret sanitization via `Matcher.quoteReplacement`.

### #012 (23/08/26) — YAML Diagnostic Clarity & ParserFeatures (v0.4.0)
- **Decision**: Rejection of rigid Spring-like profiles in favor of the YAML + environment variables duo. Ordered multi-line validation error reporting without truncation.

### #013 (23/08/26) — Universal Zero-Kryo Validation (v0.4.0)
- **Decision**: `PojoValidator` purely forbids `GenericTypeInfo` (Kryo) without imposing arbitrary class hierarchies. Native readiness for Flink 2.0.

### #014 (25/08/26) — Duration Typing & Realization on requireNonNull (v0.4.0)
- **Decision**: Generalization of `java.time.Duration`. Realization: `Objects.requireNonNull` in constructors masks Jakarta Validation's comprehensive diagnostic, mandating its future removal.

### #015 (26/08/26) — Closing 0.4.0 & Banning the Term clazz (v0.4.0)
- **Decision**: Elimination of the term `clazz` in favor of `type`. Contrasted documentation: 1-line native use case vs composition pattern.

### #016 (27/08/26) — Maven Central Deployment (v0.4.0)
- **Decision**: Separation of concerns: guide users on JVM options via documentation without polluting the build. Conceptual symmetry across connectors.

### #017 (30/08/26) — Visual Identity & Public Presence (v0.4.0)
- **Decision**: SVG logo cropping and refined public messaging: rejection of marketing jargon in favor of accurate, honest production technical terms.

### #018 (30/08/26) — Release Train 0.5.0 & KISS on Durations (v0.5.0)
- **Decision**: Monthly Release Train framework. Rejection of custom annotations in favor of native Flink `@DurationMin`, matching the runtime without truncation risk.

### #019 (30/08/26) — Bean Validation Diagnostics & End of requireNonNull (#36) (v0.5.0)
- **Decision**: Removal of `Objects.requireNonNull` in constructors: Jackson instantiates the full object tree, enabling Bean Validation to report all errors instead of crashing on the first field.

### #020 (30/08/26) — Classpath Fallback in Tests (#46) (v0.5.0)
- **Decision**: Test helper robustness: absence of arguments naturally activates the classpath file without injecting parasitic empty arguments.

### #021 (30/08/26) — Dynamic JaCoCo Discovery in CI (#47) (v0.5.0)
- **Decision**: Dynamic aggregation of JaCoCo reports via discovery patterns rather than static lists, ensuring automatic inclusion of new modules.

### #022 (31/08/26) — FQCN Diagnostic Clarity & Community (#42, #54) (v0.5.0)
- **Decision**: Hermetic encapsulation of parsing exceptions. Precise diagnostics displaying FQCN (`type.getName()`) to eliminate production ambiguities.

### #023 (03/09/26) — Monorepo Autonomy & Network Precision (#75-#80) (v0.5.0)
- **Decision**: Repatriation of documentation into the monorepo. Dynamic ephemeral port allocation for parallel tests aligned with Flink's network runtime.

### #024 (06/09/26) — Subagents & The Goldilocks Rule (#94, #96) (v0.5.0)
- **Decision**: Unlocking documentation PRs in CI. Subagents powered by dynamic workspace inspection (anti-overfitting). Goldilocks rule on tests: mutation-resistant, zero conditional logic.

### #025 (06/09/26) — Semantic Test Clarity & Anti-Over-Engineering (#105) (v0.5.0)
- **Decision**: Refusal of false factorization: isolation of invariants per dedicated test. End of assertions on internal error text messages (testing observable contracts only).

### #026 (06/09/26) — Pragmatic Audit Discernment (#113) (v0.5.0)
- **Decision**: Uncompromising triage of audit alerts: elimination of theoretical false positives and dogmatic rules to retain only real runtime stability impacts.

### #027 (06/09/26) — Exception Extensibility & Rejection of the Final Dogma (#113, #114) (v0.5.0)
- **Decision**: Rejection of the `final` keyword applied mechanically to exceptions: exceptions are diagnostic vectors and must remain extensible per JDK idioms.

### #028 (06/09/26) — Test Debt Cleanup (#119, #122) (v0.5.0)
- **Decision**: Radical elimination of redundant test classes over artificial maintenance. Targeted unit tests with high mutation resistance.

### #029 (08/09/26) — Subagent Swarming & Empirical Validation (#126, #127, PR #128) (v0.5.0)
- **Decision**: Production scanner verified directly against Apache Flink source code. Rejection of superfluous defensive guards and simplification of memory size configuration.

### #030 (12/09/26) — Strict Invariants & Lucidity on Isolation (#99, #103, #132-#134, #138) (v0.5.0)
- **Decision**: Demystification of `@ResourceLock` in multi-process CI. Rejection of false equivalence `Optional.empty() == TIMESTAMP` (OCP respect). Mutation testing uncovering tests blind to exceptions.

### #031 (12/09/26) — Windows Support & Builders' Merit (#56, #57, PR #129, PR #147, PR #152) (v0.5.0)
- **Decision**: Windows unblocking (RFC 8089 `file:///C:` vs Win32 collision) via `FileSystemSupports` backed by `java.nio.file.FileSystems`. UNC/NTFS compliance, intact POSIX isolation (validated on `windows-latest` CI).

### #032 (13/09/26) — DevSecOps & Principle of Least Privilege (v0.5.0)
- **Decision**: Absolute least privilege: zero personal access tokens, dedicated GitHub App (`flinkboot-ci-bot`) with surgical bypass on `main`. Zero third-party dependencies to eradicate supply-chain risks (Bandit, Ruff, CWE-78).

### #033 (14/09/26) — Connector Sovereignty & Streamlined Model (#126) (v0.5.0)
- **Decision**: Golden Rule: top-level DTOs reserved for identity, infrastructure, and Flink semantics. All vendor client tuning (batching, timeouts) offloaded to the free-form `properties` map to eliminate vendor drift.

### #034 (14/09/26) — Jakarta Auto-Validation & End of Tunnel Vision (#158) (v0.5.0)
- **Decision**: End of constructor validations (tunnel vision). Cross-field unification via `ValidatableProperties` and `@ValidConfiguration`. Rejection of custom proprietary validation helpers in favor of standard `ConstraintValidatorContext`.

### #035 (17/09/26) — Architectural Memory & Engineering Humility (#187) (v0.5.0)
- **Decision**: Archiving past tradeoffs in `.agents/refused_past_issues.md`. Rejection of "prod-ready" marketing illusions: maturity takes time. Clear boundary: Flinkboot configures and validates, Flink executes.

### #036 (19/09/26) — Eradication of Audit Noise & Production Impact (#189-#192) (v0.5.0)
- **Decision**: Refusal to alter production code to pass obsolete tests. Strict distinction between field-level constraints (`@Pattern`) and interdependence validators.

### #037 (20/09/26) — Strategy Pattern, Root $ & Creator Bias (#178, PR #195) (v0.5.0)
- **Decision**: Clean Strategy Pattern on `PojoValidator` (specialized handlers, orchestrator < 60 lines). Root prefix `$` (JSONPath style) eliminating Flink `MissingTypeInfo` traps. Total elimination of Mockito in the test module.

### #038 (21/09/26) — Documentation Tidy-Up & Lucidity on FlinkbootTest (#174, PR #199) (v0.5.0)
- **Decision**: Dedicated per-assertion guides in `howto/testing/` and strict separation of YAML parsing. Lucidity on rigid `FlinkbootTest` API (blocking CLI and Jackson options), preparing its removal.

### #039 (21/09/26) — Retirement of FlinkbootTest & Coronation of Varargs (#200, PR #201) (v0.5.0)
- **Decision**: Definitive removal of `FlinkbootTest` in favor of universal varargs `Flinkboot.initialize(String... args)`. Total decoupling: elimination of compile dependency `flinkboot-test` -> `flinkboot-core`.

### #040 (21/09/26) — CollectingSink on SinkV2 & End of Artificial Tests (#173, PR #202) (v0.5.0)
- **Decision**: Pure migration of `CollectingSink` to `SinkV2` (`Sink<T>`) with try-with-resources lifecycle. Elimination of fragile concurrent simulations (`CountDownLatch`) in favor of native Flink runtime.

### #041 (21/09/26) — Escape Hatch Rule & Anticipating Flink 2.0 (#206, PR #208) (v0.5.0)
- **Decision**: Sanctuarisation of the escape hatch: `properties:` applied last in all factories to guarantee operator overrides. Alignment of serialization on `SerializerConfig` and formal deprecation of 1.x APIs.

### #042 (21/09/26) — End of Kafka Validation Short-Circuiting & Hibernate Rigor (#207, PR #209) (v0.5.0)
- **Decision**: Eradication of logical short-circuiting (`&&`): unconditional collection of all violations in `ConstraintValidatorContext`. Complementarity of mocked unit tests vs integration tests with real Hibernate Validator.

### #043 (22/09/26) — IntegerParsingException Typing & Feature Freeze Anticipation (#166) (v0.5.0)
- **Decision**: Replacement of `IllegalArgumentException` with `IntegerParsingException` (non-final, extending `FlinkbootException`) to complete fail-fast encapsulation of CLI/env parameters.

### #044 (24/09/26) — Production Scans Sign-Off & API Sobriety (#216, #218, PR #217, PR #219) (v0.5.0)
- **Decision**: Hard `@NotNull` on `KafkaSinkProperties.deliveryGuarantee` (anti-data loss). Conversion of `InvalidTypesException` to `AssertionFailedError` in `ClassAssert`. Formal rejection of `clearAll()` in `CollectingSink` (Rule #12) to preserve strict UUID isolation in parallel tests.

### #045 (28/09/26) — Closing 0.5.0 & Real-World Application Crucible (#224) (v0.5.0)
- **Decision**: Empirical validation of Flinkboot 0.5.0 in a real production project (`adtech-flinkboot`). Integration of `flink-statebackend-rocksdb` into BOM and upgrade to Fluss 1.0.0 to ensure a complete streaming stack without dependency friction.

### #046 (28/09/26) — Delivery of v0.5.0 & Aiming for Maturity (v0.5.0)
- **Decision**: Publication of `v0.5.0-1.20` to Maven Central. Rejection of premature 1.0.0: API stabilization requires Apache Iceberg (v0.6.0) validation and field experience.

### #047 (03/10/26) — Audit 0.6.0 & Refusal of Pedantry (v0.6.0)
- **Decision**: Audit across all 4 modules (26 targeted issues). Categorical rejection of over-engineering: refusal to catch `Error`/`OutOfMemoryError`, refusal to reinvent Awaitility in `CollectingSink`, and refusal of premature YAML properties on Fluss in favor of pure customizers (`Consumer<Builder>`).

### #048 (04/10/26) — Dual-Mode Boundedness & Kafka Encapsulation (#232, PR #264) (v0.6.0)
- **Decision**: Encapsulation of offsets within `KafkaOffsetProperties` (KISS vs scattering 6+ fields at root) and enabling native Flink batch mode (`setBounded` / `setUnbounded`). Rejection of artificial chronological validation between start and stop offsets: split topology and temporal validity belong to Flink/Kafka runtime.

### #049 (04/10/26) — Jakarta Kebab-Case Unification & Zero XML (#265, PR #272) (v0.6.0)
- **Decision**: Unification of Jakarta error paths to YAML kebab-case via `KebabCasePropertyNameMapper` (`PropertyNodeNameProvider`) and `Validators.factory()`. Rejection of global `validation.xml` (classpath pollution) and abandonment of complex Jackson introspection in favor of direct delegation to `KebabCaseStrategy.INSTANCE` (< 20 lines, zero reflection, zero casting). Strict retention of `@JsonProperty` for immutable DTO instantiation.

### #050 (06/10/26) — Kebab-Case Sanctuarization & Rejection of Caching Over-Engineering (v0.6.0)
- **Decision**: Confirmation of YAML-first kebab-case convention by default across Jackson and Bean Validation with documented rollback via builder customizer. Categorical rejection of complex ClassValue/ConcurrentMap caching of Jackson introspection in favor of simplicity and alignment with Spring/Flink standards.

### #051 (07/10/26) — BufferTimeout Sentinel, KISS & Rejection of Audit Zeal (#249, PR #274) (v0.6.0)
- **Decision**: Retention of `@DurationMin(millis = -1)` without custom annotation to reject values `< -1ms`. Wiring `ExecutionOptions.BUFFER_TIMEOUT_ENABLED = false` in `ExecutionCustomizer` on negative durations to enable maximum Flink throughput, and restoring a pure DTO accessor without filtering.

### #052 (10/10/26) — Single Source of Truth for Docs & Corpus Hub Recognition (v0.6.0)
- **Decision**: Removal of `howto/` guides from repository in favor of dedicated `flinkboot.com` website to eradicate documentation drift (Single Source of Truth) and establish canonical semantic authority.

### #053 (11/10/26) — README Sobriety & Separation of Workshop vs Showcase (v0.6.0)
- **Decision**: Replacement of 70 lines of Before/After code and third-party connector examples with a minimal 6-line sample focused on Flinkboot's core contract. Rejection of Quickstart template duplication and marketing prose, delegated to `flinkboot.com`.

### #054 (13/10/26) — Namespace com.flinkboot, Dogfooding OpenRewrite & Rejection of Gadget Tooling (#275, PR #278) (v0.6.0)
- **Decision**: Full migration to `com.flinkboot` (groupId, JPMS, packages) and POM alignment on `0.6.0-1.20`. Successful internal dogfooding of OpenRewrite to refactor the framework, but firm refusal to impose it on downstream users: a simple IDE rename (15s) surpasses the complexity of a third-party plugin or script.

### #055 (08/11/26) — Decoupling Checkpoint Storage from State Backend (#246) (v0.6.0)
- **Decision**: Relocation of `CheckpointStorageType` and `storage` configuration from `StateBackendProperties` to `CheckpointingProperties`, mirroring Flink's runtime model where checkpoint storage belongs to `CheckpointConfig`. Clean removal without legacy backwards-compatibility baggage and unified cross-field Bean Validation requiring `storage-uri` whenever storage is `FILESYSTEM`.

