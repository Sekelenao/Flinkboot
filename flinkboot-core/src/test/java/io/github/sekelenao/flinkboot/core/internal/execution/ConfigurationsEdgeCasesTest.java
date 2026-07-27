package io.github.sekelenao.flinkboot.core.internal.execution;

import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidRestartStrategyPropertiesException;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidStateBackendPropertiesException;
import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.JobProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingMode;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.ExternalizedCheckpointCleanupMode;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionProperties;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionRuntimeMode;
import io.github.sekelenao.flinkboot.core.api.properties.local.LocalWebUiProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.ExponentialDelayRestartProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.FailureRateRestartProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.FixedDelayRestartProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyProperties;
import io.github.sekelenao.flinkboot.core.api.properties.restart.RestartStrategyType;
import io.github.sekelenao.flinkboot.core.api.properties.savepoint.RestoreMode;
import io.github.sekelenao.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.CheckpointStorageType;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Configurations Edge Cases Tests")
public class ConfigurationsEdgeCasesTest {

    @Test
    @DisplayName("Should test equals, hashCode, toString for all Configuration DTOs")
    void shouldTestEqualsHashCodeToStringOnDtos() {
        var execConfig1 = new ExecutionProperties(ExecutionRuntimeMode.STREAMING, 4, 32, 100L, 200L, true);
        var execConfig2 = new ExecutionProperties(ExecutionRuntimeMode.STREAMING, 4, 32, 100L, 200L, true);
        var execConfig3 = new ExecutionProperties(ExecutionRuntimeMode.BATCH, 2, 16, 50L, 100L, false);

        assertAll(
            () -> assertEquals(execConfig1, execConfig1),
            () -> assertEquals(execConfig1, execConfig2),
            () -> assertNotEquals(execConfig1, execConfig3),
            () -> assertNotEquals(execConfig1, null),
            () -> assertNotEquals(execConfig1, "other-object"),
            () -> assertEquals(execConfig1.hashCode(), execConfig2.hashCode()),
            () -> assertNotNull(execConfig1.toString())
        );

        var chkConfig1 = new CheckpointingProperties(true, 1000L, CheckpointingMode.EXACTLY_ONCE, 5000L, 100L, 1, ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, true, 500L, "s3://uri");
        var chkConfig2 = new CheckpointingProperties(true, 1000L, CheckpointingMode.EXACTLY_ONCE, 5000L, 100L, 1, ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, true, 500L, "s3://uri");

        assertAll(
            () -> assertEquals(chkConfig1, chkConfig1),
            () -> assertEquals(chkConfig1, chkConfig2),
            () -> assertNotEquals(chkConfig1, null),
            () -> assertNotEquals(chkConfig1, "other"),
            () -> assertEquals(chkConfig1.hashCode(), chkConfig2.hashCode()),
            () -> assertNotNull(chkConfig1.toString())
        );

        var fixed1 = new FixedDelayRestartProperties(3, 1000L);
        var fixed2 = new FixedDelayRestartProperties(3, 1000L);
        assertAll(
            () -> assertEquals(fixed1, fixed1),
            () -> assertEquals(fixed1, fixed2),
            () -> assertNotEquals(fixed1, null),
            () -> assertNotEquals(fixed1, "other"),
            () -> assertEquals(fixed1.hashCode(), fixed2.hashCode()),
            () -> assertNotNull(fixed1.toString())
        );

        var failRate1 = new FailureRateRestartProperties(5, 60000L, 1000L);
        var failRate2 = new FailureRateRestartProperties(5, 60000L, 1000L);
        assertAll(
            () -> assertEquals(failRate1, failRate1),
            () -> assertEquals(failRate1, failRate2),
            () -> assertNotEquals(failRate1, null),
            () -> assertNotEquals(failRate1, "other"),
            () -> assertEquals(failRate1.hashCode(), failRate2.hashCode()),
            () -> assertNotNull(failRate1.toString())
        );

        var exp1 = new ExponentialDelayRestartProperties(1000L, 60000L, 2.0, 300000L, 0.1);
        var exp2 = new ExponentialDelayRestartProperties(1000L, 60000L, 2.0, 300000L, 0.1);
        assertAll(
            () -> assertEquals(exp1, exp1),
            () -> assertEquals(exp1, exp2),
            () -> assertNotEquals(exp1, null),
            () -> assertNotEquals(exp1, "other"),
            () -> assertEquals(exp1.hashCode(), exp2.hashCode()),
            () -> assertNotNull(exp1.toString())
        );

        var state1 = new StateBackendProperties(StateBackendType.ROCKSDB, CheckpointStorageType.FILESYSTEM, "s3://dir", true, true, null);
        var state2 = new StateBackendProperties(StateBackendType.ROCKSDB, CheckpointStorageType.FILESYSTEM, "s3://dir", true, true, null);
        assertAll(
            () -> assertEquals(state1, state1),
            () -> assertEquals(state1, state2),
            () -> assertNotEquals(state1, null),
            () -> assertNotEquals(state1, "other"),
            () -> assertEquals(state1.hashCode(), state2.hashCode()),
            () -> assertNotNull(state1.toString())
        );

        var save1 = new SavepointRestoreProperties("/path", true, RestoreMode.CLAIM);
        var save2 = new SavepointRestoreProperties("/path", true, RestoreMode.CLAIM);
        assertAll(
            () -> assertEquals(save1, save1),
            () -> assertEquals(save1, save2),
            () -> assertNotEquals(save1, null),
            () -> assertNotEquals(save1, "other"),
            () -> assertEquals(save1.hashCode(), save2.hashCode()),
            () -> assertNotNull(save1.toString())
        );

        var web1 = new LocalWebUiProperties(true, 8081, "localhost");
        var web2 = new LocalWebUiProperties(true, 8081, "localhost");
        assertAll(
            () -> assertEquals(web1, web1),
            () -> assertEquals(web1, web2),
            () -> assertNotEquals(web1, null),
            () -> assertNotEquals(web1, "other"),
            () -> assertEquals(web1.hashCode(), web2.hashCode()),
            () -> assertNotNull(web1.toString())
        );

        var env1 = new ExecutionEnvironmentProperties(execConfig1, chkConfig1, null, state1, save1, web1, Map.of("k", "v"));
        var env2 = new ExecutionEnvironmentProperties(execConfig1, chkConfig1, null, state1, save1, web1, Map.of("k", "v"));
        assertAll(
            () -> assertEquals(env1, env1),
            () -> assertEquals(env1, env2),
            () -> assertNotEquals(env1, null),
            () -> assertNotEquals(env1, "other"),
            () -> assertEquals(env1.hashCode(), env2.hashCode()),
            () -> assertNotNull(env1.toString())
        );

        var job1 = new JobProperties("job", env1);
        var job2 = new JobProperties("job", env1);
        assertAll(
            () -> assertEquals(job1, job1),
            () -> assertEquals(job1, job2),
            () -> assertNotEquals(job1, null),
            () -> assertNotEquals(job1, "other"),
            () -> assertEquals(job1.hashCode(), job2.hashCode()),
            () -> assertNotNull(job1.toString())
        );
    }

