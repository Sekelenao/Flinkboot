package io.github.sekelenao.flinkboot.core.internal.parser.yaml;

public final class ParserFeatures {

    private final boolean permitOverride;

    private final boolean listMerging;

    private final boolean disableValidation;

    private final int validationCapacity;

    private ParserFeatures(boolean permitOverride, boolean listMerging, boolean disableValidation, int validationCapacity) {
        this.permitOverride = permitOverride;
        this.listMerging = listMerging;
        this.disableValidation = disableValidation;
        this.validationCapacity = validationCapacity;
    }

    public boolean permitOverride() {
        return permitOverride;
    }

    public boolean listMerging() {
        return listMerging;
    }

    public boolean disableValidation() {
        return disableValidation;
    }

    public int validationCapacity() {
        return validationCapacity;
    }

    public static StepOne builder() {
        return new Builder();
    }

    public interface StepOne {
        StepTwo permitOverride(boolean permitOverride);
    }

    public interface StepTwo {
        StepThree listMerging(boolean listMerging);
    }

    public interface StepThree {
        StepFour disableValidation(boolean disableValidation);
    }

    public interface StepFour {
        Build validationCapacity(int validationCapacity);
    }

    public interface Build {
        ParserFeatures build();
    }

    private static final class Builder implements StepOne, StepTwo, StepThree, StepFour, Build {
        private boolean permitOverride;
        private boolean listMerging;
        private boolean disableValidation;
        private int validationCapacity;

        @Override
        public StepTwo permitOverride(boolean permitOverride) {
            this.permitOverride = permitOverride;
            return this;
        }

        @Override
        public StepThree listMerging(boolean listMerging) {
            this.listMerging = listMerging;
            return this;
        }

        @Override
        public StepFour disableValidation(boolean disableValidation) {
            this.disableValidation = disableValidation;
            return this;
        }

        @Override
        public Build validationCapacity(int validationCapacity) {
            if (validationCapacity <= 0) {
                throw new IllegalArgumentException("Validation capacity must be strictly positive");
            }
            this.validationCapacity = validationCapacity;
            return this;
        }

        @Override
        public ParserFeatures build() {
            return new ParserFeatures(permitOverride, listMerging, disableValidation, validationCapacity);
        }
    }
}
