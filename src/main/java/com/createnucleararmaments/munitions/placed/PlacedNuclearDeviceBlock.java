package com.createnucleararmaments.munitions.placed;

import com.createnucleararmaments.munitions.NuclearTier;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class PlacedNuclearDeviceBlock extends Block {
    public static final MapCodec<PlacedNuclearDeviceBlock> CODEC = simpleCodec(PlacedNuclearDeviceBlock::new);

    private final NuclearTier tier;

    public PlacedNuclearDeviceBlock(Properties properties, NuclearTier tier) {
        super(properties);
        this.tier = tier;
    }

    private PlacedNuclearDeviceBlock(Properties properties) {
        this(properties, NuclearTier.T1);
    }

    public NuclearTier tier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) {
        if (level.hasNeighborSignal(pos)) {
            activate(level, pos, state);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.hasNeighborSignal(pos)) {
            activate(level, pos, state);
        }
    }

    public static void activate(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide || !(state.getBlock() instanceof PlacedNuclearDeviceBlock deviceBlock)) {
            return;
        }
        PrimedNuclearCharge primed = new PrimedNuclearCharge(
                level,
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D,
                deviceBlock.tier()
        );
        level.addFreshEntity(primed);
        level.removeBlock(pos, false);
    }
}
