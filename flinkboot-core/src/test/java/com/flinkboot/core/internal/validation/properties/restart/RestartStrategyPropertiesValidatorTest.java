package com.flinkboot.core.internal.validation.properties.restart;

import com.flinkboot.core.api.properties.restart.ExponentialDelayRestartProperties;
import com.flinkboot.core.api.properties.restart.FailureRateRestartProperties;
import com.flinkboot.core.api.properties.restart.FixedDelayRestartProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import com.flinkboot.core.api.properties.restart.RestartStrategyType;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("RestartStrategyPropertiesValidator")
class RestartStrategyPropertiesValidatorTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when trying to instantiate via reflection")
        void shouldThrowWhenInstantiatedViaReflection() throws Exception {
            var constructor = RestartStrategyPropertiesValidator.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            var targetException = assertThrows(
                InvocationTargetException.class,
                constructor::newInstance
            );

            assertInstanceOf(AssertionError.class, targetException.getCause());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should throw NullPointerException when properties is null")
        void shouldThrowWhenPropertiesIsNull() {
            var context = mock(ConstraintValidatorContext.class);

            var exception = assertThrows(
                NullPointerException.class,
                () -> RestartStrategyPropertiesValidator.validate(null, context)
            );

            assertEquals("properties must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when context is null")
        void shouldThrowWhenContextIsNull() {
            var props = new RestartStrategyProperties(RestartStrategyType.FALLBACK, null, null, null);

            var exception = assertThrows(
                NullPointerException.class,
                () -> RestartStrategyPropertiesValidator.validate(props, null)
            );

            assertEquals("context must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should return true when restart strategy configuration is valid")
        void shouldPassWhenValid() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new RestartStrategyProperties(
                RestartStrategyType.FIXED_DELAY,
                new FixedDelayRestartProperties(3, Duration.ofSeconds(10)),
                null,
                null
            );

            assertTrue(RestartStrategyPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should accumulate violations when sub-block and exponential-delay rules fail simultaneously")
        void shouldAccumulateAllViolations() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new RestartStrategyProperties(
                RestartStrategyType.EXPONENTIAL_DELAY,
                new FixedDelayRestartProperties(3, Duration.ofSeconds(10)),
                new FailureRateRestartProperties(3, Duration.ofMinutes(5), Duration.ofSeconds(10)),
                new ExponentialDelayRestartProperties(Duration.ofSeconds(10), Duration.ofSeconds(5), null, null, null, null)
            );

            assertFalse(RestartStrategyPropertiesValidator.validate(props, context));
            verify(context).buildConstraintViolationWithTemplate("fixed-delay cannot be specified when restart strategy type is EXPONENTIAL_DELAY");
            verify(context).buildConstraintViolationWithTemplate("failure-rate cannot be specified when restart strategy type is EXPONENTIAL_DELAY");
            verify(context).buildConstraintViolationWithTemplate("max-backoff cannot be smaller than initial-backoff in exponential-delay restart strategy");
        }

        @Test
        @DisplayName("Should reject incompatible exponential-delay sub-block and missing backoff pair")
        void shouldWireExponentialDelaySubBlockAndPresenceRules() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new RestartStrategyProperties(
                RestartStrategyType.FIXED_DELAY,
                null,
                null,
                new ExponentialDelayRestartProperties(Duration.ofSeconds(1), null, null, null, null, null)
            );
            var missingPairProps = new RestartStrategyProperties(
                RestartStrategyType.EXPONENTIAL_DELAY,
                null,
                null,
                new ExponentialDelayRestartProperties(Duration.ofSeconds(1), null, null, null, null, null)
            );

            assertFalse(RestartStrategyPropertiesValidator.validate(props, context));
            verify(context).buildConstraintViolationWithTemplate("exponential-delay cannot be specified when restart strategy type is FIXED_DELAY");

            assertFalse(RestartStrategyPropertiesValidator.validate(missingPairProps, context));
            verify(context).buildConstraintViolationWithTemplate("both initial-backoff and max-backoff must be specified together in exponential-delay restart strategy");
        }
    }
}
