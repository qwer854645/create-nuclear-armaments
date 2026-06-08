package com.createnucleararmaments.index;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import rbasamoyai.createbigcannons.CBCTags;

public final class CNATags {
    public static final TagKey<EntityType<?>> PRECISE_MOTION = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("ritchiesprojectilelib", "precise_motion")
    );

    public static final TagKey<net.minecraft.world.item.Item> BIG_CANNON_PROJECTILES = CBCTags.CBCItemTags.BIG_CANNON_PROJECTILES;

    private CNATags() {
    }
}
