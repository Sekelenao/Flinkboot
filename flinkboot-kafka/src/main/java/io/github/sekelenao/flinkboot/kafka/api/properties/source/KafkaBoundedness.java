package io.github.sekelenao.flinkboot.kafka.api.properties.source;

/**
 * Execution boundedness mode for Apache Kafka sources in Apache Flink pipelines.
 */
public enum KafkaBoundedness {

    /**
     * Bounded source execution (finite batch or backfill mode) terminating upon reaching stopping offsets.
     */
    BOUNDED,

    /**
     * Unbounded continuous streaming execution (default), with optional stopping condition.
     */
    UNBOUNDED
}
