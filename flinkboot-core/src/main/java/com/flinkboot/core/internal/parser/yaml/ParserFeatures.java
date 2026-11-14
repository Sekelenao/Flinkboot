package com.flinkboot.core.internal.parser.yaml;

public final class ParserFeatures {

    private final boolean permitOverride;

    private final boolean listMerging;

    private ParserFeatures(boolean permitOverride, boolean listMerging) {
        this.permitOverride = permitOverride;
        this.listMerging = listMerging;
    }

    public boolean permitOverride() {
        return permitOverride;
    }

    public boolean listMerging() {
        return listMerging;
    }

    public static StepOne builder() {
        return new Builder();
    }

    public interface StepOne {
        StepTwo permitOverride(boolean permitOverride);
    }

    public interface StepTwo {
        Build listMerging(boolean listMerging);
    }

    public interface Build {
        ParserFeatures build();
    }

    private static final class Builder implements StepOne, StepTwo, Build {
        private boolean permitOverride;
        private boolean listMerging;

        @Override
        public StepTwo permitOverride(boolean permitOverride) {
            this.permitOverride = permitOverride;
            return this;
        }

        @Override
        public Build listMerging(boolean listMerging) {
            this.listMerging = listMerging;
            return this;
        }

        @Override
        public ParserFeatures build() {
            return new ParserFeatures(permitOverride, listMerging);
        }
    }
}
