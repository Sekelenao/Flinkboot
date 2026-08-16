package io.github.sekelenao.flinkboot.core.api.typing.collection;

import org.apache.flink.api.common.functions.InvalidTypesException;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Generic {@link TypeInfoFactory} for {@link List} fields in Flink POJOs.
 * <p>
 * Extracts generic element type parameters to construct {@link Types#LIST} without falling back to Kryo.
 *
 * @param <E> element type
 */
public class ListTypeInfoFactory<E> extends TypeInfoFactory<List<E>> {

    @Override
    @SuppressWarnings("unchecked")
    public TypeInformation<List<E>> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        var elementType = genericParameters.get("E");
        if (elementType == null) {
            throw new InvalidTypesException("Type extraction is not possible on List (element type unknown).");
        }
        return Types.LIST((TypeInformation<E>) elementType);
    }

}
