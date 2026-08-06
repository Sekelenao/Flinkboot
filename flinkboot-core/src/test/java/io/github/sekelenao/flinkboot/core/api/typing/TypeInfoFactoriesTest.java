package io.github.sekelenao.flinkboot.core.api.typing;

import io.github.sekelenao.flinkboot.core.api.typing.time.InstantTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTimeTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalTimeTypeInfoFactory;
import org.apache.flink.api.common.typeinfo.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeInfoFactoriesTest {

    @Test
    @DisplayName("LocalDateTimeTypeInfoFactory returns Types.LOCAL_DATE_TIME")
    void testLocalDateTimeTypeInfoFactory() {
        var factory = new LocalDateTimeTypeInfoFactory();
        assertEquals(Types.LOCAL_DATE_TIME, factory.createTypeInfo(null, Collections.emptyMap()));
    }

    @Test
    @DisplayName("LocalDateTypeInfoFactory returns Types.LOCAL_DATE")
    void testLocalDateTypeInfoFactory() {
        var factory = new LocalDateTypeInfoFactory();
        assertEquals(Types.LOCAL_DATE, factory.createTypeInfo(null, Collections.emptyMap()));
    }

    @Test
    @DisplayName("LocalTimeTypeInfoFactory returns Types.LOCAL_TIME")
    void testLocalTimeTypeInfoFactory() {
        var factory = new LocalTimeTypeInfoFactory();
        assertEquals(Types.LOCAL_TIME, factory.createTypeInfo(null, Collections.emptyMap()));
    }

    @Test
    @DisplayName("InstantTypeInfoFactory returns Types.INSTANT")
    void testInstantTypeInfoFactory() {
        var factory = new InstantTypeInfoFactory();
        assertEquals(Types.INSTANT, factory.createTypeInfo(null, Collections.emptyMap()));
    }

}
