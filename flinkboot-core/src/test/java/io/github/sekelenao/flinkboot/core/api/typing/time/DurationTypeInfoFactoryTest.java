package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("DurationTypeInfoFactory")
class DurationTypeInfoFactoryTest {

    @Nested
    @DisplayName("Create Type Information")
    class CreateTypeInfo {

        @Test
        @DisplayName("Should return DurationTypeInfo.INSTANCE")
        void shouldReturnDurationTypeInfo() {
            var factory = new DurationTypeInfoFactory();
            var typeInfo = factory.createTypeInfo(null, Collections.emptyMap());

            assertEquals(DurationTypeInfo.INSTANCE, typeInfo);
        }

    }

}
