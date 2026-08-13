package io.github.sekelenao.flinkboot.core.internal.typing;

import io.github.sekelenao.flinkboot.core.api.typing.collection.ListTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.collection.MapTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.DurationTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTimeTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalTimeTypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.java.typeutils.TypeExtractor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DefaultTypeInfoRegistrar {

    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private DefaultTypeInfoRegistrar() {
        throw new AssertionError("You cannot instantiate this class");
    }

    @SuppressWarnings("unchecked")
    public static void registerDefaultFactories() {
        if (REGISTERED.compareAndSet(false, true)) {
            TypeExtractor.registerFactory(LocalDateTime.class, LocalDateTimeTypeInfoFactory.class);
            TypeExtractor.registerFactory(LocalDate.class, LocalDateTypeInfoFactory.class);
            TypeExtractor.registerFactory(LocalTime.class, LocalTimeTypeInfoFactory.class);
            TypeExtractor.registerFactory(Duration.class, DurationTypeInfoFactory.class);
            TypeExtractor.registerFactory(List.class, (Class<? extends TypeInfoFactory<?>>) ListTypeInfoFactory.class);
            TypeExtractor.registerFactory(Map.class, (Class<? extends TypeInfoFactory<?>>) MapTypeInfoFactory.class);
        }
    }

}
