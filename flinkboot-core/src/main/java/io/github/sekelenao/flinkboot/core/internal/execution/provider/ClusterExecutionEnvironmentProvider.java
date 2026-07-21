package io.github.sekelenao.flinkboot.core.internal.execution.provider;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Objects;

public final class ClusterExecutionEnvironmentProvider implements ExecutionEnvironmentProvider {

    @Override
    public StreamExecutionEnvironment createEnvironment(Configuration configuration) {
        Objects.requireNonNull(configuration);
        return StreamExecutionEnvironment.getExecutionEnvironment(configuration);
    }
}
