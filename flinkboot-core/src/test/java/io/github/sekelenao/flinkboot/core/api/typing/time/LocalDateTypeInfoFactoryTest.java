package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.typeinfo.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("LocalDateTypeInfoFactory")
class LocalDateTypeInfoFactoryTest {

    @Nested
    @DisplayName("Create Type Information")
    class CreateTypeInfo {

        @Test
        @DisplayName("Should return Types.LOCAL_DATE")
        void shouldReturnLocalDateTypeInfo() {
            var factory = new LocalDateTypeInfoFactory();
            var typeInfo = factory.createTypeInfo(null, Collections.emptyMap());

            assertEquals(Types.LOCAL_DATE, typeInfo);
        }

    }

}
