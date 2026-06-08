package com.createnucleararmaments.munitions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public enum NuclearTier {
    /** ~5 kt tactical */
    T1(1, 12, 5, 160.0F, 0.0F, 28.0F, 1600, 2),
    /** ~25 kt operational */
    T2(2, 48, 25, 280.0F, 0.0F, 40.0F, 2400, 3),
    /** ~100 kt strategic */
    T3(3, 192, 100, 480.0F, 0.0F, 52.0F, 3200, 4);

    public static final NuclearTier[] VALUES = values();

    private static final Map<EntityType<?>, NuclearTier> BY_ENTITY = new HashMap<>();

    private final int tier;
    private final int billetCost;
    private final int yieldKilotons;
    private final float entityExplosionPower;
    private final float blockExplosionPower;
    private final float blastRadius;
    private final float radiationRadius;
    private final int radiationDurationTicks;
    private final int radiationAmplifier;

    NuclearTier(
            int tier,
            int billetCost,
            int yieldKilotons,
            float entityExplosionPower,
            float blockExplosionPower,
            float blastRadius,
            int radiationDurationTicks,
            int radiationAmplifier
    ) {
        this.tier = tier;
        this.billetCost = billetCost;
        this.yieldKilotons = yieldKilotons;
        this.entityExplosionPower = entityExplosionPower;
        this.blockExplosionPower = blockExplosionPower;
        this.blastRadius = blastRadius;
        this.radiationRadius = blastRadius * 2.0F;
        this.radiationDurationTicks = radiationDurationTicks;
        this.radiationAmplifier = radiationAmplifier;
    }

    public int tier() {
        return tier;
    }

    public String suffix() {
        return "t" + tier;
    }

    public int billetCost() {
        return billetCost;
    }

    public int yieldKilotons() {
        return yieldKilotons;
    }

    public float entityExplosionPower() {
        return entityExplosionPower;
    }

    public float blockExplosionPower() {
        return blockExplosionPower;
    }

    public float blastRadius() {
        return blastRadius;
    }

    public float radiationRadius() {
        return radiationRadius;
    }

    public int radiationDurationTicks() {
        return radiationDurationTicks;
    }

    public int radiationAmplifier() {
        return radiationAmplifier;
    }

    public Component displayName() {
        return Component.translatable("tier.createnucleararmaments.t" + tier);
    }

    public void bindEntity(EntityType<?> type) {
        BY_ENTITY.put(type, this);
    }

    public static NuclearTier fromEntity(EntityType<?> type) {
        return BY_ENTITY.getOrDefault(type, T1);
    }
}
