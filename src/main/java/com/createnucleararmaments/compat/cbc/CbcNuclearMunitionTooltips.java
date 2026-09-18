package com.createnucleararmaments.compat.cbc;

import com.cainiao1053.cbcmoreshells.base.CBCMSTooltip;
import com.cainiao1053.cbcmoreshells.index.CBCMSMunitionPropertiesHandlers;
import com.cainiao1053.cbcmoreshells.munitions.big_cannon.config.TorpedoProperties;
import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.config.RackedProjectileProperties;
import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.config.RackedRocketProjectileProperties;
import com.createnucleararmaments.munitions.NuclearMunitionTooltips;
import com.createnucleararmaments.munitions.NuclearTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * CBCMS munition tooltips. Only referenced from CBC More Shells munition item classes.
 */
public final class CbcNuclearMunitionTooltips {
    private CbcNuclearMunitionTooltips() {
    }

    public static void appendTorpedoShiftStats(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, EntityType<?> entityType) {
        TorpedoProperties properties = CBCMSMunitionPropertiesHandlers.TORPEDO_PROJECTILE.getPropertiesOf(entityType);
        CBCMSTooltip.appendTorpedoInfo(stack, context, tooltip, flag, properties);
        NuclearMunitionTooltips.appendNuclearYieldNote(stack, tooltip, NuclearTier.fromEntity(entityType));
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
        NuclearMunitionTooltips.appendNuclearYieldNote(stack, tooltip, NuclearTier.fromEntity(entityType));
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
        NuclearMunitionTooltips.appendNuclearYieldNote(stack, tooltip, NuclearTier.fromEntity(entityType));
    }
}
