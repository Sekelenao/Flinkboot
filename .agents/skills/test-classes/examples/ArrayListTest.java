package io.github.example;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


class ArrayListTest {

    @Nested
    @DisplayName("Add")
    class Add {
        
        @Test
        @DisplayName("Should throw IndexOutOfBoundsException when index is out of bounds")
        void shouldThrowExceptionWhenIndexIsOutOfBounds() {
            var lst = new ArrayList<String>();
            lst.add("a");
            lst.add("b");
            assertThrows(IndexOutOfBoundsException.class, () -> lst.get(2));
        }

        @Test
        @DisplayName("Should add element to the list")
        void shouldAddElement() {
            var lst = new ArrayList<String>();
            lst.add("a");
            lst.add("b");
            assertAll(
                () -> assertEquals(2, lst.size()),
                () -> assertTrue(lst.contains("a")),
                () -> assertTrue(lst.contains("b"))
            );
        }

    }

}
