package com.createnucleararmaments.compat.cbc;

import com.createnucleararmaments.compat.CbcCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;
import rbasamoyai.createbigcannons.remix.CBCExplodableBlock;

/**
 * CBC-only blast chain path. Loaded only when Create Big Cannons is present.
 */
public final class CbcBlastChainIgnition {
    private static final int CBC_BLAST_COUNTDOWN_MAX = 20;

    private CbcBlastChainIgnition() {
    }

    public static boolean tryIgnite(ServerLevel level, BlockPos pos, BlockState state) {
        if (!CbcCompat.isCbcLoaded()) {
            return false;
        }
        Block block = state.getBlock();
        if (!(block instanceof CBCExplodableBlock explodable)) {
            return false;
        }
        Explosion explosion = dummyExplosion(level, pos);
        explodable.createbigcannons$onBlockExplode(level, pos, state, explosion);
        if (level.getBlockState(pos).getBlock() == block) {
            level.removeBlock(pos, false);
        }
        shortenNearbyCountdowns(level, pos);
        return true;
    }

    private static void shortenNearbyCountdowns(ServerLevel level, BlockPos pos) {
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
