package com.createnucleararmaments.munitions;

import com.createnucleararmaments.compat.CreateNewAgeBridge;
import com.createnucleararmaments.compat.CreateNuclearBridge;
import com.createnucleararmaments.compat.SableSpace;
import com.createnucleararmaments.network.CNANetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class NuclearDetonation {
    /** Outer blast shell: stops at obsidian-tier resistance (1200). */
    private static final float MAX_BREAK_RESISTANCE = 1200.0F;
    /**
     * Inner core: only hardness &lt; 0 (bedrock / barrier / command blocks) is spared,
     * so obsidian, ancient debris, reinforced deepslate, anvils, etc. are cleared.
     */
    private static final float CORE_RADIUS_FRACTION = 0.28F;
    private static final int BLOCK_UPDATE_FLAGS = Block.UPDATE_CLIENTS;
    private static final float FLUID_SOURCE_EXTRA_RADIUS = 15.0F;

    private NuclearDetonation() {
    }

    public static void detonate(Level level, Vec3 center, NuclearTier tier) {
        if (level.isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        // Global blast origin: FX, entity falloff, and the world-space volume.
        // Intersecting sub-levels get the same sphere after inverse-posing into plot space,
        // so structure blocks at world point W match world blocks at W.
        Vec3 globalCenter = SableSpace.projectOut(serverLevel, center);
        BlockPos effectPos = BlockPos.containing(globalCenter);
        float radius = tier.blastRadius();
        float fluidRadius = radius + FLUID_SOURCE_EXTRA_RADIUS;
        float radiationRadius = tier.radiationRadius();
        float volumeQueryRadius = Math.max(fluidRadius, radiationRadius);

        CNANetwork.sendMushroomCloud(serverLevel, globalCenter, tier);
        MushroomCloudParticleScheduler.schedule(serverLevel, globalCenter, tier);
        playDetonationEffects(serverLevel, globalCenter, tier, effectPos, radius);

        List<Vec3> volumeCenters = SableSpace.blastVolumeCenters(serverLevel, center, volumeQueryRadius);
        for (Vec3 volumeCenter : volumeCenters) {
            applyVolumetricEffects(serverLevel, volumeCenter, tier, radius, fluidRadius);
        }

        // Entity / lingering radiation use global distances so riders and other structures match world falloff.
        applyBlastDamage(serverLevel, globalCenter, tier, radius);
        applyInstantRadiation(serverLevel, globalCenter, tier, radius, volumeCenters);
        RadiationZoneScheduler.schedule(serverLevel, globalCenter, tier);
    }

    /**
     * Identical crater / fluid / rim / fallout pass for world space and each plot space.
     */
    private static void applyVolumetricEffects(
            ServerLevel level,
            Vec3 center,
            NuclearTier tier,
            float radius,
            float fluidRadius
    ) {
        clearSphere(level, center, radius);
        clearFluidSourcesInSphere(level, center, fluidRadius);
        EdgeFractureScheduler.schedule(level, center, tier, radius);
        FalloutVegetationScheduler.schedule(level, center, tier);
    }

    private static void playDetonationEffects(ServerLevel level, Vec3 center, NuclearTier tier, BlockPos pos, float radius) {
        float volume = 14.0F + tier.tier() * 3.0F;
        double spread = radius * 0.45D;

        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, volume, 0.25F);
        level.playSound(null, pos, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, volume * 0.65F, 0.55F);

        level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, tier.tier() + 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 24 + tier.tier() * 16, spread, spread, spread, 0.02D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, 32 + tier.tier() * 20, spread, spread, spread, 0.015D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.x, center.y, center.z, 20 + tier.tier() * 12, spread * 0.85D, spread * 0.85D, spread * 0.85D, 0.01D);
        spawnInitialGroundBurst(level, center, radius, tier.tier());
    }

    private static void spawnInitialGroundBurst(ServerLevel level, Vec3 center, float radius, int tier) {
        double groundSpread = radius * 0.55D;
        level.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                center.x,
                center.y,
                center.z,
                14 + tier * 6,
                groundSpread,
                0.35D,
                groundSpread,
                0.02D
        );
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                center.x,
                center.y,
                center.z,
                10 + tier * 4,
                groundSpread * 0.7D,
                0.25D,
                groundSpread * 0.7D,
                0.015D
        );
        if (tier >= 2) {
            level.sendParticles(
                    ParticleTypes.LAVA,
                    center.x,
                    center.y,
                    center.z,
                    6 + tier * 3,
                    groundSpread * 0.45D,
                    0.15D,
                    groundSpread * 0.45D,
                    0.012D
            );
        }
    }

    /** Removes every breakable block inside the blast sphere. */
    private static void clearSphere(ServerLevel level, Vec3 center, float radius) {
        BlockPos core = BlockPos.containing(center);
        int bound = Mth.ceil(radius);
        double radiusSq = radius * radius;
        double coreRadius = radius * CORE_RADIUS_FRACTION;
        double coreRadiusSq = coreRadius * coreRadius;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int ox = -bound; ox <= bound; ox++) {
            for (int oy = -bound; oy <= bound; oy++) {
                for (int oz = -bound; oz <= bound; oz++) {
                    double distSq = ox * ox + oy * oy + oz * oz;
                    if (distSq > radiusSq) {
                        continue;
                    }
                    mutable.set(core.getX() + ox, core.getY() + oy, core.getZ() + oz);
                    BlockState state = level.getBlockState(mutable);
                    if (BlastChainIgnition.tryIgnite(level, mutable, state)) {
                        continue;
                    }
                    boolean innerCore = distSq <= coreRadiusSq;
                    if (canBreak(state, level, mutable, innerCore)) {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), BLOCK_UPDATE_FLAGS);
                    }
                }
            }
        }
    }

    /** Removes fluid source blocks inside a sphere larger than the blast radius. */
    private static void clearFluidSourcesInSphere(ServerLevel level, Vec3 center, float radius) {
        BlockPos core = BlockPos.containing(center);
        int bound = Mth.ceil(radius);
        double radiusSq = radius * radius;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int ox = -bound; ox <= bound; ox++) {
            for (int oy = -bound; oy <= bound; oy++) {
                for (int oz = -bound; oz <= bound; oz++) {
                    if (ox * ox + oy * oy + oz * oz > radiusSq) {
                        continue;
                    }
                    mutable.set(core.getX() + ox, core.getY() + oy, core.getZ() + oz);
                    FluidState fluid = level.getFluidState(mutable);
                    if (fluid.isSource()) {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), BLOCK_UPDATE_FLAGS);
                    }
                }
            }
        }
    }

    private static boolean canBreak(BlockState state, ServerLevel level, BlockPos pos, boolean innerCore) {
        if (state.isAir()) {
            return false;
        }
        // Bedrock, barriers, command blocks, etc.
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        if (innerCore) {
            return true;
        }
        return state.getBlock().getExplosionResistance() < MAX_BREAK_RESISTANCE;
    }

    private static void applyBlastDamage(ServerLevel level, Vec3 globalCenter, NuclearTier tier, float radius) {
        float maxDamage = tier.entityExplosionPower();
        float maxKnockback = 2.5F + tier.tier() * 2.0F;
        float maxFireSeconds = 5.0F + tier.tier() * 4.0F;
        DamageSource explosionSource = level.damageSources().explosion(null, null);
        DamageSource fireSource = level.damageSources().onFire();

        SableSpace.forLivingInRadius(level, globalCenter, radius, (entity, distance) -> {
            float intensity = 1.0F - (float) (distance / radius);
            intensity *= intensity;
            float damage = maxDamage * intensity;
            if (damage > 0.5F) {
                entity.hurt(explosionSource, damage);
            }

            float thermalDamage = maxDamage * 0.45F * intensity;
            if (thermalDamage > 0.5F) {
                entity.hurt(fireSource, thermalDamage);
            }

            int fireSeconds = Mth.floor(maxFireSeconds * intensity);
            if (fireSeconds > 0) {
                entity.setRemainingFireTicks(fireSeconds * 20);
            }

            if (intensity <= 0.01F) {
                return;
            }
            Vec3 away = SableSpace.directionGlobal(level, globalCenter, entity.position());
            if (away.lengthSqr() > 1.0E-6D) {
                away = away.normalize().scale(maxKnockback * intensity);
                entity.push(away.x, 0.35D + intensity * 0.65D, away.z);
            }
        });
    }

    private static void applyInstantRadiation(
            ServerLevel level,
            Vec3 globalCenter,
            NuclearTier tier,
            float blastRadius,
            List<Vec3> volumeCenters
    ) {
        float radiationRadius = tier.radiationRadius();
        int pulseRadius = Math.max(1, (int) radiationRadius);
        for (Vec3 volumeCenter : volumeCenters) {
            CreateNewAgeBridge.burstRadiationField(level, BlockPos.containing(volumeCenter), pulseRadius);
        }
        SableSpace.forLivingInRadius(level, globalCenter, radiationRadius, (entity, distance) -> {
            double falloff = distance <= blastRadius
                    ? 1.0D - distance / blastRadius * 0.35D
                    : 1.0D - (distance - blastRadius) / (radiationRadius - blastRadius) * 0.65D;
            int duration = (int) (tier.radiationDurationTicks() * falloff) + 120;
            CreateNuclearBridge.applyFalloutEffects(entity, duration, tier.radiationAmplifier());
        });
    }
}
