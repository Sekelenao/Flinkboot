package com.flinkboot.core.internal.execution.customizer;

import com.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import com.flinkboot.core.api.properties.savepoint.RestoreMode;
import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.StateRecoveryOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SavepointRestoreCustomizer Tests")
class SavepointRestoreCustomizerTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should successfully instantiate when Configuration is non-null")
        void shouldSuccessfullyInstantiateWithValidConfiguration() {
            assertDoesNotThrow(() -> new SavepointRestoreCustomizer(new Configuration()));
        }

        @Test
        @DisplayName("Should throw NullPointerException when Configuration is null")
        void shouldThrowNullPointerExceptionWhenConfigurationIsNull() {
            assertThrows(NullPointerException.class, () -> new SavepointRestoreCustomizer(null));
        }
    }

    @Nested
    @DisplayName("Configure")
    class Configure {

        @Test
        @DisplayName("Should throw NullPointerException when ExecutionEnvironmentProperties is null")
        void shouldThrowNullPointerExceptionWhenEnvironmentPropertiesIsNull() {
            var customizer = new SavepointRestoreCustomizer(new Configuration());
            assertThrows(NullPointerException.class, () -> customizer.configure(null));
        }

        @Test
        @DisplayName("Should do nothing when savepoint-restore configuration is absent")
        void shouldDoNothingWhenSavepointRestoreIsAbsent() {
            var flinkConfig = new Configuration();
            var customizer = new SavepointRestoreCustomizer(flinkConfig);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, null, null);

            customizer.configure(envProps);

            assertTrue(flinkConfig.toMap().isEmpty(), "No options should be set when savepoint-restore is absent");
        }

        @Test
        @DisplayName("Should configure all properties when all are present")
        void shouldConfigureAllPropertiesWhenAllArePresent() {
            var flinkConfig = new Configuration();
            var customizer = new SavepointRestoreCustomizer(flinkConfig);
            var savepointConfig = new SavepointRestoreProperties(
                "/tmp/savepoint-1",
                true,
                RestoreMode.CLAIM,
                "/tmp/savepoints"
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, savepointConfig, null, null);

            customizer.configure(envProps);

            assertAll(
                () -> assertEquals("/tmp/savepoints", flinkConfig.get(CheckpointingOptions.SAVEPOINT_DIRECTORY)),
                () -> assertEquals("/tmp/savepoint-1", flinkConfig.get(StateRecoveryOptions.SAVEPOINT_PATH)),
                () -> assertTrue(flinkConfig.get(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE)),
                () -> assertEquals("CLAIM", flinkConfig.get(StateRecoveryOptions.RESTORE_MODE).name())
            );
        }

        @Test
        @DisplayName("Should configure savepointPath only when optional fields are absent")
        void shouldConfigureSavepointPathOnlyWhenOptionalFieldsAreAbsent() {
            var flinkConfig = new Configuration();
            var customizer = new SavepointRestoreCustomizer(flinkConfig);
            var savepointConfig = new SavepointRestoreProperties(
                "/tmp/savepoint-1",
                null,
                null,
                null
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, savepointConfig, null, null);

            customizer.configure(envProps);

            assertAll(
                () -> assertEquals("/tmp/savepoint-1", flinkConfig.get(StateRecoveryOptions.SAVEPOINT_PATH)),
                () -> assertFalse(flinkConfig.contains(CheckpointingOptions.SAVEPOINT_DIRECTORY)),
                () -> assertFalse(flinkConfig.contains(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE)),
                () -> assertFalse(flinkConfig.contains(StateRecoveryOptions.RESTORE_MODE))
            );
        }

        @Test
        @DisplayName("Should configure directory only when savepointPath is absent")
        void shouldConfigureDirectoryOnlyWhenSavepointPathIsAbsent() {
            var flinkConfig = new Configuration();
            var customizer = new SavepointRestoreCustomizer(flinkConfig);
            var savepointConfig = new SavepointRestoreProperties(
                null,
                null,
                null,
                "/tmp/savepoints"
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, savepointConfig, null, null);

            customizer.configure(envProps);

            assertAll(
                () -> assertEquals("/tmp/savepoints", flinkConfig.get(CheckpointingOptions.SAVEPOINT_DIRECTORY)),
                () -> assertFalse(flinkConfig.contains(StateRecoveryOptions.SAVEPOINT_PATH)),
                () -> assertFalse(flinkConfig.contains(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE)),
                () -> assertFalse(flinkConfig.contains(StateRecoveryOptions.RESTORE_MODE))
            );
        }

        @Test
        @DisplayName("Should not apply restore options when savepointPath is absent even if they are specified")
        void shouldNotApplyRestoreOptionsWhenSavepointPathIsAbsentEvenIfSpecified() {
            var flinkConfig = new Configuration();
            var customizer = new SavepointRestoreCustomizer(flinkConfig);
            var savepointConfig = new SavepointRestoreProperties(
                null,
                true,
                RestoreMode.CLAIM,
                "/tmp/savepoints"
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, savepointConfig, null, null);

            customizer.configure(envProps);

            assertAll(
                () -> assertEquals("/tmp/savepoints", flinkConfig.get(CheckpointingOptions.SAVEPOINT_DIRECTORY)),
                () -> assertFalse(flinkConfig.contains(StateRecoveryOptions.SAVEPOINT_PATH)),
                () -> assertFalse(flinkConfig.contains(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE)),
                () -> assertFalse(flinkConfig.contains(StateRecoveryOptions.RESTORE_MODE))
            );
        }

        @Test
        @DisplayName("Should configure restoreMode NO_CLAIM and allowNonRestoredState false when savepointPath is present")
        void shouldConfigureRestoreModeNoClaimAndAllowNonRestoredStateFalseWhenSavepointPathIsPresent() {
            var flinkConfig = new Configuration();
            var customizer = new SavepointRestoreCustomizer(flinkConfig);
            var savepointConfig = new SavepointRestoreProperties(
                "/tmp/savepoint-1",
                false,
                RestoreMode.NO_CLAIM,
                null
            );
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, savepointConfig, null, null);

            customizer.configure(envProps);

            assertAll(
                () -> assertEquals("/tmp/savepoint-1", flinkConfig.get(StateRecoveryOptions.SAVEPOINT_PATH)),
                () -> assertFalse(flinkConfig.get(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE)),
                () -> assertEquals("NO_CLAIM", flinkConfig.get(StateRecoveryOptions.RESTORE_MODE).name()),
                () -> assertFalse(flinkConfig.contains(CheckpointingOptions.SAVEPOINT_DIRECTORY))
            );
        }
    }
}
