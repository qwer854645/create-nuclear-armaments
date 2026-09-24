package com.createnucleararmaments.index;

import com.createnucleararmaments.CNArmaments;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

/**
 * Nuclear feedstock items. Always registered (Create is a hard dependency).
 * Crafting paths differ: Create Nuclear / New Age sequenced recipes, or a Create-only
 * nether-star fallback when no nuclear-material mods are present.
 */
public final class CNAItems {
    public static ItemEntry<Item> SUPER_ENRICHED_URANIUM_BILLET;
    public static ItemEntry<SequencedAssemblyItem> INCOMPLETE_SUPER_ENRICHED_URANIUM_BILLET;

    private CNAItems() {
    }

    public static void register() {
        SUPER_ENRICHED_URANIUM_BILLET = CNArmaments.REGISTRATE
                .item("super_enriched_uranium_billet", Item::new)
                .lang("Super Enriched Uranium Billet")
                .register();

        INCOMPLETE_SUPER_ENRICHED_URANIUM_BILLET = CNArmaments.REGISTRATE
                .item("incomplete_super_enriched_uranium_billet", SequencedAssemblyItem::new)
                .lang("Incomplete Super Enriched Uranium Billet")
                .register();
    }

    public static boolean materialsRegistered() {
        return SUPER_ENRICHED_URANIUM_BILLET != null;
    }
}
