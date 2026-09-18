package com.createnucleararmaments;

import com.tterrag.registrate.Registrate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Shared Registrate + ids. Uses plain Registrate so Create is not required at classload time.
 */
public final class CNArmaments {
    public static final Registrate REGISTRATE = Registrate.create(CreateNuclearArmaments.MOD_ID);

    private CNArmaments() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateNuclearArmaments.MOD_ID, path);
    }

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }
}
