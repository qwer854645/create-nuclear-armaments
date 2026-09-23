package com.createnucleararmaments.munitions;

public enum MunitionKind {
    SHELL(
            "nuclear_shell",
            "createbigcannons:he_shell",
            "createnucleararmaments:block/nuclear_shell",
            "createnucleararmaments:item/nuclear_shell"
    ),
    TORPEDO(
            "nuclear_torpedo",
            "cbcmoreshells:short_range_torpedo",
            "createnucleararmaments:block/nuclear_torpedo",
            "createnucleararmaments:item/nuclear_torpedo"
    ),
    BOMB(
            "nuclear_bomb",
            "cbcmoreshells:he_bomb",
            "createnucleararmaments:block/nuclear_bomb",
            "createnucleararmaments:item/nuclear_bomb"
    ),
    ROCKET(
            "nuclear_rocket",
            "cbcmoreshells:he_rocket",
            "createnucleararmaments:block/nuclear_rocket",
            "createnucleararmaments:item/nuclear_rocket"
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
