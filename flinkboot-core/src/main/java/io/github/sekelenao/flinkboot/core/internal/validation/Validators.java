package io.github.sekelenao.flinkboot.core.internal.validation;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;

/**
 * Internal factory providing Jakarta Bean Validation instances configured with Flinkboot conventions.
 */
public final class Validators {

    private Validators() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static ValidatorFactory factory() {
        return Validation.byProvider(HibernateValidator.class)
            .configure()
            .propertyNodeNameProvider(new KebabCasePropertyNameMapper())
            .buildValidatorFactory();
    }
}
