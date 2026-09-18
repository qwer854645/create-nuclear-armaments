package com.createnucleararmaments.munitions;

import com.createnucleararmaments.config.CNAConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public enum NuclearTier {
    /** ~5 kt tactical */
    T1(1, 12, 5, 320.0F, 0.0F, 28.0F, 1600, 2),
    /** ~25 kt operational */
    T2(2, 48, 25, 560.0F, 0.0F, 40.0F, 2400, 3),
    /** ~100 kt strategic */
    T3(3, 192, 100, 960.0F, 0.0F, 52.0F, 3200, 4);

    public static final NuclearTier[] VALUES = values();

    private static final Map<EntityType<?>, NuclearTier> BY_ENTITY = new HashMap<>();

    private final int tier;
    private final int defaultBilletCost;
    private final int defaultYieldKilotons;
    private final float defaultEntityExplosionPower;
    private final float defaultBlockExplosionPower;
    private final float defaultBlastRadius;
    private final int defaultRadiationDurationTicks;
    private final int defaultRadiationAmplifier;

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
        this.defaultBilletCost = billetCost;
        this.defaultYieldKilotons = yieldKilotons;
        this.defaultEntityExplosionPower = entityExplosionPower;
        this.defaultBlockExplosionPower = blockExplosionPower;
        this.defaultBlastRadius = blastRadius;
        this.defaultRadiationDurationTicks = radiationDurationTicks;
        this.defaultRadiationAmplifier = radiationAmplifier;
    }

    public int tier() {
        return tier;
    }

    public String suffix() {
        return "t" + tier;
    }

    public int billetCost() {
        return configOrDefault(v -> v.billetCost.get(), defaultBilletCost);
    }

    public int yieldKilotons() {
        return configOrDefault(v -> v.yieldKilotons.get(), defaultYieldKilotons);
    }

    public float entityExplosionPower() {
        return configOrDefault(v -> v.entityExplosionPower.get().floatValue(), defaultEntityExplosionPower);
    }

    public float blockExplosionPower() {
        return defaultBlockExplosionPower;
    }

    public float blastRadius() {
        return configOrDefault(v -> v.blastRadius.get().floatValue(), defaultBlastRadius);
    }

    public float radiationRadius() {
        return blastRadius() * 2.0F;
    }

    public int radiationDurationTicks() {
        return configOrDefault(v -> v.radiationDurationTicks.get(), defaultRadiationDurationTicks);
    }

    public int radiationAmplifier() {
        return configOrDefault(v -> v.radiationAmplifier.get(), defaultRadiationAmplifier);
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

    private <T> T configOrDefault(java.util.function.Function<CNAConfig.TierValues, T> getter, T fallback) {
        try {
            if (CNAConfig.SERVER_SPEC.isLoaded()) {
                return getter.apply(CNAConfig.SERVER.values(this));
            }
        } catch (Throwable ignored) {
            // Config may be unavailable during early bootstrap / datagen.
        }
        return fallback;
    }
}
