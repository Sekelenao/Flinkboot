package io.github.sekelenao.flinkboot.core.internal.execution.provider;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public interface ExecutionEnvironmentProvider {
    StreamExecutionEnvironment createEnvironment(Configuration configuration);
}
