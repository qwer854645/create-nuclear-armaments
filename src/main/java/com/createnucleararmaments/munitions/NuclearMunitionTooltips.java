package com.createnucleararmaments.munitions;

import com.simibubi.create.foundation.item.TooltipHelper;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Extra nuclear-yield lines using Create's TooltipHelper / palette.
 * Hold-Shift chrome for registered items comes from {@link com.simibubi.create.foundation.item.ItemDescription}.
 */
public final class NuclearMunitionTooltips {
    private NuclearMunitionTooltips() {
    }

    public static void appendNuclearYieldNote(ItemStack stack, List<Component> tooltip, NuclearTier tier) {
        if (!Screen.hasShiftDown()) {
            return;
        }
        String key = stack.getDescriptionId() + ".tooltip.nuclear";
        if (!I18n.exists(key)) {
            return;
        }
        String text = I18n.get(
                key,
                tier.yieldKilotons(),
                (int) tier.blastRadius(),
                (int) tier.radiationRadius(),
                tier.radiationDurationTicks() / 20
        );
        tooltip.addAll(TooltipHelper.cutStringTextComponent(text, FontHelper.Palette.STANDARD_CREATE));
    }
}
