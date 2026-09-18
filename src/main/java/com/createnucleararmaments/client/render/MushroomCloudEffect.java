package com.createnucleararmaments.client.render;

import com.createnucleararmaments.munitions.MushroomCloudProfile;
import com.createnucleararmaments.munitions.NuclearTier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Client nuclear cloud driven by a compact mesh silhouette plus accent particles.
 */
public final class MushroomCloudEffect {
    private final Vec3 center;
    private final MushroomCloudProfile profile;
    private final long startTick;
    private final int seed;
    private boolean flashSpawned;

    public MushroomCloudEffect(Vec3 center, MushroomCloudProfile profile, long startTick) {
        this.center = center;
        this.profile = profile;
        this.startTick = startTick;
        this.seed = MushroomCloudVariation.seedFrom(center, startTick);
    }

    public boolean matches(Vec3 otherCenter, NuclearTier otherTier, long otherStartTick) {
        if (profile.tier() != otherTier) {
            return false;
        }
        if (Math.abs(startTick - otherStartTick) > 5L) {
            return false;
        }
        return BlockPos.containing(center).equals(BlockPos.containing(otherCenter));
    }

    public Vec3 center() {
        return center;
    }

    public MushroomCloudProfile profile() {
        return profile;
    }

    public float ageTicks(long gameTime, float partialTick) {
        return (gameTime - startTick) + partialTick;
    }

    public boolean isExpired(long gameTime) {
        return gameTime > startTick + profile.endTicks() + 40L;
    }

    public float globalAlpha(float age) {
        MushroomCloudProfile profile = this.profile;
        if (age < 4.0F) {
            return Mth.clamp(age / 4.0F, 0.0F, 1.0F);
        }
        if (age >= profile.dissipateStartTick()) {
            float fade = (age - profile.dissipateStartTick())
                    / Math.max(1.0F, profile.endTicks() - profile.dissipateStartTick());
            return 1.0F - Mth.clamp(fade, 0.0F, 1.0F);
        }
        return 1.0F;
    }

    public int seed() {
        return seed;
    }

