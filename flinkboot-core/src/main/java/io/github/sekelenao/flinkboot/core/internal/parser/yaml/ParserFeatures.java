package io.github.sekelenao.flinkboot.core.internal.parser.yaml;

public final class ParserFeatures {

    private final boolean permitOverride;

    private final boolean listMerging;

    private final int validationCapacity;

    private ParserFeatures(boolean permitOverride, boolean listMerging, int validationCapacity) {
        this.permitOverride = permitOverride;
        this.listMerging = listMerging;
        this.validationCapacity = validationCapacity;
    }

    public boolean permitOverride() {
        return permitOverride;
    }

    public boolean listMerging() {
        return listMerging;
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
        Build validationCapacity(int validationCapacity);
    }

    public interface Build {
        ParserFeatures build();
    }

    private static final class Builder implements StepOne, StepTwo, StepThree, Build {
        private boolean permitOverride;
        private boolean listMerging;
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
        public Build validationCapacity(int validationCapacity) {
            if (validationCapacity <= 0) {
                throw new IllegalArgumentException("Validation capacity must be strictly positive");
            }
            this.validationCapacity = validationCapacity;
            return this;
        }

        @Override
        public ParserFeatures build() {
            return new ParserFeatures(permitOverride, listMerging, validationCapacity);
        }
    }
}
