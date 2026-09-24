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
    public static ItemEntry<Item> SUPER_ENRICHED_URANIUM_BILLET;

    private CNAItems() {
    }

    public static void register() {
        if (!CreateNuclearBridge.isLoaded() && !CreateNewAgeBridge.isLoaded()) {
            return;
        }

        SUPER_ENRICHED_URANIUM_BILLET = CNArmaments.REGISTRATE
                .item("super_enriched_uranium_billet", Item::new)
                .lang("Super Enriched Uranium Billet")
                .register();
    }

    public static boolean materialsRegistered() {
        return SUPER_ENRICHED_URANIUM_BILLET != null;
    }
}
