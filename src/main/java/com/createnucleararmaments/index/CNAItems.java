package com.createnucleararmaments.index;

import com.createnucleararmaments.CNArmaments;
import com.createnucleararmaments.compat.CreateCompat;
import com.createnucleararmaments.compat.CreateNuclearBridge;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

/**
 * Nuclear feedstock items. Only registered when Create Nuclear is present.
 */
public final class CNAItems {
    public static ItemEntry<Item> HEU_COMPOUND;
    public static ItemEntry<Item> FISSILE_PRECURSOR;
    public static ItemEntry<Item> ARMAMENT_URANIUM_BILLET;

    private CNAItems() {
    }

    public static void register() {
        if (!CreateCompat.isLoaded() || !CreateNuclearBridge.isLoaded()) {
            return;
        }

        HEU_COMPOUND = CNArmaments.REGISTRATE
                .item("heu_compound", Item::new)
                .lang("Highly Enriched Uranium Compound")
                .register();

        FISSILE_PRECURSOR = CNArmaments.REGISTRATE
                .item("fissile_precursor", Item::new)
                .lang("Fissile Precursor Billet")
                .register();

        ARMAMENT_URANIUM_BILLET = CNArmaments.REGISTRATE
                .item("armament_uranium_billet", Item::new)
                .lang("Armament Uranium Billet")
                .register();
    }

    public static boolean materialsRegistered() {
        return ARMAMENT_URANIUM_BILLET != null;
    }
}
