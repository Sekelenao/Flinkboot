package io.github.sekelenao.flinkboot.core.api.typing.math;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Map;

public class BigIntegerTypeInfoFactory extends TypeInfoFactory<BigInteger> {

    @Override
    public TypeInformation<BigInteger> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        return Types.BIG_INT;
    }

}
