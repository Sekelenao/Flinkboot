package com.flinkboot.core.internal.validation;

public final class ValidationFeatures {

    private final boolean disableValidation;

    private final int capacity;

    private ValidationFeatures(boolean disableValidation, int capacity) {
        this.disableValidation = disableValidation;
        this.capacity = capacity;
    }

    public boolean disableValidation() {
        return disableValidation;
    }

    public int capacity() {
        return capacity;
    }

    public static StepOne builder() {
        return new Builder();
    }

    public interface StepOne {
        StepTwo disableValidation(boolean disableValidation);
    }

    public interface StepTwo {
        Build capacity(int capacity);
    }

    public interface Build {
        ValidationFeatures build();
    }

    private static final class Builder implements StepOne, StepTwo, Build {
        private boolean disableValidation;
        private int capacity;

        @Override
        public StepTwo disableValidation(boolean disableValidation) {
            this.disableValidation = disableValidation;
            return this;
        }

        @Override
        public Build capacity(int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("Validation capacity must be strictly positive");
            }
            this.capacity = capacity;
            return this;
        }

        @Override
        public ValidationFeatures build() {
            return new ValidationFeatures(disableValidation, capacity);
        }
    }
}
