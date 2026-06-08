package com.createnucleararmaments.munitions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

/**
 * Placed munition blocks stay inert; nuclear yield is only available to fired projectiles.
 */
public class NuclearFuzedBlockEntity extends FuzedBlockEntity {
    public NuclearFuzedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void detonate() {
    }
}
