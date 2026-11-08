package com.flinkboot.core.internal.execution.customizer;

import com.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import com.flinkboot.core.api.properties.restart.ExponentialDelayRestartProperties;
import com.flinkboot.core.api.properties.restart.FailureRateRestartProperties;
import com.flinkboot.core.api.properties.restart.FixedDelayRestartProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyType;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RestartStrategyCustomizer Tests")
class RestartStrategyCustomizerTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw NullPointerException when Configuration is null")
        void shouldThrowNullPointerExceptionWhenConfigurationIsNull() {
            assertThrows(NullPointerException.class, () -> new RestartStrategyCustomizer(null));
        }
    }

    @Nested
    @DisplayName("Configure")
    class Configure {

        @Test
        @DisplayName("Should throw NullPointerException when ExecutionEnvironmentProperties is null")
        void shouldThrowNullPointerExceptionWhenPropertiesAreNull() {
            var customizer = new RestartStrategyCustomizer(new Configuration());
            assertThrows(NullPointerException.class, () -> customizer.configure(null));
        }

        @Test
        @DisplayName("Should do nothing when restart strategy configuration is empty")
        void shouldDoNothingWhenEmpty() {
            var flinkConfig = new Configuration();
            var customizer = new RestartStrategyCustomizer(flinkConfig);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, null, null);
            customizer.configure(envProps);

            assertTrue(flinkConfig.toMap().isEmpty(), "No options should be set when restart strategy is absent");
        }

        @Test
        @DisplayName("Should do nothing when restart strategy type is absent")
        void shouldDoNothingWhenTypeIsAbsent() {
            var flinkConfig = new Configuration();
            var customizer = new RestartStrategyCustomizer(flinkConfig);
            var restartProps = new RestartStrategyProperties(null, null, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartProps, null, null, null, null);
            customizer.configure(envProps);

            assertTrue(flinkConfig.toMap().isEmpty(), "No options should be set when restart strategy type is absent");
        }

        @Test
        @DisplayName("Should configure NO_RESTART strategy correctly")
        void shouldConfigureNoRestart() {
            var flinkConfig = new Configuration();
            var customizer = new RestartStrategyCustomizer(flinkConfig);
            var restartProps = new RestartStrategyProperties(RestartStrategyType.NO_RESTART, null, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartProps, null, null, null, null);
            customizer.configure(envProps);

            assertEquals("none", flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY));
        }

        @Test
        @DisplayName("Should configure FIXED_DELAY strategy with all properties")
        void shouldConfigureFixedDelay() {
            var flinkConfig = new Configuration();
            var customizer = new RestartStrategyCustomizer(flinkConfig);
            var fixed = new FixedDelayRestartProperties(3, Duration.ofSeconds(10));
            var restartProps = new RestartStrategyProperties(RestartStrategyType.FIXED_DELAY, fixed, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartProps, null, null, null, null);
            customizer.configure(envProps);

            assertAll(
                () -> assertEquals("fixed-delay", flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY)),
                () -> assertEquals(3, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS)),
                () -> assertEquals(Duration.ofSeconds(10), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY))
            );
        }

        @Test
        @DisplayName("Should configure FAILURE_RATE strategy with all properties")
        void shouldConfigureFailureRate() {
            var flinkConfig = new Configuration();
            var customizer = new RestartStrategyCustomizer(flinkConfig);
            var failure = new FailureRateRestartProperties(5, Duration.ofMinutes(5), Duration.ofSeconds(15));
            var restartProps = new RestartStrategyProperties(RestartStrategyType.FAILURE_RATE, null, failure, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartProps, null, null, null, null);
            customizer.configure(envProps);

            assertAll(
                () -> assertEquals("failure-rate", flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY)),
                () -> assertEquals(5, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_MAX_FAILURES_PER_INTERVAL)),
                () -> assertEquals(Duration.ofMinutes(5), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_FAILURE_RATE_INTERVAL)),
                () -> assertEquals(Duration.ofSeconds(15), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_DELAY))
            );
        }

        @Test
        @DisplayName("Should configure EXPONENTIAL_DELAY strategy with attempts and all properties")
        void shouldConfigureExponentialDelayWithAttempts() {
            var flinkConfig = new Configuration();
            var customizer = new RestartStrategyCustomizer(flinkConfig);
            var expo = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                2.0,
                Duration.ofHours(1),
                0.1,
                5
            );
            var restartProps = new RestartStrategyProperties(RestartStrategyType.EXPONENTIAL_DELAY, null, null, expo);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartProps, null, null, null, null);
            customizer.configure(envProps);

            assertAll(
                () -> assertEquals("exponential-delay", flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY)),
                () -> assertEquals(Duration.ofSeconds(1), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_INITIAL_BACKOFF)),
                () -> assertEquals(Duration.ofMinutes(1), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_MAX_BACKOFF)),
                () -> assertEquals(2.0, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_BACKOFF_MULTIPLIER)),
                () -> assertEquals(Duration.ofHours(1), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_RESET_BACKOFF_THRESHOLD)),
                () -> assertEquals(0.1, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_JITTER_FACTOR)),
                () -> assertEquals(5, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_ATTEMPTS))
            );
        }

        @Test
        @DisplayName("Should configure EXPONENTIAL_DELAY strategy when attempts is omitted")
        void shouldConfigureExponentialDelayWithoutAttempts() {
            var flinkConfig = new Configuration();
            var customizer = new RestartStrategyCustomizer(flinkConfig);
            var expo = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(2),
                Duration.ofMinutes(2),
                1.5,
                Duration.ofMinutes(30),
                0.2,
                null
            );
            var restartProps = new RestartStrategyProperties(RestartStrategyType.EXPONENTIAL_DELAY, null, null, expo);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartProps, null, null, null, null);
            customizer.configure(envProps);

            assertAll(
                () -> assertEquals("exponential-delay", flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY)),
                () -> assertEquals(Duration.ofSeconds(2), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_INITIAL_BACKOFF)),
                () -> assertEquals(Duration.ofMinutes(2), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_MAX_BACKOFF)),
                () -> assertEquals(1.5, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_BACKOFF_MULTIPLIER)),
                () -> assertEquals(Duration.ofMinutes(30), flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_RESET_BACKOFF_THRESHOLD)),
                () -> assertEquals(0.2, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_JITTER_FACTOR)),
                () -> assertTrue(flinkConfig.getOptional(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_ATTEMPTS).isEmpty()),
                () -> assertEquals(Integer.MAX_VALUE, flinkConfig.get(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_ATTEMPTS))
            );
        }

        @Test
        @DisplayName("Should do nothing for FALLBACK restart strategy")
        void shouldDoNothingForFallback() {
            var flinkConfig = new Configuration();
            var customizer = new RestartStrategyCustomizer(flinkConfig);
            var restartProps = new RestartStrategyProperties(RestartStrategyType.FALLBACK, null, null, null);
            var envProps = new ExecutionEnvironmentProperties(null, null, restartProps, null, null, null, null);
            customizer.configure(envProps);

            assertTrue(flinkConfig.toMap().isEmpty(), "No configuration options should be set for FALLBACK strategy");
        }
    }
}
