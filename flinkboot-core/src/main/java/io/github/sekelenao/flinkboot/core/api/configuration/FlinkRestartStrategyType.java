package io.github.sekelenao.flinkboot.core.api.configuration;

public enum FlinkRestartStrategyType {
    NONE,
    FIXED_DELAY,
    FAILURE_RATE,
    EXPONENTIAL_DELAY
}
