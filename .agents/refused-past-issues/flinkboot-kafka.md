# Refused Past Issues — `flinkboot-kafka`

Module-specific design trade-offs evaluated and rejected in `flinkboot-kafka`. Do not report findings matching these patterns unless you have concrete evidence of a runtime crash or broken contract.

## Configuration-Level Regex Compilation & Partition Uniqueness
- **Pattern**: Enforcing `Pattern.compile()` syntax checks or duplicate `(topic, partition)` checks inside `KafkaSourcePropertiesValidator`.
- **Status**: **REJECTED**
- **Rationale**: Redundant over-validation; Flink and client libraries natively validate regexes and handle partition assignments upon builder initialization.

## Making `KafkaSinkProperties.topic` Optional for `TopicSelector`
- **Pattern**: Making `topic` optional in `KafkaSinkProperties` so `KafkaSinkFactory.supplyBuilderFor(KafkaSinkProperties, Consumer<KafkaRecordSerializationSchemaBuilder<T>>)` can call `setTopicSelector(...)` without conflicting with `setTopic(config.topic())`.
- **Status**: **REJECTED**
- **Rationale**: Keeping `topic` `@NotBlank` preserves strict startup validation for the standard single-topic YAML case. Dynamic multi-topic routing is already supported via `KafkaSinkFactory.supplyBuilderFor(KafkaSinkProperties, KafkaRecordSerializationSchema<T>)` or Flink's `KafkaSink.builder()`.

## Distinguishing Explicit Empty `partitions: []` from `null` in `KafkaOffsetPropertiesValidator`
- **Pattern**: Rejecting `partitions: []` when `strategy` is not `OFFSETS` in `KafkaOffsetPropertiesValidator`.
- **Status**: **REJECTED**
- **Rationale**: Across Flinkboot DTOs, `null` and `[]` are normalized to `Collections.emptyList()`. Distinguishing them solely to reject `partitions: []` adds complexity without practical production value.

## Duplicate `(topic, partition)` Check in `KafkaSourceFactory`
- **Pattern**: Failing fast on duplicate `(topic, partition)` entries in `KafkaSourceFactory.supplyBuilderFor` when building `Set<TopicPartition>`.
- **Status**: **REJECTED**
- **Rationale**: Unlike `OffsetInitializerMapper` (which builds a `Map<TopicPartition, Long>` where conflicting offset values are ambiguous), `KafkaSourceFactory` builds a value-less `Set<TopicPartition>` where duplicates are harmlessly deduplicated by `Collectors.toSet()`, and partition uniqueness validation in `KafkaSourcePropertiesValidator` is intentionally omitted.

## Rejecting `KafkaOffsetInitializer.EARLIEST` in `stopping-offsets`
- **Pattern**: Adding a cross-field validation rule to reject `stopping-offsets.strategy: EARLIEST` in `KafkaSourcePropertiesValidator` / `KafkaBoundednessRule`.
- **Status**: **REJECTED**
- **Rationale**: Flink's `KafkaSourceBuilder.setBounded(OffsetsInitializer)` natively permits `OffsetsInitializer.earliest()`. Avoiding unnecessary cross-field over-validation keeps Flinkboot aligned with Flink's API contract.
