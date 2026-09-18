package com.createnucleararmaments.index;

import com.createnucleararmaments.CNArmaments;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class CNATags {
    public static final TagKey<EntityType<?>> PRECISE_MOTION = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("ritchiesprojectilelib", "precise_motion")
    );

    /** Soil / grass / mud etc. Biome mods can append here for fallout compatibility. */
    public static final TagKey<Block> FALLOUT_SOILS = TagKey.create(
            Registries.BLOCK,
            CNArmaments.id("fallout_soils")
    );

    public static final TagKey<Block> FALLOUT_PLANTS = TagKey.create(
            Registries.BLOCK,
            CNArmaments.id("fallout_plants")
    );

    public static final TagKey<Block> FALLOUT_LEAVES = TagKey.create(
            Registries.BLOCK,
            CNArmaments.id("fallout_leaves")
    );

    public static final TagKey<Block> FALLOUT_LOGS = TagKey.create(
            Registries.BLOCK,
            CNArmaments.id("fallout_logs")
    );

    /** Ice, snow, powder snow, etc. Biome mods can append here. */
    public static final TagKey<Block> FALLOUT_ICE_AND_SNOW = TagKey.create(
            Registries.BLOCK,
            CNArmaments.id("fallout_ice_and_snow")
    );

    public static final TagKey<Block> FALLOUT_SANDS = TagKey.create(
            Registries.BLOCK,
            CNArmaments.id("fallout_sands")
    );

    public static final TagKey<Block> FALLOUT_STONES = TagKey.create(
            Registries.BLOCK,
            CNArmaments.id("fallout_stones")
    );

    public static final TagKey<Block> FALLOUT_TERRACOTTA = TagKey.create(
            Registries.BLOCK,
            CNArmaments.id("fallout_terracotta")
    );

    public static final TagKey<Block> FALLOUT_ORES = TagKey.create(
            Registries.BLOCK,
            CNArmaments.id("fallout_ores")
    );

    /** Common convention tags used by many biome / content packs (optional at load). */
    public static final TagKey<Block> C_DIRT = common("dirt");
    public static final TagKey<Block> C_GRASS = common("grasses");
    public static final TagKey<Block> C_LOGS = common("logs");
    public static final TagKey<Block> C_STRIPPED_LOGS = common("stripped_logs");
    public static final TagKey<Block> C_LEAVES = common("leaves");
    public static final TagKey<Block> C_FLOWERS = common("flowers");
    public static final TagKey<Block> C_CROPS = common("crops");
    public static final TagKey<Block> C_SANDS = common("sands");
    public static final TagKey<Block> C_STONES = common("stones");
    public static final TagKey<Block> C_ORES = common("ores");
    public static final TagKey<Block> C_ICES = common("ices");
    public static final TagKey<Block> C_SNOW_BLOCKS = common("snow_blocks");

    public static final TagKey<Item> BIG_CANNON_PROJECTILES = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("createbigcannons", "big_cannon_projectiles")
    );

    private CNATags() {
    }

    private static TagKey<Block> common(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
