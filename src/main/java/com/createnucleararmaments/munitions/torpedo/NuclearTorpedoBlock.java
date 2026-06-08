package com.createnucleararmaments.munitions.torpedo;

import com.cainiao1053.cbcmoreshells.munitions.torpedo_tube.short_range_torpedo.ShortRangeTorpedoBlock;
import com.cainiao1053.cbcmoreshells.munitions.torpedo_tube.short_range_torpedo.ShortRangeTorpedoProjectile;
import com.createnucleararmaments.index.CNABlockEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

import java.util.function.Supplier;

public class NuclearTorpedoBlock extends ShortRangeTorpedoBlock {
    private final Supplier<? extends EntityType<? extends NuclearTorpedoProjectile>> entityType;

    public NuclearTorpedoBlock(Properties properties, Supplier<? extends EntityType<? extends NuclearTorpedoProjectile>> entityType) {
        super(properties);
        this.entityType = entityType;
    }

    @Override
    public EntityType<? extends ShortRangeTorpedoProjectile> getAssociatedEntityType() {
        return entityType.get();
    }

    @Override
    public BlockEntityType<? extends FuzedBlockEntity> getBlockEntityType() {
        return CNABlockEntities.FUZED_BLOCK.get();
    }
}
