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

@DisplayName("MapTypeInfoFactory")
class MapTypeInfoFactoryTest {

    @Nested
    @DisplayName("Create Type Information")
    class CreateTypeInfo {

        @Test
        @DisplayName("Should return Types.MAP when key and value types are provided")
        void shouldCreateMapTypeInfo() {
            var factory = new MapTypeInfoFactory<String, Integer>();
            var typeInfo = factory.createTypeInfo(null, Map.of("K", Types.STRING, "V", Types.INT));

            assertEquals(Types.MAP(Types.STRING, Types.INT), typeInfo);
        }

        @Test
        @DisplayName("Should throw InvalidTypesException when key type is missing")
        void shouldThrowExceptionWhenKeyTypeIsMissing() {
            var factory = new MapTypeInfoFactory<>();

            assertThrows(InvalidTypesException.class, () -> factory.createTypeInfo(null, Map.of("V", Types.INT)));
        }

        @Test
        @DisplayName("Should throw InvalidTypesException when value type is missing")
        void shouldThrowExceptionWhenValueTypeIsMissing() {
            var factory = new MapTypeInfoFactory<>();

            assertThrows(InvalidTypesException.class, () -> factory.createTypeInfo(null, Map.of("K", Types.STRING)));
        }

        @Test
        @DisplayName("Should throw InvalidTypesException when generic parameters are empty")
        void shouldThrowExceptionWhenGenericParametersAreEmpty() {
            var factory = new MapTypeInfoFactory<>();

            assertThrows(InvalidTypesException.class, () -> factory.createTypeInfo(null, Collections.emptyMap()));
        }

    }

}
