package com.createnucleararmaments.munitions;

import com.createnucleararmaments.compat.CreateNuclearBridge;
import com.createnucleararmaments.config.CNAConfig;
import com.createnucleararmaments.index.CNATags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Replaces blocks inside the radiation sphere after a nuclear detonation.
 * <p>
 * Each in-range column is scanned from {@code core.y - bound} through {@code core.y + bound};
 * {@link Job#applyFalloutAt} then applies the spherical melt/radiation tests per block.
 */
public final class FalloutVegetationScheduler {
    private static final int BLOCK_UPDATE_FLAGS = Block.UPDATE_CLIENTS;
    private static final int DEFAULT_COLUMNS_PER_TICK = 4096;
    private static final int STONE_FALLOUT_ROLL_RANGE = 1000;
    private static final int STONE_LAVA_THRESHOLD = 8;
    private static final int STONE_AIR_THRESHOLD = 38;
    /** Weights for enriched soul soil, soul soil, coarse dirt, dirt (left to right, increasing). */
    private static final int[] SOIL_REPLACEMENT_WEIGHTS = {1, 19, 39, 141};
    /** Weights for dead bush, air (left to right, increasing). */
    private static final int[] PLANT_REPLACEMENT_WEIGHTS = {4, 6};
    /** Weights for vitrify, unchanged sand-family block. */
    private static final int[] SAND_VITRIFY_WEIGHTS = {3, 17};
    /** Weights for light green stained glass, black stained glass. */
    private static final int[] SAND_GLASS_WEIGHTS = {9, 1};
    /** Weights for air, coal block, unchanged log/wood. */
    private static final int[] LOG_REPLACEMENT_WEIGHTS = {3, 2, 7};
    /** Weights for Create scoria (slag), unchanged ore. */
    private static final int[] ORE_REPLACEMENT_WEIGHTS = {17, 3};
    private static final int TERRACOTTA_FALLOUT_ROLL_RANGE = 1000;
    private static final int TERRACOTTA_BLACK_GLASS_THRESHOLD = 10;
    private static final int TERRACOTTA_BROWN_GLASS_THRESHOLD = 20;
    private static final int TERRACOTTA_AIR_THRESHOLD = 35;
    private static final ResourceLocation CREATE_SCORIA_ID = ResourceLocation.fromNamespaceAndPath("create", "scoria");
    private static final BlockState CREATE_SCORIA_STATE = BuiltInRegistries.BLOCK.get(CREATE_SCORIA_ID).defaultBlockState();

    private static final float MELT_RADIUS_BLAST_MULTIPLIER = 1.0F;

    private static final List<Job> JOBS = new ArrayList<>();

    private FalloutVegetationScheduler() {
    }

    public static void clearAll() {
        JOBS.clear();
    }

    public static void clearLevel(ServerLevel level) {
        JOBS.removeIf(job -> job.level == level);
    }

    public static void schedule(ServerLevel level, Vec3 center, NuclearTier tier) {
        float radiationRadius = tier.radiationRadius();
        float meltRadius = radiationRadius + tier.blastRadius() * MELT_RADIUS_BLAST_MULTIPLIER;
        BlockPos core = BlockPos.containing(center);
        Job job = new Job(level, core, radiationRadius, meltRadius);
        JOBS.add(job);
    }

    public static void tick(ServerLevel level) {
        Iterator<Job> iterator = JOBS.iterator();
        while (iterator.hasNext()) {
            Job job = iterator.next();
            if (job.level != level) {
                continue;
            }
            if (job.tickScan()) {
                iterator.remove();
            }
        }
    }

    private static boolean isFalloutSoilSource(BlockState state) {
        return state.is(CNATags.FALLOUT_SOILS)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.NYLIUM)
                || state.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || state.is(BlockTags.SNIFFER_DIGGABLE_BLOCK)
                || state.is(CNATags.C_DIRT)
                || state.is(CNATags.C_GRASS)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT_PATH)
                || state.is(Blocks.FARMLAND)
                || state.is(Blocks.MUD)
                || state.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || state.is(Blocks.MOSS_BLOCK);
    }

    private static boolean isFalloutPlantSource(BlockState state) {
        return !state.isAir()
                && !state.is(Blocks.DEAD_BUSH)
                && (state.is(CNATags.FALLOUT_PLANTS)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS)
                || state.is(BlockTags.REPLACEABLE_BY_TREES)
                || state.is(CNATags.C_FLOWERS)
                || state.is(CNATags.C_CROPS));
    }

    private static boolean isFalloutLeafSource(BlockState state) {
        return state.is(CNATags.FALLOUT_LEAVES)
                || state.is(BlockTags.LEAVES)
                || state.is(CNATags.C_LEAVES);
    }

    private static boolean isFalloutSandSource(BlockState state) {
        return state.is(BlockTags.SAND)
                || state.is(CNATags.FALLOUT_SANDS)
                || state.is(CNATags.C_SANDS);
    }

    private static boolean isFalloutStoneSource(BlockState state) {
        return !state.isAir()
                && !state.is(Blocks.LAVA)
                && (state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(CNATags.FALLOUT_STONES)
                || state.is(CNATags.C_STONES));
    }

    private static boolean isFalloutIceAndSnowSource(BlockState state) {
        return state.is(CNATags.FALLOUT_ICE_AND_SNOW)
                || state.is(BlockTags.SNOW)
                || state.is(CNATags.C_ICES)
                || state.is(CNATags.C_SNOW_BLOCKS)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.FROSTED_ICE);
    }

    private static boolean isFalloutLogSource(BlockState state) {
        return state.is(CNATags.FALLOUT_LOGS)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.OVERWORLD_NATURAL_LOGS)
                || state.is(CNATags.C_LOGS)
                || state.is(CNATags.C_STRIPPED_LOGS);
    }

    private static boolean isFalloutTerracottaSource(BlockState state) {
        return state.is(CNATags.FALLOUT_TERRACOTTA);
    }

    private static boolean isFalloutOreSource(BlockState state) {
        return state.is(CNATags.FALLOUT_ORES)
                || state.is(CNATags.C_ORES)
                || state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES)
                || state.is(Blocks.NETHER_QUARTZ_ORE)
                || state.is(Blocks.NETHER_GOLD_ORE)
                || state.is(Blocks.ANCIENT_DEBRIS);
    }

    private static BlockState pickFalloutSoil(RandomSource random) {
        return pickWeightedBlockState(
                random,
                SOIL_REPLACEMENT_WEIGHTS,
                CreateNuclearBridge.enrichedSoulSoilOrFallback(),
                Blocks.SOUL_SOIL.defaultBlockState(),
                Blocks.COARSE_DIRT.defaultBlockState(),
                Blocks.DIRT.defaultBlockState()
        );
    }

    private static BlockState pickFalloutPlant(RandomSource random) {
        return pickWeightedBlockState(
                random,
                PLANT_REPLACEMENT_WEIGHTS,
                Blocks.DEAD_BUSH.defaultBlockState(),
                Blocks.AIR.defaultBlockState()
        );
    }

    /**
     * @return replacement state, or {@code null} to leave the block unchanged
     */
    private static BlockState pickFalloutSandReplacement(RandomSource random) {
        int vitrifyTotal = SAND_VITRIFY_WEIGHTS[0] + SAND_VITRIFY_WEIGHTS[1];
        if (random.nextInt(vitrifyTotal) >= SAND_VITRIFY_WEIGHTS[0]) {
            return null;
        }

        return pickWeightedBlockState(
                random,
                SAND_GLASS_WEIGHTS,
                Blocks.LIME_STAINED_GLASS.defaultBlockState(),
                Blocks.BLACK_STAINED_GLASS.defaultBlockState()
        );
    }

    /**
     * @return replacement state, or {@code null} to leave the block unchanged
     */
    private static BlockState rollFalloutStoneReplacement(RandomSource random) {
        int roll = random.nextInt(STONE_FALLOUT_ROLL_RANGE);
        if (roll < STONE_LAVA_THRESHOLD) {
            return Blocks.LAVA.defaultBlockState();
        }
        if (roll < STONE_AIR_THRESHOLD) {
            return Blocks.AIR.defaultBlockState();
        }
        return null;
    }

    /**
     * @return replacement state, or {@code null} to leave the block unchanged
     */
    private static BlockState rollFalloutTerracottaReplacement(RandomSource random) {
        int roll = random.nextInt(TERRACOTTA_FALLOUT_ROLL_RANGE);
        if (roll < TERRACOTTA_BLACK_GLASS_THRESHOLD) {
            return Blocks.BLACK_STAINED_GLASS.defaultBlockState();
        }
        if (roll < TERRACOTTA_BROWN_GLASS_THRESHOLD) {
            return Blocks.BROWN_STAINED_GLASS.defaultBlockState();
        }
        if (roll < TERRACOTTA_AIR_THRESHOLD) {
            return Blocks.AIR.defaultBlockState();
        }
        return null;
    }

    /**
     * @return replacement state, or {@code null} to leave the block unchanged
     */
    private static BlockState rollFalloutOreReplacement(RandomSource random) {
        int totalWeight = ORE_REPLACEMENT_WEIGHTS[0] + ORE_REPLACEMENT_WEIGHTS[1];
        if (random.nextInt(totalWeight) >= ORE_REPLACEMENT_WEIGHTS[0]) {
            return null;
        }
        return CREATE_SCORIA_STATE;
    }

    /**
     * @return replacement state, or {@code null} to leave the block unchanged
     */
    private static BlockState rollFalloutLogReplacement(RandomSource random) {
        int totalWeight = 0;
        for (int weight : LOG_REPLACEMENT_WEIGHTS) {
            totalWeight += weight;
        }

        int roll = random.nextInt(totalWeight);
        if (roll < LOG_REPLACEMENT_WEIGHTS[0]) {
            return Blocks.AIR.defaultBlockState();
        }
        if (roll < LOG_REPLACEMENT_WEIGHTS[0] + LOG_REPLACEMENT_WEIGHTS[1]) {
            return Blocks.COAL_BLOCK.defaultBlockState();
        }
        return null;
    }

    private static BlockState pickWeightedBlockState(RandomSource random, int[] weights, BlockState... options) {
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < options.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) {
                return options[i];
            }
        }

        return options[options.length - 1];
    }

    private static void replaceFalloutPlant(ServerLevel level, BlockPos pos, BlockState state, BlockState replacement) {
        clearDoublePlantPartner(level, pos, state);
        level.setBlock(pos, replacement, BLOCK_UPDATE_FLAGS);
    }

    private static void clearDoublePlantPartner(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return;
        }

        BlockPos partnerPos = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState partnerState = level.getBlockState(partnerPos);
        if (partnerState.is(state.getBlock())) {
            level.setBlock(partnerPos, Blocks.AIR.defaultBlockState(), BLOCK_UPDATE_FLAGS);
        }
    }

    private static boolean clearFalloutWater(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.WATER) || state.is(Blocks.BUBBLE_COLUMN)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), BLOCK_UPDATE_FLAGS);
            return true;
        }

        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), BLOCK_UPDATE_FLAGS);
            return true;
        }

        FluidState fluid = state.getFluidState();
        if (fluid.is(FluidTags.WATER)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), BLOCK_UPDATE_FLAGS);
            return true;
        }

        return false;
    }

    private static boolean clearFalloutIce(ServerLevel level, BlockPos pos, BlockState state) {
        if (!isFalloutIceAndSnowSource(state)) {
            return false;
        }
        // Same as water: ice/snow always vanishes to air.
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), BLOCK_UPDATE_FLAGS);
        return true;
    }

    private static void meltFalloutIce(ServerLevel level, BlockPos pos, BlockState state) {
        if (!isFalloutIceAndSnowSource(state)) {
            return;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), BLOCK_UPDATE_FLAGS);
    }

    private static final class Job {
        private final ServerLevel level;
        private final BlockPos core;
        private final double radiationRadiusSq;
        private final double meltRadiusSq;
        private final int bound;
        private final int yMin;
        private final int yMax;
        private boolean columnPassComplete;
        private int columnOx;
        private int columnOz;
        private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        private Job(ServerLevel level, BlockPos core, float radiationRadius, float meltRadius) {
            this.level = level;
            this.core = core;
            this.radiationRadiusSq = radiationRadius * radiationRadius;
            this.meltRadiusSq = meltRadius * meltRadius;
            this.bound = Mth.ceil(meltRadius);
            this.yMin = level.getMinBuildHeight();
            this.yMax = level.getMaxBuildHeight() - 1;
            this.columnOx = -bound;
            this.columnOz = -bound;
        }

        private boolean tickScan() {
            if (columnPassComplete) {
                return true;
            }

            RandomSource random = level.getRandom();
            int columnsPerTick = columnsPerTick();
            for (int i = 0; i < columnsPerTick && !columnPassComplete; i++) {
                processColumn(columnOx, columnOz, random);
                advanceColumnCursor();
            }

            return columnPassComplete;
        }

        /**
         * Scan every Y level in the melt sphere's bounding column for this (ox, oz) offset.
         * Horizontal filtering matches the sphere footprint; per-block {@code distSq} is checked again later.
         */
        private void processColumn(int offsetX, int offsetZ, RandomSource random) {
            double horizontalDistSq = (double) offsetX * offsetX + (double) offsetZ * offsetZ;
            if (horizontalDistSq > meltRadiusSq) {
                return;
            }

            int worldX = core.getX() + offsetX;
            int worldZ = core.getZ() + offsetZ;
            mutable.set(worldX, core.getY(), worldZ);
            if (!level.isLoaded(mutable)) {
                return;
            }

            int columnMinY = Math.max(yMin, core.getY() - bound);
            int columnMaxY = Math.min(yMax, core.getY() + bound);
            for (int y = columnMinY; y <= columnMaxY; y++) {
                mutable.set(worldX, y, worldZ);
                applyFalloutAt(mutable, random);
            }
        }

        private void advanceColumnCursor() {
            columnOx++;
            if (columnOx <= bound) {
                return;
            }
            columnOx = -bound;
            columnOz++;
            if (columnOz > bound) {
                columnPassComplete = true;
            }
        }

        private void applyFalloutAt(BlockPos pos, RandomSource random) {
            double distSq = distanceSqFromCore(pos);
            if (distSq > meltRadiusSq) {
                return;
            }

            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                return;
            }

            if (distSq <= radiationRadiusSq) {
                applyRadiationFallout(level, pos, state, random);
            } else {
                meltFalloutIce(level, pos, state);
            }
        }

        private double distanceSqFromCore(BlockPos pos) {
            double dx = pos.getX() - core.getX();
            double dy = pos.getY() - core.getY();
            double dz = pos.getZ() - core.getZ();
            return dx * dx + dy * dy + dz * dz;
        }

        private void applyRadiationFallout(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
            if (clearFalloutWater(level, pos, state)) {
                return;
            }
            if (clearFalloutIce(level, pos, state)) {
                return;
            }
            if (isFalloutSoilSource(state)) {
                level.setBlock(pos, pickFalloutSoil(random), BLOCK_UPDATE_FLAGS);
            } else if (isFalloutSandSource(state)) {
                BlockState sandReplacement = pickFalloutSandReplacement(random);
                if (sandReplacement != null) {
                    level.setBlock(pos, sandReplacement, BLOCK_UPDATE_FLAGS);
                }
            } else if (isFalloutStoneSource(state)) {
                BlockState stoneReplacement = rollFalloutStoneReplacement(random);
                if (stoneReplacement != null) {
                    level.setBlock(pos, stoneReplacement, BLOCK_UPDATE_FLAGS);
                }
            } else if (isFalloutTerracottaSource(state)) {
                BlockState terracottaReplacement = rollFalloutTerracottaReplacement(random);
                if (terracottaReplacement != null) {
                    level.setBlock(pos, terracottaReplacement, BLOCK_UPDATE_FLAGS);
                }
            } else if (isFalloutOreSource(state)) {
                BlockState oreReplacement = rollFalloutOreReplacement(random);
                if (oreReplacement != null) {
                    level.setBlock(pos, oreReplacement, BLOCK_UPDATE_FLAGS);
                }
            } else if (isFalloutLogSource(state)) {
                BlockState logReplacement = rollFalloutLogReplacement(random);
                if (logReplacement != null) {
                    level.setBlock(pos, logReplacement, BLOCK_UPDATE_FLAGS);
                }
            } else if (isFalloutLeafSource(state)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), BLOCK_UPDATE_FLAGS);
            } else if (isFalloutPlantSource(state)) {
                replaceFalloutPlant(level, pos, state, pickFalloutPlant(random));
            }
        }

    }

    private static int columnsPerTick() {
        try {
            if (CNAConfig.SERVER_SPEC.isLoaded()) {
                return CNAConfig.SERVER.falloutColumnsPerTick.get();
            }
        } catch (Throwable ignored) {
        }
        return DEFAULT_COLUMNS_PER_TICK;
    }
}
