package com.createnucleararmaments.munitions;

import com.createnucleararmaments.munitions.placed.PlacedNuclearDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;
import rbasamoyai.createbigcannons.remix.CBCExplodableBlock;

/**
 * When a nuclear blast would clear a block, ignite chain-reactive explosives instead of deleting them.
 * Blast-chain fuses are intentionally shorter than normal priming.
 */
public final class BlastChainIgnition {
    /** ~1–2s, shorter than vanilla TNT's 4s flint fuse. */
    private static final int TNT_BLAST_FUSE_MIN = 20;
    private static final int TNT_BLAST_FUSE_RANGE = 21;
    /** Cap CBC munition explosion countdown after chain ignition (~1s). */
    private static final int CBC_BLAST_COUNTDOWN_MAX = 20;

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

        if (block instanceof CBCExplodableBlock explodable) {
            Explosion explosion = dummyExplosion(level, pos);
            explodable.createbigcannons$onBlockExplode(level, pos, state, explosion);
            if (level.getBlockState(pos).getBlock() == block) {
                level.removeBlock(pos, false);
            }
            shortenNearbyCbcCountdowns(level, pos);
            return true;
        }

        return false;
    }

    private static void shortenNearbyCbcCountdowns(ServerLevel level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(0.75D);
        for (FuzedBigCannonProjectile projectile : level.getEntitiesOfClass(FuzedBigCannonProjectile.class, area)) {
            int countdown = projectile.getExplosionCountdown();
            if (countdown > CBC_BLAST_COUNTDOWN_MAX) {
                projectile.setExplosionCountdown(CBC_BLAST_COUNTDOWN_MAX);
            }
        }
    }

    private static Explosion dummyExplosion(Level level, BlockPos pos) {
        return new Explosion(
                level,
                null,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D,
                4.0F,
                false,
                Explosion.BlockInteraction.KEEP
        );
    }
}
