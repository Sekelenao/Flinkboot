package io.github.sekelenao.flinkboot.core.internal.validation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.KebabCaseStrategy;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

import java.util.Objects;

/**
 * Resolves Bean Validation property paths to canonical kebab-case names matching Jackson YAML/JSON configuration keys.
 */
public final class KebabCasePropertyNameMapper implements PropertyNodeNameProvider {

    private static final KebabCaseStrategy STRATEGY = KebabCaseStrategy.INSTANCE;

    @Override
    public String getName(Property property) {
        Objects.requireNonNull(property, "property must not be null");
        return STRATEGY.translate(property.getName());
    }
}
