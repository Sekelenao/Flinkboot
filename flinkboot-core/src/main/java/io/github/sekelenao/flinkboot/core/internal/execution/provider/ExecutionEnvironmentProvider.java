package io.github.sekelenao.flinkboot.core.internal.execution.provider;

import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidLocalWebUiPropertiesException;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public interface ExecutionEnvironmentProvider {

    static ExecutionEnvironmentProvider get(boolean localWebUIEnabled){
        if(localWebUIEnabled){
            var defaultEnv = StreamExecutionEnvironment.getExecutionEnvironment();
            if (!(defaultEnv instanceof LocalStreamEnvironment)) {
                throw new InvalidLocalWebUiPropertiesException(
                    "Local WebUI configuration (local-web-ui.enabled=true) cannot be used when submitted to a Flink cluster environment."
                );
            }
            return new LocalWebUiExecutionEnvironmentProvider();
        }
        return new ClusterExecutionEnvironmentProvider();
    }

    StreamExecutionEnvironment createEnvironment(Configuration configuration);

}
