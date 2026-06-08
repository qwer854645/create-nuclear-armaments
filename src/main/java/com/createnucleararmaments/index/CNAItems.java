package com.createnucleararmaments.index;

import com.createnucleararmaments.CNArmaments;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

public final class CNAItems {
    public static final ItemEntry<Item> HEU_COMPOUND = CNArmaments.REGISTRATE
            .item("heu_compound", Item::new)
            .lang("Highly Enriched Uranium Compound")
            .register();

    public static final ItemEntry<Item> FISSILE_PRECURSOR = CNArmaments.REGISTRATE
            .item("fissile_precursor", Item::new)
            .lang("Fissile Precursor Billet")
            .register();

    public static final ItemEntry<Item> ARMAMENT_URANIUM_BILLET = CNArmaments.REGISTRATE
            .item("armament_uranium_billet", Item::new)
            .lang("Armament Uranium Billet")
            .register();

    private CNAItems() {
    }

    public static void register() {
    }
}
