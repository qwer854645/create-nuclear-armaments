package com.createnucleararmaments.munitions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Scans the crater rim shell for solid blocks, then spreads fractures outward across ticks:
 * capped real {@code level.explode()} bursts plus cheaper particle/block breaks.
 */
public final class EdgeFractureScheduler {
    private static final float SHELL_INNER = 1.02F;
    private static final float SHELL_OUTER = 1.40F;
    private static final float EXPOSED_FRACTURE_CHANCE = 0.75F;
    private static final float BURIED_FRACTURE_CHANCE = 0.25F;
    private static final float MAX_BREAK_RESISTANCE = 1200.0F;
    private static final int BLOCK_UPDATE_FLAGS = Block.UPDATE_CLIENTS;
    private static final int REAL_EXPLODES_PER_TICK = 40;
    private static final int VISUAL_FRACTURES_PER_TICK = 90;
    private static final float VISUAL_CAP_MULTIPLIER = 4.0F;
    private static final int REAL_EXPLODE_SOUND_INTERVAL = 20;

    private static final List<Job> JOBS = new ArrayList<>();

    private EdgeFractureScheduler() {
    }

    public static void schedule(ServerLevel level, Vec3 center, NuclearTier tier, float blastRadius) {
        RandomSource random = level.getRandom();
        List<BlockPos> candidates = scanFractureCandidates(level, center, blastRadius, random);
        if (candidates.isEmpty()) {
            return;
        }

        sortByDistanceFromCenter(candidates, center);

        int realCap = realExplodeCap(tier);
        int visualCap = Mth.floor(realCap * VISUAL_CAP_MULTIPLIER);
        float explodePower = 2.5F + tier.tier() * 0.8F;

        Deque<Vec3> realExplodes = new ArrayDeque<>();
        Deque<BlockPos> visualFractures = new ArrayDeque<>();
        Set<BlockPos> visualQueued = new HashSet<>();

        for (BlockPos pos : candidates) {
            if (realExplodes.size() < realCap) {
                realExplodes.add(Vec3.atCenterOf(pos));
            } else if (visualFractures.size() < visualCap) {
                enqueueVisual(pos, visualFractures, visualQueued);
            } else {
                break;
            }
        }

        JOBS.add(new Job(level, realExplodes, visualFractures, visualQueued, visualCap, explodePower, random));
    }

    public static void tick(ServerLevel level) {
        Iterator<Job> iterator = JOBS.iterator();
        while (iterator.hasNext()) {
            Job job = iterator.next();
            if (job.level != level) {
                continue;
            }
            if (job.advance()) {
                iterator.remove();
            }
        }
    }

    private static int realExplodeCap(NuclearTier tier) {
        return switch (tier) {
            case T1 -> 120;
            case T2 -> 220;
            case T3 -> 350;
        };
    }

