package com.createnucleararmaments.munitions;

import com.createnucleararmaments.munitions.placed.PlacedNuclearDeviceBlock;
import com.createnucleararmaments.compat.CbcCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * When a nuclear blast would clear a block, ignite chain-reactive explosives instead of deleting them.
 * Blast-chain fuses are intentionally shorter than normal priming.
 */
public final class BlastChainIgnition {
    /** ~1–2s, shorter than vanilla TNT's 4s flint fuse. */
    private static final int TNT_BLAST_FUSE_MIN = 20;
    private static final int TNT_BLAST_FUSE_RANGE = 21;

    private BlastChainIgnition() {
    }

    /**
     * @return {@code true} if the block was handled as an explosive (primed / spawned) and must not be cleared as air
     */
    public static boolean tryIgnite(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return false;
        }

        Block block = state.getBlock();

        if (PlacedNuclearDeviceBlock.tryIgniteFromBlast(level, pos, state)) {
            return true;
        }

        if (block instanceof TntBlock) {
            PrimedTnt primed = new PrimedTnt(
                    level,
                    pos.getX() + 0.5D,
                    pos.getY(),
                    pos.getZ() + 0.5D,
                    null
            );
            primed.setFuse(TNT_BLAST_FUSE_MIN + level.random.nextInt(TNT_BLAST_FUSE_RANGE));
            level.addFreshEntity(primed);
            level.removeBlock(pos, false);
            return true;
        }

        return tryIgniteCbc(level, pos, state);
    }

    private static boolean tryIgniteCbc(ServerLevel level, BlockPos pos, BlockState state) {
        if (!CbcCompat.isCbcLoaded()) {
            return false;
        }
        try {
            Class<?> cls = Class.forName("com.createnucleararmaments.compat.cbc.CbcBlastChainIgnition");
            Object result = cls.getMethod("tryIgnite", ServerLevel.class, BlockPos.class, BlockState.class)
                    .invoke(null, level, pos, state);
            return result instanceof Boolean b && b;
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }
}
