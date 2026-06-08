package com.createnucleararmaments.munitions;

import com.cainiao1053.cbcmoreshells.base.CBCMSTooltip;
import com.cainiao1053.cbcmoreshells.index.CBCMSMunitionPropertiesHandlers;
import com.cainiao1053.cbcmoreshells.munitions.big_cannon.config.TorpedoProperties;
import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.config.RackedProjectileProperties;
import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.config.RackedRocketProjectileProperties;
import com.simibubi.create.foundation.item.TooltipHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.createmod.catnip.lang.FontHelper;
import rbasamoyai.createbigcannons.base.CBCTooltip;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonCommonShellProperties;

import java.util.List;

public final class NuclearMunitionTooltips {
    private NuclearMunitionTooltips() {
    }

    public static void appendShellShiftStats(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, EntityType<?> entityType) {
        if (!Screen.hasShiftDown()) {
            CBCMSTooltip.addHoldShift(false, tooltip);
            return;
        }
        BigCannonCommonShellProperties properties = CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE.getPropertiesOf(entityType);
        CBCMSTooltip.appendExplosiveInfo(
                stack,
                context,
                tooltip,
                flag,
                properties.ballistics().durabilityMass(),
                properties.ballistics().penetration(),
                properties.ballistics().deflection(),
                properties.explosion().blockDamagePower()
        );
        appendNuclearYieldNote(stack, tooltip, NuclearTier.fromEntity(entityType));
    }

    public static void appendTorpedoShiftStats(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, EntityType<?> entityType) {
        TorpedoProperties properties = CBCMSMunitionPropertiesHandlers.TORPEDO_PROJECTILE.getPropertiesOf(entityType);
        CBCMSTooltip.appendTorpedoInfo(stack, context, tooltip, flag, properties);
        appendNuclearYieldNote(stack, tooltip, NuclearTier.fromEntity(entityType));
    }

    public static void appendBombShiftStats(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, EntityType<?> entityType) {
        RackedProjectileProperties properties = CBCMSMunitionPropertiesHandlers.RACKED_PROJECTILE.getPropertiesOf(entityType);
        CBCMSTooltip.appendBombInfo(
                stack,
                context,
                tooltip,
                flag,
                properties.ballistics().durabilityMass(),
                properties.ballistics().penetration(),
                properties.ballistics().deflection(),
                properties.explosion().blockDamagePower(),
                properties.lifetime()
        );
        appendNuclearYieldNote(stack, tooltip, NuclearTier.fromEntity(entityType));
    }

    public static void appendRocketShiftStats(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, EntityType<?> entityType) {
        RackedRocketProjectileProperties properties = CBCMSMunitionPropertiesHandlers.RACKED_ROCKET.getPropertiesOf(entityType);
        CBCMSTooltip.appendRackedRocketInfo(
                stack,
                context,
                tooltip,
                flag,
                properties.ballistics().durabilityMass(),
                properties.ballistics().penetration(),
                properties.ballistics().deflection(),
                properties.explosion().blockDamagePower(),
                properties.lifetime(),
                properties.steadyStateVel(),
                properties.thrustTime()
        );
        appendNuclearYieldNote(stack, tooltip, NuclearTier.fromEntity(entityType));
    }

    public static void appendChargeShiftStats(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, NuclearTier tier) {
        if (!Screen.hasShiftDown()) {
            CBCMSTooltip.addHoldShift(false, tooltip);
            return;
        }

        FontHelper.Palette palette = CBCTooltip.getPalette();
        String titleKey = stack.getDescriptionId() + ".tooltip.title";
        tooltip.add(Component.translatable(titleKey).withStyle(net.minecraft.ChatFormatting.GRAY));

        String descKey = stack.getDescriptionId() + ".tooltip.desc";
        tooltip.addAll(TooltipHelper.cutStringTextComponent(
                I18n.get(descKey),
                palette.primary(),
                palette.highlight(),
                1
        ));

        appendNuclearYieldNote(stack, tooltip, tier);
    }

    public static void appendNuclearYieldNote(ItemStack stack, List<Component> tooltip, NuclearTier tier) {
        if (!Screen.hasShiftDown()) {
            return;
        }
        FontHelper.Palette palette = CBCTooltip.getPalette();
        String key = stack.getDescriptionId() + ".tooltip.nuclear";
        tooltip.addAll(TooltipHelper.cutStringTextComponent(
                I18n.get(key, tier.yieldKilotons(), (int) tier.blastRadius(), (int) tier.radiationRadius(), tier.radiationDurationTicks() / 20),
                palette.primary(),
                palette.highlight(),
                1
        ));
    }
}
