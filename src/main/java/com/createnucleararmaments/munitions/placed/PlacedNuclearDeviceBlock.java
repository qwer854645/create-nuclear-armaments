package com.createnucleararmaments.munitions.placed;

import com.createnucleararmaments.munitions.NuclearTier;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        // Block is already gone here — prime from this instance's tier (TNT / creeper / etc.).
        primeAt(level, pos, PrimedNuclearCharge.BLAST_CHAIN_FUSE_TICKS);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (tryIgniteFromBlast(level, pos, state)) {
            return;
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return false;
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, Direction direction, @Nullable LivingEntity igniter) {
        // Ignore flint-and-steel, fire, and lava ignition; redstone and explosions only.
    }

    public static void activate(Level level, BlockPos pos, BlockState state) {
        activate(level, pos, state, PrimedNuclearCharge.FUSE_TICKS);
    }

    public static void activate(Level level, BlockPos pos, BlockState state, int fuseTicks) {
        if (level.isClientSide || !(state.getBlock() instanceof PlacedNuclearDeviceBlock deviceBlock)) {
            return;
        }
        deviceBlock.primeAt(level, pos, fuseTicks);
    }

    /** Returns true if the block was a placed charge and is now primed with a short blast-chain fuse. */
    public static boolean tryIgniteFromBlast(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof PlacedNuclearDeviceBlock deviceBlock)) {
            return false;
        }
        deviceBlock.primeAt(level, pos, PrimedNuclearCharge.BLAST_CHAIN_FUSE_TICKS);
        return true;
    }

    private void primeAt(Level level, BlockPos pos, int fuseTicks) {
        if (level.isClientSide) {
            return;
        }
        PrimedNuclearCharge primed = new PrimedNuclearCharge(
                level,
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D,
                tier,
                fuseTicks
        );
        level.addFreshEntity(primed);
        if (level.getBlockState(pos).is(this)) {
            level.removeBlock(pos, false);
        }
    }
}
