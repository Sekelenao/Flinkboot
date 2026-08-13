package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.typeinfo.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("InstantTypeInfoFactory")
class InstantTypeInfoFactoryTest {

    @Nested
    @DisplayName("Create Type Information")
    class CreateTypeInfo {

        @Test
        @DisplayName("Should return Types.INSTANT")
        void shouldReturnInstantTypeInfo() {
            var factory = new InstantTypeInfoFactory();
            var typeInfo = factory.createTypeInfo(null, Collections.emptyMap());

            assertEquals(Types.INSTANT, typeInfo);
        }

    }

}
