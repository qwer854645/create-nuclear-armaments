package com.createnucleararmaments.munitions.racked;

import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.he_rocket.HERocketBlock;
import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.he_rocket.HERocketProjectile;
import com.createnucleararmaments.index.CNABlockEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

import java.util.function.Supplier;

public class NuclearRocketBlock extends HERocketBlock {
    private final Supplier<? extends EntityType<? extends NuclearRocketProjectile>> entityType;

    public NuclearRocketBlock(Properties properties, Supplier<? extends EntityType<? extends NuclearRocketProjectile>> entityType) {
        super(properties);
        this.entityType = entityType;
    }

    @Override
    public EntityType<? extends HERocketProjectile> getAssociatedEntityType() {
        return entityType.get();
    }

    @Override
    public BlockEntityType<? extends FuzedBlockEntity> getBlockEntityType() {
        return CNABlockEntities.FUZED_BLOCK.get();
    }
}
