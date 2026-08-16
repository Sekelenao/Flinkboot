package io.github.sekelenao.flinkboot.core.api.properties.execution;

/**
 * Execution runtime mode defining how the Flink job is executed.
 */
public enum ExecutionRuntimeMode {
    /**
     * Continuous stream execution mode.
     */
    STREAMING,

    /**
     * Bounded batch execution mode.
     */
    BATCH,

    /**
     * Automatic mode selection based on data sources.
     */
    AUTOMATIC
}
