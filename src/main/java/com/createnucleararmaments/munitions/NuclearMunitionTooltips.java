package com.createnucleararmaments.munitions;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Tooltips that must work without Create / CBC / CBCMS.
 */
public final class NuclearMunitionTooltips {
    private NuclearMunitionTooltips() {
    }

    public static void appendChargeShiftStats(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, NuclearTier tier) {
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.literal("Hold ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("Shift").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(" for details").withStyle(ChatFormatting.DARK_GRAY)));
            return;
        }

        String titleKey = stack.getDescriptionId() + ".tooltip.title";
        if (I18n.exists(titleKey)) {
            tooltip.add(Component.translatable(titleKey).withStyle(ChatFormatting.GRAY));
        }

        String descKey = stack.getDescriptionId() + ".tooltip.desc";
        if (I18n.exists(descKey)) {
            tooltip.add(Component.literal(I18n.get(descKey)).withStyle(ChatFormatting.DARK_GRAY));
        }

        appendNuclearYieldNote(stack, tooltip, tier);
    }

    public static void appendNuclearYieldNote(ItemStack stack, List<Component> tooltip, NuclearTier tier) {
        if (!Screen.hasShiftDown()) {
            return;
        }
        String key = stack.getDescriptionId() + ".tooltip.nuclear";
        if (!I18n.exists(key)) {
            return;
        }
        tooltip.add(Component.literal(I18n.get(
                key,
                tier.yieldKilotons(),
                (int) tier.blastRadius(),
                (int) tier.radiationRadius(),
                tier.radiationDurationTicks() / 20
        )).withStyle(ChatFormatting.GOLD));
    }
}
