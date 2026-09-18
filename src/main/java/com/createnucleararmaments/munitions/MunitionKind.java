package com.createnucleararmaments.munitions;

public enum MunitionKind {
    SHELL(
            "nuclear_shell",
            "createbigcannons:he_shell",
            "createbigcannons:block/he_shell",
            "createbigcannons:item/he_shell"
    ),
    TORPEDO(
            "nuclear_torpedo",
            "cbcmoreshells:short_range_torpedo",
            "cbcmoreshells:block/short_range_torpedo",
            "cbcmoreshells:item/short_range_torpedo"
    ),
    BOMB(
            "nuclear_bomb",
            "cbcmoreshells:he_bomb",
            "cbcmoreshells:block/he_bomb",
            "cbcmoreshells:item/he_bomb"
    ),
    ROCKET(
            "nuclear_rocket",
            "cbcmoreshells:he_rocket",
            "cbcmoreshells:block/he_rocket",
            "cbcmoreshells:item/he_rocket"
    );

    private final String idPrefix;
    private final String baseItemId;
    private final String visualModel;
    private final String itemModel;

    MunitionKind(String idPrefix, String baseItemId, String visualModel, String itemModel) {
        this.idPrefix = idPrefix;
        this.baseItemId = baseItemId;
        this.visualModel = visualModel;
        this.itemModel = itemModel;
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
        return baseItemId;
    }

    public String visualModel() {
        return visualModel;
    }

    public String itemModel() {
        return itemModel;
    }
}
