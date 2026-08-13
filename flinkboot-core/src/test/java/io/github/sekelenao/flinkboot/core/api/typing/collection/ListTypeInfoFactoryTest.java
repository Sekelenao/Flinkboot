package io.github.sekelenao.flinkboot.core.api.typing.collection;

import org.apache.flink.api.common.functions.InvalidTypesException;
import org.apache.flink.api.common.typeinfo.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ListTypeInfoFactory")
class ListTypeInfoFactoryTest {

    @Nested
    @DisplayName("Create Type Information")
    class CreateTypeInfo {

        @Test
        @DisplayName("Should return Types.LIST when element type is provided")
        void shouldCreateListTypeInfo() {
            var factory = new ListTypeInfoFactory<String>();
            var typeInfo = factory.createTypeInfo(null, Map.of("E", Types.STRING));

            assertEquals(Types.LIST(Types.STRING), typeInfo);
        }

        @Test
        @DisplayName("Should throw InvalidTypesException when element type is missing")
        void shouldThrowExceptionWhenElementTypeIsMissing() {
            var factory = new ListTypeInfoFactory<>();

            assertThrows(InvalidTypesException.class, () -> factory.createTypeInfo(null, Collections.emptyMap()));
        }

    }

}
