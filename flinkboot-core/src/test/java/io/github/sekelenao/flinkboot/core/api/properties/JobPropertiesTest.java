package io.github.sekelenao.flinkboot.core.api.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionProperties;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionRuntimeMode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JobProperties Tests")
class JobPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should correctly return name and environment Optional")
        void shouldReturnGettersCorrectly() {
            var execConfig = new ExecutionProperties(ExecutionRuntimeMode.STREAMING, 8, 128, Duration.ofMillis(100), Duration.ofMillis(200), true);
            var envProps = new ExecutionEnvironmentProperties(execConfig, null, null, null, null, null, null);
            var jobConfig = new JobProperties("my-job", envProps);

            assertAll(
                () -> assertEquals("my-job", jobConfig.name()),
                () -> assertEquals(envProps, jobConfig.environment().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should return empty environment Optional when environment is null")
        void shouldReturnEmptyEnvironmentOptional() {
            var jobConfig = new JobProperties("my-job", null);

            assertAll(
                () -> assertEquals("my-job", jobConfig.name()),
                () -> assertTrue(jobConfig.environment().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid name and environment")
        void shouldPassValidation() {
            var execConfig = new ExecutionProperties(ExecutionRuntimeMode.STREAMING, 8, 128, Duration.ofMillis(100), Duration.ofMillis(200), true);
            var envProps = new ExecutionEnvironmentProperties(execConfig, null, null, null, null, null, null);
            var jobConfig = new JobProperties("my-job", envProps);


            Set<ConstraintViolation<JobProperties>> violations = validator.validate(jobConfig);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when name is null")
        void shouldFailValidationWhenNameIsNull() {
            var jobConfig = new JobProperties(null, null);

            Set<ConstraintViolation<JobProperties>> violations = validator.validate(jobConfig);
            assertEquals(1, violations.size());
        }

        @Test
        @DisplayName("Should fail validation when name is blank")
        void shouldFailValidationWhenNameIsBlank() {
            var jobConfig = new JobProperties("   ", null);

            Set<ConstraintViolation<JobProperties>> violations = validator.validate(jobConfig);
            assertEquals(1, violations.size());
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize JobProperties from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = "{\n" +
                "  \"name\": \"test-job\",\n" +
                "  \"environment\": {\n" +
                "    \"execution\": {\n" +
                "      \"runtime-mode\": \"STREAMING\",\n" +
                "      \"parallelism\": 4\n" +
                "    }\n" +
                "  }\n" +
                "}";

            JobProperties jobConfig = mapper.readValue(json, JobProperties.class);

            assertAll(
                () -> assertEquals("test-job", jobConfig.name()),
                () -> assertTrue(jobConfig.environment().isPresent()),
                () -> assertEquals(ExecutionRuntimeMode.STREAMING, jobConfig.environment().get().execution().orElseThrow().runtimeMode().orElseThrow()),
                () -> assertEquals(4, jobConfig.environment().get().execution().orElseThrow().parallelism().orElseThrow())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should respect equals and hashCode contract")
        void shouldRespectEqualsAndHashCode() {
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, null, null);
            var jobConfig1 = new JobProperties("job1", envProps);
            var jobConfig2 = new JobProperties("job1", envProps);
            var jobConfig3 = new JobProperties("job2", envProps);

            assertAll(
                () -> assertEquals(jobConfig1, jobConfig2),
                () -> assertEquals(jobConfig1.hashCode(), jobConfig2.hashCode()),
                () -> assertNotEquals(jobConfig1, jobConfig3)
            );
        }
    }
}
