package com.createnucleararmaments.client.render;

import com.createnucleararmaments.munitions.MushroomCloudProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MushroomCloudEffect {
    public record ShockRingTrailRing(double radius, float depositAge) {
    }

    private final Vec3 center;
    private final MushroomCloudProfile profile;
    private final long startTick;
    private final int seed;
    private final List<ShockRingTrailRing> shockRingTrail = new ArrayList<>();
    private double lastTrailRadius = -1.0D;
    private boolean flashSpawned;

    public MushroomCloudEffect(Vec3 center, MushroomCloudProfile profile, long startTick) {
        this.center = center;
        this.profile = profile;
        this.startTick = startTick;
        this.seed = MushroomCloudVariation.seedFrom(center, startTick);
    }

    public Vec3 center() {
        return center;
    }

    public MushroomCloudProfile profile() {
        return profile;
    }

    public int seed() {
        return seed;
    }

    public List<ShockRingTrailRing> shockRingTrail() {
        return Collections.unmodifiableList(shockRingTrail);
    }

    public float trailAlpha(float currentAge, float depositAge) {
        if (currentAge <= depositAge) {
            return 0.0F;
        }
        float settle = Mth.clamp((currentAge - depositAge) / 8.0F, 0.0F, 1.0F);
        float alpha = 0.82F * settle;
        if (currentAge >= profile.dissipateStartTick()) {
            float fade = (currentAge - profile.dissipateStartTick())
                    / (profile.endTicks() - profile.dissipateStartTick());
            alpha *= 1.0F - Mth.clamp(fade, 0.0F, 1.0F);
        }
        return alpha;
    }

    public void tickShockRingTrail(float age) {
        if (!ShockRingState.isVisible(profile, age, 1.0F)) {
            return;
        }

        double frontRadius = ShockRingState.frontRadius(profile, age);
        double step = Math.max(2.0D, profile.shockRingMaxRadius() / 32.0D);
        if (lastTrailRadius < 0.0D || frontRadius - lastTrailRadius >= step) {
            shockRingTrail.add(new ShockRingTrailRing(frontRadius, age));
            lastTrailRadius = frontRadius;
            while (shockRingTrail.size() > 48) {
                shockRingTrail.remove(0);
            }
        }
    }

    public float ageTicks(long gameTime, float partialTick) {
        return (gameTime - startTick) + partialTick;
    }

    public boolean isExpired(long gameTime) {
        long endTick = startTick + Math.max(profile.endTicks(), groundFalloutDurationTicks());
        return gameTime > endTick;
    }

    public int groundFalloutDurationTicks() {
        int tier = profile.tier().tier();
        return (int) (profile.tier().blastRadius() * 14.0F) + 180 + tier * 60;
    }

    public float globalAlpha(float age) {
        if (age >= profile.dissipateStartTick()) {
            float fade = (age - profile.dissipateStartTick())
                    / (profile.endTicks() - profile.dissipateStartTick());
            return 1.0F - Mth.clamp(fade, 0.0F, 1.0F);
        }
        return 1.0F;
    }

    public void tickAccentParticles(ClientLevel level, long gameTime) {
        float age = ageTicks(gameTime, 0.0F);
        RandomSource random = level.getRandom();

        tickShockRingTrail(age);

        if (!flashSpawned && age >= MushroomCloudProfile.SYNC_LEAD_TICKS - 1.0F) {
            flashSpawned = true;
            level.addAlwaysVisibleParticle(
                    ParticleTypes.FLASH,
                    center.x,
                    center.y + 1.5D,
                    center.z,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }

        if (ShockRingState.isVisible(profile, age, 1.0F)) {
            spawnShockRingParticles(level, age, random);
        }

        if (age <= profile.capStartTick() + profile.capExpandTicks() * 0.85F) {
            spawnBodySmokeParticles(level, age, random);
        }

        if (age <= profile.fireballTicks() + profile.shockRingEndTicks() / 2) {
            spawnFireParticles(level, age, random);
        }

        if (age <= groundFalloutDurationTicks() && ((int) age % 2) == 0) {
            spawnGroundFalloutParticles(level, age, random);
        }
    }

    /** Random-density smoke and fire across the blast disk, sampled to local ground height. */
    private void spawnGroundFalloutParticles(ClientLevel level, float age, RandomSource random) {
        int duration = groundFalloutDurationTicks();
        float life = Mth.clamp(age / duration, 0.0F, 1.0F);
        float firePhase = Mth.clamp(1.0F - life / 0.4F, 0.0F, 1.0F);
        double radius = profile.tier().blastRadius() * 0.96D;
        int tier = profile.tier().tier();
        double area = Math.PI * radius * radius;
        int maxSpawns = switch (tier) {
            case 1 -> 28;
            case 2 -> 44;
            default -> 58;
        };
        int spawns = Mth.clamp((int) (area * 0.012D * (1.0D - life * 0.3D)) + tier * 4, 8, maxSpawns);

        for (int i = 0; i < spawns; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = Math.sqrt(random.nextDouble()) * radius;
            double x = center.x + Math.cos(angle) * dist;
            double z = center.z + Math.sin(angle) * dist;

            int cellX = Mth.floor((x - center.x) / 5.0D);
            int cellZ = Mth.floor((z - center.z) / 5.0D);
            float cellDensity = 0.38F + MushroomCloudVariation.hash01(seed, cellX * 928371 + cellZ * 689287 + 17) * 0.52F;
            if (random.nextFloat() > cellDensity) {
                continue;
            }

            int blockX = Mth.floor(x);
            int blockZ = Mth.floor(z);
            int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockX, blockZ);
            double distNorm = dist / radius;
            double drift = 0.001D + random.nextDouble() * 0.003D;
            double vx = (random.nextDouble() - 0.5D) * drift;
            double vz = (random.nextDouble() - 0.5D) * drift;
            double y = groundY + 0.08D + random.nextDouble() * 0.35D;
            double vy = 0.001D + random.nextDouble() * 0.003D;

            level.addAlwaysVisibleParticle(
                    ParticleTypes.LARGE_SMOKE,
                    x,
                    y,
                    z,
                    vx,
                    vy,
                    vz
            );

            if (random.nextFloat() < 0.28F) {
                level.addAlwaysVisibleParticle(
                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        x,
                        y + 0.1D,
                        z,
                        vx * 0.7D,
                        vy * 0.8D,
                        vz * 0.7D
                );
            }

            if (firePhase > 0.05F && distNorm < 0.55D && random.nextFloat() < firePhase * (0.4F - distNorm * 0.35F)) {
                level.addAlwaysVisibleParticle(
                        ParticleTypes.FLAME,
                        x,
                        groundY + 0.08D,
                        z,
                        vx * 0.5D,
                        0.012D + random.nextDouble() * 0.02D,
                        vz * 0.5D
                );
            }
            if (firePhase > 0.35F && distNorm < 0.32D && random.nextFloat() < firePhase * 0.12F) {
                level.addAlwaysVisibleParticle(
                        ParticleTypes.SMALL_FLAME,
                        x,
                        groundY + 0.05D,
                        z,
                        vx * 0.35D,
                        0.015D + random.nextDouble() * 0.02D,
                        vz * 0.35D
                );
            }
            if (firePhase > 0.5F && distNorm < 0.18D && random.nextFloat() < 0.04F) {
                level.addAlwaysVisibleParticle(ParticleTypes.LAVA, x, groundY + 0.1D, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void spawnFireParticles(ClientLevel level, float age, RandomSource random) {
        float fireLife = Mth.clamp(age / profile.fireballTicks(), 0.0F, 1.0F);
        int flames = 18 + profile.tier().tier() * 10;
        double spread = profile.stemBaseRadius() * (2.5D + fireLife * 2.0D);

        for (int i = 0; i < flames; i++) {
            double ox = (random.nextDouble() - 0.5D) * spread;
            double oy = random.nextDouble() * (3.5D + fireLife * 4.0D);
            double oz = (random.nextDouble() - 0.5D) * spread;
            level.addAlwaysVisibleParticle(
                    ParticleTypes.FLAME,
                    center.x + ox,
                    center.y + oy,
                    center.z + oz,
                    (random.nextDouble() - 0.5D) * 0.04D,
                    0.03D + random.nextDouble() * 0.05D,
                    (random.nextDouble() - 0.5D) * 0.04D
            );
            if (random.nextFloat() < 0.45F) {
                level.addAlwaysVisibleParticle(
                        ParticleTypes.LAVA,
                        center.x + ox * 0.7D,
                        center.y + oy * 0.6D,
                        center.z + oz * 0.7D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }

        if (fireLife < 0.65F) {
            int bursts = 6 + profile.tier().tier() * 3;
            for (int i = 0; i < bursts; i++) {
                double ox = (random.nextDouble() - 0.5D) * spread * 0.6D;
                double oy = 1.0D + random.nextDouble() * 2.5D;
                double oz = (random.nextDouble() - 0.5D) * spread * 0.6D;
                level.addAlwaysVisibleParticle(
                        ParticleTypes.SMALL_FLAME,
                        center.x + ox,
                        center.y + oy,
                        center.z + oz,
                        (random.nextDouble() - 0.5D) * 0.02D,
                        0.04D + random.nextDouble() * 0.03D,
                        (random.nextDouble() - 0.5D) * 0.02D
                );
            }
        }
    }

    private void spawnShockRingParticles(ClientLevel level, float age, RandomSource random) {
        double frontRadius = ShockRingState.frontRadius(profile, age);
        float ringAlpha = ShockRingState.frontAlpha(profile, age, 1.0F);
        if (ringAlpha <= 0.02F) {
            return;
        }

        int points = 24 + profile.tier().tier() * 8;

        for (int i = 0; i < points; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = frontRadius * (0.78D + random.nextDouble() * 0.32D);
            double x = center.x + Math.cos(angle) * dist;
            double z = center.z + Math.sin(angle) * dist;
            double y = center.y + 0.4D + random.nextDouble() * 2.8D;
            double speed = 0.035D + (frontRadius / profile.shockRingMaxRadius()) * 0.05D;
            level.addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x,
                    y,
                    z,
                    Math.cos(angle) * speed,
                    0.012D + random.nextDouble() * 0.02D,
                    Math.sin(angle) * speed
            );
            double distNorm = dist / profile.tier().radiationRadius();
            if (distNorm < 0.62D && ringAlpha > 0.35F && random.nextFloat() < 0.45F) {
                level.addAlwaysVisibleParticle(
                        ParticleTypes.FLAME,
                        x,
                        y,
                        z,
                        Math.cos(angle) * speed * 0.5D,
                        0.02D + random.nextDouble() * 0.03D,
                        Math.sin(angle) * speed * 0.5D
                );
            }
            if (random.nextFloat() < 0.3F) {
                level.addAlwaysVisibleParticle(
                        ParticleTypes.LARGE_SMOKE,
                        x,
                        y + 0.4D,
                        z,
                        Math.cos(angle) * speed * 0.7D,
                        0.02D,
                        Math.sin(angle) * speed * 0.7D
                );
            }
        }
    }

    private void spawnBodySmokeParticles(ClientLevel level, float age, RandomSource random) {
        float stemProgress = smoothstep(0.0F, profile.stemRiseTicks(), age);
        double stemY = center.y + profile.stemHeight() * stemProgress;
        int count = 6 + profile.tier().tier() * 3;

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = profile.stemBaseRadius() * (0.4D + random.nextDouble() * 0.9D);
            double x = center.x + Math.cos(angle) * dist;
            double z = center.z + Math.sin(angle) * dist;
            double y = center.y + 0.5D + random.nextDouble() * Math.max(1.0D, stemY - center.y);
            level.addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x,
                    y,
                    z,
                    (random.nextDouble() - 0.5D) * 0.01D,
                    0.03D + random.nextDouble() * 0.03D,
                    (random.nextDouble() - 0.5D) * 0.01D
            );
        }

        if (age >= profile.capStartTick()) {
            float capAge = age - profile.capStartTick();
            float emergePhase = profile.capExpandTicks() * 0.35F;
            float emergeT = smoothstep(0.0F, emergePhase, capAge);
            float spreadT = smoothstep(emergePhase * 0.45F, profile.capExpandTicks(), capAge);
            double capRadius = Mth.lerp(emergeT, profile.stemTopRadius() * 0.9D, profile.stemTopRadius() * 1.6D)
                    + profile.capRadius() * (0.18D + spreadT * 0.72D);
            int capPoints = 4 + profile.tier().tier() * 2;
            for (int i = 0; i < capPoints; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double dist = capRadius * (0.55D + random.nextDouble() * 0.45D);
                level.addAlwaysVisibleParticle(
                        ParticleTypes.LARGE_SMOKE,
                        center.x + Math.cos(angle) * dist,
                        stemY + 0.5D + random.nextDouble() * 2.0D,
                        center.z + Math.sin(angle) * dist,
                        (random.nextDouble() - 0.5D) * 0.015D,
                        0.015D,
                        (random.nextDouble() - 0.5D) * 0.015D
                );
            }
        }
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
