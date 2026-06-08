package com.createnucleararmaments.munitions.racked;

import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.he_bomb.HEBombBlock;
import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.he_bomb.HEBombProjectile;
import com.createnucleararmaments.index.CNABlockEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

import java.util.function.Supplier;

public class NuclearBombBlock extends HEBombBlock {
    private final Supplier<? extends EntityType<? extends NuclearBombProjectile>> entityType;

    public NuclearBombBlock(Properties properties, Supplier<? extends EntityType<? extends NuclearBombProjectile>> entityType) {
        super(properties);
        this.entityType = entityType;
    }

    @Override
    public EntityType<? extends HEBombProjectile> getAssociatedEntityType() {
        return entityType.get();
    }

    @Override
    public BlockEntityType<? extends FuzedBlockEntity> getBlockEntityType() {
        return CNABlockEntities.FUZED_BLOCK.get();
    }
}
