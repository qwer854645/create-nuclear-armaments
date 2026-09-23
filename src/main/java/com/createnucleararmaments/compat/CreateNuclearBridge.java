package com.createnucleararmaments.compat;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

/**
 * Soft bridge to Create Nuclear. CN types are only touched via reflection after
 * {@code createnuclear} is confirmed loaded.
 */
public final class CreateNuclearBridge {
    private CreateNuclearBridge() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("createnuclear");
    }

    /**
     * Always applies poison. When Create Nuclear is present, also applies its radiation.
     * When Create: New Age is present, also applies {@code radiation_poisoning}.
     */
    public static void applyFalloutEffects(LivingEntity entity, int duration, int amplifier) {
        applyFalloutEffects(entity, duration, amplifier, false, true, true);
    }

    public static void applyFalloutEffects(
            LivingEntity entity,
            int duration,
            int amplifier,
            boolean ambient,
            boolean visible,
            boolean showIcon
    ) {
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amplifier, ambient, visible, showIcon));
        Holder<MobEffect> radiation = radiationEffectOrNull();
        if (radiation != null) {
            entity.addEffect(new MobEffectInstance(radiation, duration, amplifier, ambient, visible, showIcon));
        }
        CreateNewAgeBridge.applyRadiationPoisoning(entity, duration, amplifier, ambient, visible, showIcon);
    }

    private static Holder<MobEffect> radiationEffectOrNull() {
        if (!isLoaded()) {
            return null;
        }
        try {
            Class<?> effects = Class.forName("net.nuclearteam.createnuclear.CNEffects");
            @SuppressWarnings("unchecked")
            Holder<MobEffect> radiation = (Holder<MobEffect>) effects.getField("RADIATION").get(null);
            return radiation;
        } catch (ReflectiveOperationException | ClassCastException ex) {
            return null;
        }
    }

    public static BlockState enrichedSoulSoilOrFallback() {
        if (!isLoaded()) {
            return Blocks.SOUL_SOIL.defaultBlockState();
        }
        try {
            Class<?> blocks = Class.forName("net.nuclearteam.createnuclear.CNBlocks");
            Object registryObject = blocks.getField("ENRICHED_SOUL_SOIL").get(null);
            Object block = registryObject.getClass().getMethod("get").invoke(registryObject);
            return ((net.minecraft.world.level.block.Block) block).defaultBlockState();
        } catch (ReflectiveOperationException | ClassCastException ex) {
            return Blocks.SOUL_SOIL.defaultBlockState();
        }
    }
}
