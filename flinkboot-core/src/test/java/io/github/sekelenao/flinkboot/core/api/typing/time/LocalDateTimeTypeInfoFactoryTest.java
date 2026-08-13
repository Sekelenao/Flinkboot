package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.typeinfo.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("LocalDateTimeTypeInfoFactory")
class LocalDateTimeTypeInfoFactoryTest {

    @Nested
    @DisplayName("Create Type Information")
    class CreateTypeInfo {

        @Test
        @DisplayName("Should return Types.LOCAL_DATE_TIME")
        void shouldReturnLocalDateTimeTypeInfo() {
            var factory = new LocalDateTimeTypeInfoFactory();
            var typeInfo = factory.createTypeInfo(null, Collections.emptyMap());

            assertEquals(Types.LOCAL_DATE_TIME, typeInfo);
        }

    }

}
