package com.createnucleararmaments.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

/**
 * Soft bridge to Create: New Age ({@code create_new_age}).
 * Recipes load via datapack conditions; radiation uses the registry effect / NuclearUtil.
 */
public final class CreateNewAgeBridge {
    public static final String MOD_ID = "create_new_age";

    private static final ResourceLocation RADIATION_POISONING_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "radiation_poisoning");
    private static final TagKey<Item> HAZMAT_SUIT =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "hazmat_suit"));

    private CreateNewAgeBridge() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    /**
     * Applies New Age {@code radiation_poisoning}, respecting full hazmat suits.
     */
    public static void applyRadiationPoisoning(
            LivingEntity entity,
            int duration,
            int amplifier,
            boolean ambient,
            boolean visible,
            boolean showIcon
    ) {
        if (!isLoaded() || duration <= 0 || isFullyHazmatProtected(entity)) {
            return;
        }
        Holder<MobEffect> effect = radiationPoisoningOrNull();
        if (effect != null) {
            entity.addEffect(new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon));
        }
    }

    /**
     * Instant New Age radiation pulse with line-of-sight / casing checks.
     */
    public static void burstRadiationField(Level level, BlockPos pos, int radius) {
        if (!isLoaded() || level.isClientSide || radius <= 0) {
            return;
        }
        try {
            Class<?> util = Class.forName("org.antarcticgardens.cna.content.nuclear.NuclearUtil");
            util.getMethod("createRadiation", int.class, Level.class, BlockPos.class)
                    .invoke(null, radius, level, pos);
        } catch (ReflectiveOperationException ignored) {
            // Soft dependency — ignore if API moves.
        }
    }

    private static Holder<MobEffect> radiationPoisoningOrNull() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(RADIATION_POISONING_ID).orElse(null);
    }

    /** Mirrors New Age {@code NuclearUtil}: every armor slot must be hazmat-tagged. */
    private static boolean isFullyHazmatProtected(LivingEntity entity) {
        boolean anyArmor = false;
        for (ItemStack stack : entity.getArmorSlots()) {
            anyArmor = true;
            if (!stack.is(HAZMAT_SUIT)) {
                return false;
            }
        }
        return anyArmor;
    }
}
