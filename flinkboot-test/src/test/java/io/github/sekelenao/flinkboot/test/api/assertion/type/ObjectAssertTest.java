package io.github.sekelenao.flinkboot.test.api.assertion.type;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

class ObjectAssertTest {

    static class SerializableSample implements Serializable {
        private final String value = "sample";
    }

    static class NonSerializableSample {
        private final Object value = new Object();
    }

    @Test
    void isSerializable_serializableObject_passesAndReturnsThis() {
        ObjectAssert objectAssert = new ObjectAssert(new SerializableSample());

        ObjectAssert result = objectAssert.isSerializable();

        assertSame(objectAssert, result);
    }

    @Test
    void isSerializable_nonSerializableObject_throwsAssertionError() {
        ObjectAssert objectAssert = new ObjectAssert(new NonSerializableSample());

        assertThrows(AssertionError.class, objectAssert::isSerializable);
    }

    @Test
    void constructor_nullObject_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ObjectAssert(null));
    }
}
