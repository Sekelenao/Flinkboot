package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.ExecutionConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DurationTypeInfo")
class DurationTypeInfoTest {

    private final DurationTypeInfo typeInfo = DurationTypeInfo.INSTANCE;

    @Nested
    @DisplayName("Type Information Properties")
    class TypeProperties {

        @Test
        @DisplayName("Should verify all type properties")
        void shouldVerifyTypeProperties() {
            assertAll(
                () -> assertTrue(typeInfo.isBasicType()),
                () -> assertFalse(typeInfo.isTupleType()),
                () -> assertTrue(typeInfo.isKeyType()),
                () -> assertEquals(1, typeInfo.getArity()),
                () -> assertEquals(1, typeInfo.getTotalFields()),
                () -> assertEquals(Duration.class, typeInfo.getTypeClass()),
                () -> assertEquals("Duration", typeInfo.toString())
            );
        }

        @Test
        @DisplayName("Should create DurationSerializer")
        void shouldCreateSerializer() {
            var serializer = typeInfo.createSerializer(new ExecutionConfig());
            assertEquals(DurationSerializer.INSTANCE, serializer);
        }

        @Test
        @DisplayName("Should implement equals, canEqual and hashCode correctly")
        void shouldImplementEqualsAndHashCode() {
            var same = new DurationTypeInfo();
            assertAll(
                () -> assertTrue(typeInfo.canEqual(same)),
                () -> assertFalse(typeInfo.canEqual(new Object())),
                () -> assertEquals(typeInfo, same),
                () -> assertEquals(typeInfo.hashCode(), same.hashCode()),
                () -> assertNotEquals(typeInfo, new Object()),
                () -> assertNotEquals(typeInfo, null)
            );
        }

    }

}