    @Test
    @DisplayName("Should test invalid restart strategy sub-block configurations")
    void shouldTestInvalidRestartStrategySubBlocks() {
        var fixed = new FixedDelayRestartProperties(1, 1000L);
        assertAll(
            () -> assertThrows(InvalidRestartStrategyPropertiesException.class,
                () -> new RestartStrategyProperties(RestartStrategyType.FIXED_DELAY, null, new FailureRateRestartProperties(1, 1L, 1L), null)),
            () -> assertThrows(InvalidRestartStrategyPropertiesException.class,
                () -> new RestartStrategyProperties(RestartStrategyType.FAILURE_RATE, fixed, null, null)),
            () -> assertThrows(InvalidRestartStrategyPropertiesException.class,
                () -> new RestartStrategyProperties(RestartStrategyType.EXPONENTIAL_DELAY, fixed, null, null)),
            () -> assertThrows(InvalidRestartStrategyPropertiesException.class,
                () -> new RestartStrategyProperties(RestartStrategyType.NO_RESTART, fixed, null, null))
        );
    }

    @Test
    @DisplayName("Should test invalid state backend custom class configuration")
    void shouldTestInvalidStateBackendCustomClass() {
        assertAll(
            () -> assertThrows(InvalidStateBackendPropertiesException.class,
                () -> new StateBackendProperties(StateBackendType.CUSTOM, CheckpointStorageType.JOBMANAGER, null, false, false, null)),
            () -> assertThrows(InvalidStateBackendPropertiesException.class,
                () -> new StateBackendProperties(StateBackendType.ROCKSDB, CheckpointStorageType.JOBMANAGER, null, false, false, "com.example.CustomBackend"))
        );
    }
}
