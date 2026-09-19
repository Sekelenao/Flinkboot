# How to Validate Custom Cross-Field Configurations

Flinkboot provides the `ValidatableProperties` contract in `flinkboot-core` to express validation rules that span more than one configuration field.

---

## 1. Overview

`ValidatableProperties` is the contract for configuration records and classes whose fields constrain each other. Implement it, write your cross-field rule in the `validate` method, and Flinkboot reports every failure as a standard Jakarta Bean Validation violation.

The public API consists of:
* `ValidatableProperties.validate(ConstraintValidatorContext)` declares the cross-field rule for one properties instance.
* `ValidConfiguration` is the class-level constraint that triggers the rule; implementing `ValidatableProperties` applies it automatically.
* `ConfigurationValidationException` is thrown when a configuration fails validation.

Implementing the contract is enough. Never annotate your own record with `@ValidConfiguration` — the annotation already sits on the interface and covers every implementation.

---

## 2. Defining a Custom Properties Record

Declare a record that implements `ValidatableProperties` and put the cross-field rule inside `validate`:

```java
import io.github.sekelenao.flinkboot.core.api.validation.ValidatableProperties;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public record WindowedPipelineProperties(
    @NotNull WindowType windowType,
    @NotNull Duration windowSize,
    Duration slideDuration
) implements ValidatableProperties {

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        if (windowType == WindowType.SLIDING && (slideDuration == null || slideDuration.compareTo(windowSize) >= 0)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                       "slide-duration must be specified and strictly less than window-size for SLIDING windows")
                   .addPropertyNode("slideDuration")
                   .addConstraintViolation();
            return false;
        }
        return true;
    }
}
```

`WindowType` is your own application enum. Return `true` when the cross-field rule holds, and `false` when it does not.

Field-level constraints such as `@NotNull` keep working as usual: they are evaluated independently of your cross-field rule, and Flinkboot reports both kinds of violation together.

---

## 3. Targeting the Failing Field

Simply returning `false` is not enough. You must explicitly inform the framework which configuration field triggered the validation failure. Without this step, users receive only a generic error message and cannot easily locate which property in their YAML file needs correction.

Inside the `if` branch of `validate`, build a custom violation and bind it to the offending field:

```java
context.disableDefaultConstraintViolation();
context.buildConstraintViolationWithTemplate(
           "slide-duration must be specified and strictly less than window-size for SLIDING windows")
       .addPropertyNode("slideDuration")
       .addConstraintViolation();
return false;
```

`disableDefaultConstraintViolation()` suppresses the automatically generated default constraint violation. If you omit this call, users receive two violations instead of one: the default message and your custom message.

There are two distinct outcomes when returning `false`:

* Building a custom violation: users see `slideDuration: <your-custom-message>`.
* Only returning `false` without building any violation: users see the generic text `Invalid configuration properties`.

> [!NOTE]
> All examples use public APIs only and never reference internal framework classes.

---

## 4. Asserting Validation in Tests

A cross-field rule deserves its own unit test: build an invalid instance, run it through a Bean Validation `Validator`, and assert on the violations it produces.

```java
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowedPipelinePropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Should reject a SLIDING window whose slide-duration is not smaller than the window size")
    void shouldRejectInvalidSlidingWindow() {
        var properties = new WindowedPipelineProperties(
            WindowType.SLIDING,
            Duration.ofMinutes(5),
            Duration.ofMinutes(10)
        );

        var violations = validator.validate(properties);

        assertEquals(1, violations.size());
        var violation = violations.iterator().next();
        assertEquals("slideDuration", violation.getPropertyPath().toString());
        assertTrue(violation.getMessage().contains("strictly less than window-size"));
    }
}
```

`validator.validate(...)` returns the set of violations found on the instance. Assert on three things: the number of violations, the property path, and the message. The property path proves the violation is bound to the field you targeted, and the message proves your custom template is what users will actually read.

This example drives validation through the standard `jakarta.validation.Validator`. The validator Flinkboot uses internally is not exported to application code, so always trigger validation through the standard API in your own tests.
