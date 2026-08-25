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
        config.delay().ifPresent(this::applyFixedDelayDelay);
    }

    private void applyFixedDelayAttempts(int attempts) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, attempts);
    }

    private void applyFixedDelayDelay(Duration delay) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, delay);
    }

    private void applyFailureRate(FailureRateRestartProperties config) {
        config.maxFailuresPerInterval().ifPresent(this::applyFailureRateMaxFailuresPerInterval);
        config.failureInterval().ifPresent(this::applyFailureRateFailureInterval);
        config.delay().ifPresent(this::applyFailureRateDelay);
    }

    private void applyFailureRateMaxFailuresPerInterval(int maxFailuresPerInterval) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_MAX_FAILURES_PER_INTERVAL, maxFailuresPerInterval);
    }

    private void applyFailureRateFailureInterval(Duration failureInterval) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_FAILURE_RATE_INTERVAL, failureInterval);
    }

    private void applyFailureRateDelay(Duration delay) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_DELAY, delay);
    }

    private void applyExponentialDelay(ExponentialDelayRestartProperties config) {
        config.initialBackoff().ifPresent(this::applyExponentialDelayInitialBackoff);
        config.maxBackoff().ifPresent(this::applyExponentialDelayMaxBackoff);
        config.backoffMultiplier().ifPresent(this::applyExponentialDelayBackoffMultiplier);
        config.resetBackoffThreshold().ifPresent(this::applyExponentialDelayResetBackoffThreshold);
        config.jitterFactor().ifPresent(this::applyExponentialDelayJitterFactor);
    }

    private void applyExponentialDelayInitialBackoff(Duration initialBackoff) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_INITIAL_BACKOFF, initialBackoff);
    }

    private void applyExponentialDelayMaxBackoff(Duration maxBackoff) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_MAX_BACKOFF, maxBackoff);
    }

    private void applyExponentialDelayBackoffMultiplier(double backoffMultiplier) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_BACKOFF_MULTIPLIER, backoffMultiplier);
    }

    private void applyExponentialDelayResetBackoffThreshold(Duration resetBackoffThreshold) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_RESET_BACKOFF_THRESHOLD, resetBackoffThreshold);
    }

    private void applyExponentialDelayJitterFactor(double jitterFactor) {
        toConfigure.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_JITTER_FACTOR, jitterFactor);
    }
}