    private static List<BlockPos> scanFractureCandidates(
            ServerLevel level,
            Vec3 center,
            float blastRadius,
            RandomSource random
    ) {
        float innerRadius = blastRadius * SHELL_INNER;
        float outerRadius = blastRadius * SHELL_OUTER;
        double innerSq = innerRadius * innerRadius;
        double outerSq = outerRadius * outerRadius;
        int bound = Mth.ceil(outerRadius);

        BlockPos core = BlockPos.containing(center);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        List<BlockPos> candidates = new ArrayList<>();

        for (int ox = -bound; ox <= bound; ox++) {
            for (int oy = -bound; oy <= bound; oy++) {
                for (int oz = -bound; oz <= bound; oz++) {
                    double dx = core.getX() + ox + 0.5D - center.x;
                    double dy = core.getY() + oy + 0.5D - center.y;
                    double dz = core.getZ() + oz + 0.5D - center.z;
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq < innerSq || distSq > outerSq) {
                        continue;
                    }

                    mutable.set(core.getX() + ox, core.getY() + oy, core.getZ() + oz);
                    BlockState state = level.getBlockState(mutable);
                    if (!canBreak(state, level, mutable)) {
                        continue;
                    }

                    float chance = isExposed(level, mutable) ? EXPOSED_FRACTURE_CHANCE : BURIED_FRACTURE_CHANCE;
                    if (random.nextFloat() > chance) {
                        continue;
                    }
                    candidates.add(mutable.immutable());
                }
            }
        }
        return candidates;
    }

    private static boolean isExposed(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            neighbor.setWithOffset(pos, direction);
            BlockState state = level.getBlockState(neighbor);
            if (state.isAir()) {
                return true;
            }
            FluidState fluid = level.getFluidState(neighbor);
            if (!fluid.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void sortByDistanceFromCenter(List<BlockPos> candidates, Vec3 center) {
        candidates.sort(Comparator.comparingDouble(pos -> distanceSquared(center, pos)));
    }

    private static double distanceSquared(Vec3 center, BlockPos pos) {
        double dx = pos.getX() + 0.5D - center.x;
        double dy = pos.getY() + 0.5D - center.y;
        double dz = pos.getZ() + 0.5D - center.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static void enqueueVisual(BlockPos pos, Deque<BlockPos> queue, Set<BlockPos> queued) {
        if (queued.add(pos)) {
            queue.addLast(pos);
        }
    }

    private static boolean canBreak(BlockState state, ServerLevel level, BlockPos pos) {
        if (state.isAir()) {
            return false;
        }
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        return state.getBlock().getExplosionResistance() < MAX_BREAK_RESISTANCE;
    }

    private static final class Job {
        private final ServerLevel level;
        private final Deque<Vec3> realExplodes;
        private final Deque<BlockPos> visualFractures;
        private final Set<BlockPos> visualQueued;
        private final int visualCap;
        private final float explodePower;
        private final RandomSource random;

        private int realExplodesSinceSound;
        private int visualsUntilSound;

        private Job(
                ServerLevel level,
                Deque<Vec3> realExplodes,
                Deque<BlockPos> visualFractures,
                Set<BlockPos> visualQueued,
                int visualCap,
                float explodePower,
                RandomSource random
        ) {
            this.level = level;
            this.realExplodes = realExplodes;
            this.visualFractures = visualFractures;
            this.visualQueued = visualQueued;
            this.visualCap = visualCap;
            this.explodePower = explodePower;
            this.random = random;
            this.visualsUntilSound = 4 + random.nextInt(3);
        }

        private boolean advance() {
            int budget = REAL_EXPLODES_PER_TICK;
            while (budget-- > 0 && !realExplodes.isEmpty()) {
                Vec3 pos = realExplodes.removeFirst();
                level.explode(null, pos.x, pos.y, pos.z, explodePower, false, Level.ExplosionInteraction.TNT);
                enqueueNeighborVisuals(BlockPos.containing(pos.x, pos.y, pos.z));

                realExplodesSinceSound++;
                if (realExplodesSinceSound >= REAL_EXPLODE_SOUND_INTERVAL) {
                    playFractureSound(pos.x, pos.y, pos.z, 0.55F, 0.85F);
                    realExplodesSinceSound = 0;
                }
            }

            budget = VISUAL_FRACTURES_PER_TICK;
            while (budget-- > 0 && !visualFractures.isEmpty()) {
                applyVisualFracture(visualFractures.removeFirst());
            }

            return realExplodes.isEmpty() && visualFractures.isEmpty();
        }

        private void enqueueNeighborVisuals(BlockPos origin) {
            if (visualFractures.size() >= visualCap) {
                return;
            }
            for (Direction direction : Direction.values()) {
                if (visualFractures.size() >= visualCap) {
                    return;
                }
                BlockPos neighbor = origin.relative(direction);
                if (!visualQueued.add(neighbor)) {
                    continue;
                }
                BlockState state = level.getBlockState(neighbor);
                if (canBreak(state, level, neighbor)) {
                    visualFractures.addLast(neighbor);
                } else {
                    visualQueued.remove(neighbor);
                }
            }
        }

        private void applyVisualFracture(BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            if (!canBreak(state, level, pos)) {
                return;
            }

            double x = pos.getX() + 0.5D;
            double y = pos.getY() + 0.5D;
            double z = pos.getZ() + 0.5D;

            level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 3, 0.18D, 0.18D, 0.18D, 0.0D);
            level.sendParticles(ParticleTypes.POOF, x, y, z, 6, 0.22D, 0.22D, 0.22D, 0.012D);
            level.sendParticles(ParticleTypes.SMOKE, x, y, z, 4, 0.25D, 0.25D, 0.25D, 0.008D);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), x, y, z, 8, 0.25D, 0.25D, 0.25D, 0.02D);
            level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), x, y + 0.25D, z, 3, 0.15D, 0.1D, 0.15D, 0.01D);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), BLOCK_UPDATE_FLAGS);

            visualsUntilSound--;
            if (visualsUntilSound <= 0) {
                playFractureSound(x, y, z, 0.35F, 0.95F + random.nextFloat() * 0.15F);
                visualsUntilSound = 4 + random.nextInt(3);
            }
        }

        private void playFractureSound(double x, double y, double z, float volume, float pitch) {
            level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, volume, pitch);
        }
    }
}
