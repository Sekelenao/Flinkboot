package io.github.sekelenao.flinkboot.core.internal.parser;

public final class FusionFeatures {

    private final boolean permitOverride;
    private final boolean listFusion;

    private FusionFeatures(boolean permitOverride, boolean listFusion) {
        this.permitOverride = permitOverride;
        this.listFusion = listFusion;
    }

    public boolean permitOverride() {
        return permitOverride;
    }

    public boolean listFusion() {
        return listFusion;
    }

    public static StepOne builder() {
        return new Builder();
    }

    public interface StepOne {
        StepTwo permitOverride(boolean permitOverride);
    }

    public interface StepTwo {
        Build listFusion(boolean listFusion);
    }

    public interface Build {
        FusionFeatures build();
    }

    private static final class Builder implements StepOne, StepTwo, Build {
        private boolean permitOverride;
        private boolean listFusion;

        @Override
        public StepTwo permitOverride(boolean permitOverride) {
            this.permitOverride = permitOverride;
            return this;
        }

        @Override
        public Build listFusion(boolean listFusion) {
            this.listFusion = listFusion;
            return this;
        }

        @Override
        public FusionFeatures build() {
            return new FusionFeatures(permitOverride, listFusion);
        }
    }
}
