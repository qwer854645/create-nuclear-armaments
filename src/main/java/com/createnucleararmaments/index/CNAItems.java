package com.createnucleararmaments.index;

import com.createnucleararmaments.CNArmaments;
import com.createnucleararmaments.compat.CreateNewAgeBridge;
import com.createnucleararmaments.compat.CreateNuclearBridge;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

/**
 * Nuclear feedstock items. Registered when Create Nuclear and/or Create: New Age is present.
 */
public final class CNAItems {
    public static ItemEntry<Item> HEU_COMPOUND;

    private CNAItems() {
    }

    public static void register() {
        if (!CreateNuclearBridge.isLoaded() && !CreateNewAgeBridge.isLoaded()) {
            return;
        }

        HEU_COMPOUND = CNArmaments.REGISTRATE
                .item("heu_compound", Item::new)
                .lang("Highly Enriched Uranium Compound")
                .register();
    }

    public static boolean materialsRegistered() {
        return HEU_COMPOUND != null;
    }
}
