package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionRuntimeMode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JobConfiguration Tests")
class JobConfigurationTest {

    private static Validator validator;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        mapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should throw NullPointerException when name is null in constructor")
        void shouldThrowNpeWhenNameIsNull() {
            assertThrows(NullPointerException.class, () -> new JobConfiguration(null, null));
        }

        @Test
        @DisplayName("Should correctly return name and environment Optional")
        void shouldReturnGettersCorrectly() {
            var execConfig = new ExecutionConfiguration(ExecutionRuntimeMode.STREAMING, 8, 128, 100L, 200L, true);
            var envConfig = new ExecutionEnvironmentConfiguration(execConfig);
            var jobConfig = new JobConfiguration("my-job", envConfig);

            assertAll(
                () -> assertEquals("my-job", jobConfig.name()),
                () -> assertEquals(envConfig, jobConfig.environment().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should return empty environment Optional when environment is null")
        void shouldReturnEmptyEnvironmentOptional() {
            var jobConfig = new JobConfiguration("my-job", null);

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
            var execConfig = new ExecutionConfiguration(ExecutionRuntimeMode.STREAMING, 8, 128, 100L, 200L, true);
            var envConfig = new ExecutionEnvironmentConfiguration(execConfig);
            var jobConfig = new JobConfiguration("my-job", envConfig);

            Set<ConstraintViolation<JobConfiguration>> violations = validator.validate(jobConfig);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when name is blank")
        void shouldFailValidationWhenNameIsBlank() {
            var jobConfig = new JobConfiguration("   ", null);

            Set<ConstraintViolation<JobConfiguration>> violations = validator.validate(jobConfig);
            assertEquals(1, violations.size());
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize JobConfiguration from JSON")
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

            JobConfiguration jobConfig = mapper.readValue(json, JobConfiguration.class);

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
            var envConfig = new ExecutionEnvironmentConfiguration(null);
            var jobConfig1 = new JobConfiguration("job1", envConfig);
            var jobConfig2 = new JobConfiguration("job1", envConfig);
            var jobConfig3 = new JobConfiguration("job2", envConfig);

            assertAll(
                () -> assertEquals(jobConfig1, jobConfig2),
                () -> assertEquals(jobConfig1.hashCode(), jobConfig2.hashCode()),
                () -> assertNotEquals(jobConfig1, jobConfig3),
                () -> assertNotEquals(null, jobConfig1)
            );
        }
    }
}
