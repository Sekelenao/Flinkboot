package io.github.sekelenao.flinkboot.core.api.configuration.restart;

public enum RestartStrategyType {
    NO_RESTART,
    FIXED_DELAY,
    FAILURE_RATE,
    EXPONENTIAL_DELAY,
    FALLBACK
}
