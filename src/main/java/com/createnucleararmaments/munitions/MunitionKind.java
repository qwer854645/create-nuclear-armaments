package com.createnucleararmaments.munitions;

public enum MunitionKind {
    SHELL("nuclear_shell", "createbigcannons:he_shell", "createbigcannons:block/he_shell"),
    TORPEDO("nuclear_torpedo", "cbcmoreshells:short_range_torpedo", "cbcmoreshells:block/short_range_torpedo"),
    BOMB("nuclear_bomb", "cbcmoreshells:he_bomb", "cbcmoreshells:block/he_bomb"),
    ROCKET("nuclear_rocket", "cbcmoreshells:he_rocket", "cbcmoreshells:block/he_rocket");

    private final String idPrefix;
    private final String baseItemId;
    private final String visualModel;

    MunitionKind(String idPrefix, String baseItemId, String visualModel) {
        this.idPrefix = idPrefix;
        this.baseItemId = baseItemId;
        this.visualModel = visualModel;
    }

    public String idPrefix() {
        return idPrefix;
    }

    public String idFor(NuclearTier tier) {
        return idPrefix + "_" + tier.suffix();
    }

    public String baseItemId() {
        return baseItemId;
    }

    public String baseItemId(NuclearTier tier) {
        if (this == ROCKET && tier == NuclearTier.T3) {
            return "cbc_at:medium_he_rocket_item";
        }
        return baseItemId;
    }

    public String visualModel() {
        return visualModel;
    }
}
