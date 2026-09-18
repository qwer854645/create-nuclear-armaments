package com.createnucleararmaments;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Shared CreateRegistrate + ids. Create is a hard dependency.
 */
public final class CNArmaments {
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateNuclearArmaments.MOD_ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));

    private CNArmaments() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateNuclearArmaments.MOD_ID, path);
    }

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }
}
