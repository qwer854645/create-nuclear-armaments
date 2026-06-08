package com.createnucleararmaments.munitions.big_cannon;

import com.createnucleararmaments.index.CNABlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.he_shell.HEShellBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.he_shell.HEShellProjectile;

import java.util.function.Supplier;

public class NuclearShellBlock extends HEShellBlock {
    private final Supplier<? extends EntityType<? extends NuclearShellProjectile>> entityType;

    public NuclearShellBlock(Properties properties, Supplier<? extends EntityType<? extends NuclearShellProjectile>> entityType) {
        super(properties);
        this.entityType = entityType;
    }

    @Override
    public EntityType<? extends HEShellProjectile> getAssociatedEntityType() {
        return entityType.get();
    }

    @Override
    public void detonateProjectileOnTheSpot(Level level, BlockPos pos, BlockState state, Direction direction) {
    }

    @Override
    public BlockEntityType<? extends FuzedBlockEntity> getBlockEntityType() {
        return CNABlockEntities.FUZED_BLOCK.get();
    }
}
