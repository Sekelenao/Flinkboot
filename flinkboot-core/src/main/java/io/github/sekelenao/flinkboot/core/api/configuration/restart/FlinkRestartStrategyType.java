package io.github.sekelenao.flinkboot.core.api.configuration.restart;

public enum FlinkRestartStrategyType {
    NONE,
    FIXED_DELAY,
    FAILURE_RATE,
    EXPONENTIAL_DELAY
}
