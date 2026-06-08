package com.createnucleararmaments.index;

import com.createnucleararmaments.CNArmaments;
import com.createnucleararmaments.munitions.NuclearTier;
import com.createnucleararmaments.munitions.at.NuclearMediumMissile;
import com.createnucleararmaments.munitions.at.NuclearMediumMissileItem;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.MediumRocketRenderer;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.entity.MobCategory;
import rbasamoyai.createbigcannons.multiloader.EntityTypeConfigurator;

/**
 * Strategic tier nuclear delivery uses CBC Advanced Technology medium rocket rails (missile),
 * not Military Supplement projectile racks.
 */
public final class CNAMediumMissiles {
    public static EntityEntry<NuclearMediumMissile> NUCLEAR_ROCKET_T3_ENTITY;
    public static ItemEntry<NuclearMediumMissileItem> NUCLEAR_ROCKET_T3_ITEM;

    private CNAMediumMissiles() {
    }

    public static void register() {
        NUCLEAR_ROCKET_T3_ENTITY = CNArmaments.REGISTRATE
                .entity("nuclear_rocket_t3", NuclearMediumMissile::new, MobCategory.MISC)
                .properties(b -> EntityTypeConfigurator.of(b)
                        .size(0.6f, 0.6f)
                        .fireImmune()
                        .updateInterval(1)
                        .updateVelocity(true)
                        .trackingRange(128))
                .renderer(() -> MediumRocketRenderer::new)
                .tag(CNATags.PRECISE_MOTION)
                .onRegister(type -> NuclearTier.T3.bindEntity(type))
                .register();

        NUCLEAR_ROCKET_T3_ITEM = CNArmaments.REGISTRATE
                .item("nuclear_rocket_t3", NuclearMediumMissileItem::new)
                .lang("Strategic Nuclear Missile")
                .register();
    }
}
