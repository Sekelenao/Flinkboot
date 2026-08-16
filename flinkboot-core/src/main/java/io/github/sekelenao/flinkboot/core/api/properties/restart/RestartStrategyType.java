package io.github.sekelenao.flinkboot.core.api.properties.restart;

/**
 * Restart strategy types supported by Flink.
 */
public enum RestartStrategyType {
    /**
     * Do not restart failed tasks; fail the job immediately.
     */
    NO_RESTART,

    /**
     * Restart failed tasks with a fixed delay between attempts.
     */
    FIXED_DELAY,

    /**
     * Restart failed tasks as long as failure rate within a time window is not exceeded.
     */
    FAILURE_RATE,

    /**
     * Restart failed tasks with exponentially increasing backoff delay.
     */
    EXPONENTIAL_DELAY,

    /**
     * Fallback to the cluster-level default restart strategy.
     */
    FALLBACK
}
