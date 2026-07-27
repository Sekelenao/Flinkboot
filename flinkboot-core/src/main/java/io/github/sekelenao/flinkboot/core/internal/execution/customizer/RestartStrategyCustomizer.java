package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.ExponentialDelayRestartProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.FailureRateRestartProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.FixedDelayRestartProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyType;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;

import java.time.Duration;
import java.util.Objects;

public final class RestartStrategyCustomizer implements EnvironmentCustomizer {

    private final Configuration toConfigure;

    public RestartStrategyCustomizer(Configuration toConfigure) {
        this.toConfigure = Objects.requireNonNull(toConfigure);
    }

    @Override
    public void configure(ExecutionEnvironmentProperties configuration) {
        Objects.requireNonNull(configuration);
        configuration.restartStrategy().ifPresent(this::apply);
    }

    private void apply(RestartStrategyProperties restartConfig) {
        restartConfig.type().ifPresent(type -> this.applyType(type, restartConfig));
    }

    private void applyType(RestartStrategyType type, RestartStrategyProperties restartConfig) {
        switch (type) {
            case NO_RESTART:
                toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY, "none");
                break;
            case FIXED_DELAY:
                toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
                restartConfig.fixedDelay().ifPresent(this::applyFixedDelay);
                break;
            case FAILURE_RATE:
                toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY, "failure-rate");
                restartConfig.failureRate().ifPresent(this::applyFailureRate);
                break;
            case EXPONENTIAL_DELAY:
                toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY, "exponential-delay");
                restartConfig.exponentialDelay().ifPresent(this::applyExponentialDelay);
                break;
            case FALLBACK:
                break;
        }
    }

    private void applyFixedDelay(FixedDelayRestartProperties config) {
        config.attempts().ifPresent(this::applyFixedDelayAttempts);
        config.delayMs().ifPresent(this::applyFixedDelayDelayMs);
    }

    private void applyFixedDelayAttempts(int attempts) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, attempts);
    }

    private void applyFixedDelayDelayMs(long delayMs) {
        var duration = Duration.ofMillis(delayMs);
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, duration);
    }

    private void applyFailureRate(FailureRateRestartProperties config) {
        config.maxFailuresPerInterval().ifPresent(this::applyFailureRateMaxFailuresPerInterval);
        config.failureIntervalMs().ifPresent(this::applyFailureRateFailureIntervalMs);
        config.delayMs().ifPresent(this::applyFailureRateDelayMs);
    }

    private void applyFailureRateMaxFailuresPerInterval(int maxFailuresPerInterval) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_MAX_FAILURES_PER_INTERVAL, maxFailuresPerInterval);
    }

    private void applyFailureRateFailureIntervalMs(long failureIntervalMs) {
        var duration = Duration.ofMillis(failureIntervalMs);
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_FAILURE_RATE_INTERVAL, duration);
    }

    private void applyFailureRateDelayMs(long delayMs) {
        var duration = Duration.ofMillis(delayMs);
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_DELAY, duration);
    }

    private void applyExponentialDelay(ExponentialDelayRestartProperties config) {
        config.initialBackoffMs().ifPresent(this::applyExponentialDelayInitialBackoffMs);
        config.maxBackoffMs().ifPresent(this::applyExponentialDelayMaxBackoffMs);
        config.backoffMultiplier().ifPresent(this::applyExponentialDelayBackoffMultiplier);
        config.resetBackoffThresholdMs().ifPresent(this::applyExponentialDelayResetBackoffThresholdMs);
        config.jitterFactor().ifPresent(this::applyExponentialDelayJitterFactor);
    }

    private void applyExponentialDelayInitialBackoffMs(long initialBackoffMs) {
        var duration = Duration.ofMillis(initialBackoffMs);
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_INITIAL_BACKOFF, duration);
    }

    private void applyExponentialDelayMaxBackoffMs(long maxBackoffMs) {
        var duration = Duration.ofMillis(maxBackoffMs);
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_MAX_BACKOFF, duration);
    }

    private void applyExponentialDelayBackoffMultiplier(double backoffMultiplier) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_BACKOFF_MULTIPLIER, backoffMultiplier);
    }

    private void applyExponentialDelayResetBackoffThresholdMs(long resetBackoffThresholdMs) {
        var duration = Duration.ofMillis(resetBackoffThresholdMs);
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_RESET_BACKOFF_THRESHOLD, duration);
    }

    private void applyExponentialDelayJitterFactor(double jitterFactor) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_JITTER_FACTOR, jitterFactor);
    }
}
