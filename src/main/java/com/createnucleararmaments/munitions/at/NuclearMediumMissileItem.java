package com.createnucleararmaments.munitions.at;

import com.createnucleararmaments.index.CNAMediumMissiles;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.medium_he_rocket.HEMediumRocketItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NuclearMediumMissileItem extends HEMediumRocketItem {
    public NuclearMediumMissileItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractMediumRocket getAutocannonProjectile(ItemStack stack, Level level) {
        return new NuclearMediumMissile(CNAMediumMissiles.NUCLEAR_ROCKET_T3_ENTITY.get(), level);
    }

    @Override
    public EntityType<?> getEntityType(ItemStack stack) {
        return CNAMediumMissiles.NUCLEAR_ROCKET_T3_ENTITY.get();
    }
}