    public void tickAccentParticles(ClientLevel level, long gameTime) {
        float age = ageTicks(gameTime, 0.0F);
        if (age < 0.0F || age > profile.endTicks()) {
            return;
        }

        RandomSource random = level.getRandom();
        int tier = profile.tier().tier();

        if (!flashSpawned && age >= 0.0F) {
            flashSpawned = true;
            level.addAlwaysVisibleParticle(ParticleTypes.FLASH, center.x, center.y + 1.2D, center.z, 0.0D, 0.0D, 0.0D);
            level.addAlwaysVisibleParticle(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 0.0D, 0.0D, 0.0D);
            for (int i = 0; i < 4 + tier * 2; i++) {
                level.addAlwaysVisibleParticle(
                        ParticleTypes.EXPLOSION,
                        center.x + (random.nextDouble() - 0.5D) * 3.0D,
                        center.y + random.nextDouble() * 2.0D,
                        center.z + (random.nextDouble() - 0.5D) * 3.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }

        if (age <= profile.fireballTicks()) {
            spawnFireball(level, age, random, tier);
        }

        if (age <= profile.stemRiseTicks() + profile.capExpandTicks()) {
            spawnStemAndCap(level, age, random, tier);
        }

        if (age <= profile.shockRingEndTicks()) {
            spawnGroundRing(level, age, random, tier);
        }

        if (age >= profile.dissipateStartTick() * 0.35F && ((int) age & 1) == 0) {
            spawnAsh(level, age, random, tier);
        }
    }

    private void spawnFireball(ClientLevel level, float age, RandomSource random, int tier) {
        float life = Mth.clamp(age / profile.fireballTicks(), 0.0F, 1.0F);
        double spread = 2.5D + tier + life * (4.0D + tier);
        int count = 10 + tier * 6;
        for (int i = 0; i < count; i++) {
            double ox = (random.nextDouble() - 0.5D) * spread;
            double oy = random.nextDouble() * (2.0D + life * 5.0D);
            double oz = (random.nextDouble() - 0.5D) * spread;
            level.addAlwaysVisibleParticle(
                    ParticleTypes.FLAME,
                    center.x + ox,
                    center.y + oy,
                    center.z + oz,
                    (random.nextDouble() - 0.5D) * 0.03D,
                    0.04D + random.nextDouble() * 0.05D,
                    (random.nextDouble() - 0.5D) * 0.03D
            );
            if (random.nextFloat() < 0.25F) {
                level.addAlwaysVisibleParticle(
                        ParticleTypes.LAVA,
                        center.x + ox * 0.6D,
                        center.y + oy * 0.5D,
                        center.z + oz * 0.6D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
    }

    private void spawnStemAndCap(ClientLevel level, float age, RandomSource random, int tier) {
        float stemT = smoothstep(0.0F, profile.stemRiseTicks(), age);
        double stemTop = center.y + profile.stemHeight() * stemT;

        int stemCount = 8 + tier * 3;
        for (int i = 0; i < stemCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = profile.stemBaseRadius() * (0.25D + random.nextDouble() * 0.85D)
                    * Mth.lerp(stemT, 1.15F, 0.7F);
            double y = Mth.lerp(random.nextFloat(), (float) center.y, (float) stemTop);
            double taper = 1.0D - (y - center.y) / Math.max(1.0D, profile.stemHeight()) * 0.45D;
            level.addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    center.x + Math.cos(angle) * radius * taper,
                    y,
                    center.z + Math.sin(angle) * radius * taper,
                    (random.nextDouble() - 0.5D) * 0.008D,
                    0.06D + random.nextDouble() * 0.05D,
                    (random.nextDouble() - 0.5D) * 0.008D
            );
        }

        if (age < profile.capStartTick()) {
            return;
        }

        float capT = smoothstep(profile.capStartTick(), profile.capStartTick() + profile.capExpandTicks(), age);
        double capRadius = Mth.lerp(capT, profile.stemTopRadius(), profile.capRadius());
        double capY = stemTop + capT * (3.0D + tier);
        int capCount = 10 + tier * 5;
        for (int i = 0; i < capCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = capRadius * Math.sqrt(random.nextDouble());
            double roll = Math.sin(capT * Math.PI) * profile.rollDrop() * (0.4D + random.nextDouble() * 0.6D);
            level.addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    center.x + Math.cos(angle) * dist,
                    capY - roll + random.nextDouble() * 1.5D,
                    center.z + Math.sin(angle) * dist,
                    Math.cos(angle) * 0.02D * capT,
                    0.01D + random.nextDouble() * 0.02D,
                    Math.sin(angle) * 0.02D * capT
            );
            if (random.nextFloat() < 0.35F) {
                level.addAlwaysVisibleParticle(
                        ParticleTypes.LARGE_SMOKE,
                        center.x + Math.cos(angle) * dist * 0.9D,
                        capY - roll * 0.7D,
                        center.z + Math.sin(angle) * dist * 0.9D,
                        (random.nextDouble() - 0.5D) * 0.02D,
                        0.01D,
                        (random.nextDouble() - 0.5D) * 0.02D
                );
            }
        }
    }

    private void spawnGroundRing(ClientLevel level, float age, RandomSource random, int tier) {
        float ringT = smoothstep(0.0F, profile.shockRingEndTicks(), age);
        double radius = profile.shockRingMaxRadius() * ringT;
        if (radius < 2.0D) {
            return;
        }

        int points = 14 + tier * 6;
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points + random.nextDouble() * 0.2D;
            double jitter = radius * (0.92D + random.nextDouble() * 0.12D);
            double x = center.x + Math.cos(angle) * jitter;
            double z = center.z + Math.sin(angle) * jitter;
            int blockX = Mth.floor(x);
            int blockZ = Mth.floor(z);
            if (!level.hasChunk(blockX >> 4, blockZ >> 4)) {
                continue;
            }
            int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockX, blockZ);
            double outward = 0.04D + ringT * 0.06D;
            level.addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x,
                    groundY + 0.4D + random.nextDouble(),
                    z,
                    Math.cos(angle) * outward,
                    0.02D + random.nextDouble() * 0.03D,
                    Math.sin(angle) * outward
            );
        }
    }

    private void spawnAsh(ClientLevel level, float age, RandomSource random, int tier) {
        float fade = 1.0F - Mth.clamp(
                (age - profile.dissipateStartTick() * 0.35F)
                        / Math.max(1.0F, profile.endTicks() - profile.dissipateStartTick() * 0.35F),
                0.0F,
                1.0F
        );
        if (fade <= 0.05F) {
            return;
        }

        double radius = profile.tier().blastRadius() * (0.55D + 0.35D * fade);
        int spawns = Mth.clamp((int) (8 + tier * 4 * fade), 4, 24);
        for (int i = 0; i < spawns; i++) {
            if (MushroomCloudVariation.hash01(seed, (int) age * 31 + i) > 0.72F) {
                continue;
            }
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = Math.sqrt(random.nextDouble()) * radius;
            double x = center.x + Math.cos(angle) * dist;
            double z = center.z + Math.sin(angle) * dist;
            int blockX = Mth.floor(x);
            int blockZ = Mth.floor(z);
            if (!level.hasChunk(blockX >> 4, blockZ >> 4)) {
                continue;
            }
            int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockX, blockZ);
            level.addAlwaysVisibleParticle(
                    ParticleTypes.ASH,
                    x,
                    groundY + 0.2D + random.nextDouble() * 1.5D,
                    z,
                    (random.nextDouble() - 0.5D) * 0.01D,
                    0.01D,
                    (random.nextDouble() - 0.5D) * 0.01D
            );
            if (random.nextFloat() < 0.4F) {
                level.addAlwaysVisibleParticle(
                        ParticleTypes.WHITE_ASH,
                        x,
                        groundY + 0.5D,
                        z,
                        0.0D,
                        0.01D,
                        0.0D
                );
            }
        }
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = Mth.clamp((value - edge0) / Math.max(1.0E-4F, edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
