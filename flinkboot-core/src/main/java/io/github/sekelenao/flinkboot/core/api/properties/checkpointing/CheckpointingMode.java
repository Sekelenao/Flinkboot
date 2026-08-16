package io.github.sekelenao.flinkboot.core.api.properties.checkpointing;

/**
 * Checkpointing semantic consistency guarantees.
 */
public enum CheckpointingMode {
    /**
     * Exactly-once semantic consistency guarantee.
     */
    EXACTLY_ONCE,

    /**
     * At-least-once semantic consistency guarantee (higher throughput, lower latency).
     */
    AT_LEAST_ONCE
}
