package com.createnucleararmaments.munitions;

import com.createnucleararmaments.index.CNAParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Slow-rising nuclear mushroom made primarily of mod pixel-smoke particles.
 * Timing aims for: flash → ground surge → gradual column → late overhanging cap.
 */
public final class MushroomCloudParticleScheduler {
    private static final List<Job> JOBS = new ArrayList<>();

    private MushroomCloudParticleScheduler() {
    }

    public static void schedule(ServerLevel level, Vec3 center, NuclearTier tier) {
        JOBS.add(new Job(level, center, tier, level.getGameTime()));
    }

    public static void clearAll() {
        JOBS.clear();
    }

    public static void clearLevel(ServerLevel level) {
        JOBS.removeIf(job -> job.level == level);
    }

    public static void tick(ServerLevel level) {
        long time = level.getGameTime();
        Iterator<Job> iterator = JOBS.iterator();
        while (iterator.hasNext()) {
            Job job = iterator.next();
            if (job.level != level) {
                continue;
            }
            if (job.tick(time)) {
                iterator.remove();
            }
        }
    }

    private static void particle(
            ServerLevel level,
            ParticleOptions type,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
    ) {
        for (ServerPlayer player : level.players()) {
            if (player.level() != level) {
                continue;
            }
            level.sendParticles(player, type, true, x, y, z, 0, xSpeed, ySpeed, zSpeed, 1.0D);
        }
    }

    private static void smoke(ServerLevel level, double x, double y, double z, double vx, double vy, double vz) {
        particle(level, CNAParticles.MUSHROOM_SMOKE.get(), x, y, z, vx, vy, vz);
    }

    private static final class Job {
        private final ServerLevel level;
        private final Vec3 center;
        private final NuclearTier tier;
        private final long startTick;
        private final int durationTicks;
        private final int stemRiseTicks;
        private final int capStartAge;
        private final double stemHeight;
        private final double stemRadius;
        private final double capRadius;
        private final double ringRadius;

        private Job(ServerLevel level, Vec3 center, NuclearTier tier, long startTick) {
            this.level = level;
            this.center = center;
            this.tier = tier;
            this.startTick = startTick;
            int t = tier.tier();
            // Snappier stem climb (~2.5–4s depending on tier).
            this.stemRiseTicks = 55 + t * 14;
            this.capStartAge = (int) (stemRiseTicks * 0.32F);
            this.durationTicks = stemRiseTicks + 160 + t * 60;
            double yield = Math.sqrt(tier.yieldKilotons());
            this.stemHeight = 30.0D + yield * 9.0D + tier.blastRadius() * 0.4D;
            this.stemRadius = 3.4D + t * 1.2D + yield * 0.4D;
            this.capRadius = 13.0D + yield * 5.0D + tier.blastRadius() * 0.2D;
            // Horizontal skirt should rival the radiation disk, not just the crater.
            this.ringRadius = Math.max(tier.blastRadius() * 2.15D, tier.radiationRadius() * 0.95D);
        }

        private boolean tick(long gameTime) {
            int age = (int) (gameTime - startTick);
            if (age < 0) {
                return false;
            }
            if (age >= durationTicks) {
                return true;
            }

            RandomSource random = level.getRandom();
            float life = age / (float) durationTicks;
            // Ease-out rise: quicker climb than before.
            float stemT = 1.0F - (float) Math.pow(1.0F - Mth.clamp(age / (float) stemRiseTicks, 0.0F, 1.0F), 1.25F);
            double stemTop = center.y + stemHeight * stemT;
            int t = tier.tier();

            if (age <= 45) {
                spawnFireball(random, age, t);
            }
            if (age < 175) {
                spawnShockRing(random, age, t);
            }
            if (stemT > 0.02F && life < 0.9F) {
                spawnStem(random, stemTop, stemT, life, t, age);
            }
            if (age >= capStartAge && life < 0.93F) {
                spawnCap(random, stemTop, age, life, t);
            }
            if (age > stemRiseTicks / 3 && (age & 1) == 0) {
                spawnAshFall(random, stemTop, life, t);
            }
            return false;
        }

        private void spawnFireball(RandomSource random, int age, int t) {
            if (age == 0) {
                for (int i = 0; i < 3; i++) {
                    particle(level, ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 1.0D + i, center.z, 0, 0, 0);
                }
                particle(level, ParticleTypes.FLASH, center.x, center.y + 2.0D, center.z, 0, 0, 0);
            }
            int flames = 24 + t * 14;
            double spread = 3.5D + t * 1.2D + age * 0.22D;
            for (int i = 0; i < flames; i++) {
                double ox = (random.nextDouble() - 0.5D) * spread;
                double oy = random.nextDouble() * (4.0D + age * 0.3D);
                double oz = (random.nextDouble() - 0.5D) * spread;
                particle(level, ParticleTypes.FLAME, center.x + ox, center.y + oy, center.z + oz, 0, 0.05D, 0);
                if (random.nextFloat() < 0.4F) {
                    particle(level, ParticleTypes.SMALL_FLAME, center.x + ox * 0.8D, center.y + oy * 0.7D, center.z + oz * 0.8D, 0, 0.04D, 0);
                }
                if (random.nextFloat() < 0.35F) {
                    particle(level, ParticleTypes.LAVA, center.x + ox * 0.55D, center.y + oy * 0.4D, center.z + oz * 0.55D, 0, 0, 0);
                }
                if (random.nextFloat() < 0.5F) {
                    smoke(level, center.x + ox, center.y + oy * 0.7D, center.z + oz, ox * 0.01D, 0.03D, oz * 0.01D);
                }
            }
        }

        private void spawnStem(RandomSource random, double stemTop, float stemT, float life, int t, int age) {
            double frontY = Mth.lerp(0.72F, center.y, stemTop);
            int count = 12 + t * 6;
            double fade = life > 0.75F ? (1.0F - (life - 0.75F) / 0.15F) : 1.0D;
            for (int i = 0; i < count; i++) {
                if (random.nextDouble() > fade) {
                    continue;
                }
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double yNorm = Math.pow(random.nextDouble(), 0.45D);
                double y = Mth.lerp((float) yNorm, (float) center.y, (float) Math.max(frontY, center.y + 1.0D));
                double taper = 1.2D - ((y - center.y) / Math.max(1.0D, stemHeight * stemT + 1.0D)) * 0.55D;
                double radius = stemRadius * taper * (0.45D + random.nextDouble() * 0.7D);
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;
                // Faster vertical climb
                double rise = 0.06D + (1.0D - stemT) * 0.07D;
                smoke(level, x, y, z, (random.nextDouble() - 0.5D) * 0.012D, rise, (random.nextDouble() - 0.5D) * 0.012D);
                if (random.nextFloat() < 0.22F) {
                    particle(level, ParticleTypes.LARGE_SMOKE, x, y, z, 0, rise * 0.85D, 0);
                }
                // Inner fire column while the stem is still young
                double heightFrac = (y - center.y) / Math.max(1.0D, stemHeight);
                if (stemT < 0.75F && heightFrac < 0.55D && random.nextFloat() < 0.28F) {
                    particle(level, ParticleTypes.FLAME, x, y, z, 0, rise * 1.2D, 0);
                    if (random.nextFloat() < 0.35F) {
                        particle(level, ParticleTypes.LAVA, x, y - 0.2D, z, 0, 0, 0);
                    }
                }
            }
        }

        private void spawnCap(RandomSource random, double stemTop, int age, float life, int t) {
            float capT = smooth((age - capStartAge) / (float) Math.max(1, durationTicks - capStartAge - 80));
            double radius = capRadius * (0.2D + 0.8D * capT);
            double capY = stemTop + 1.5D + capT * (4.0D + t);
            int count = 14 + t * 8;
            double fade = life > 0.8F ? (1.0F - (life - 0.8F) / 0.13F) : 1.0D;
            for (int i = 0; i < count; i++) {
                if (random.nextDouble() > fade) {
                    continue;
                }
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double dist = radius * Math.pow(random.nextDouble(), 0.5D);
                double roll = (dist / Math.max(0.1D, radius)) * (2.8D + t) * Math.sin(capT * Math.PI);
                double x = center.x + Math.cos(angle) * dist;
                double z = center.z + Math.sin(angle) * dist;
                double y = capY - roll;
                smoke(level, x, y, z, Math.cos(angle) * 0.025D * capT, 0.01D, Math.sin(angle) * 0.025D * capT);
                if (capT < 0.45F && dist < radius * 0.35D && random.nextFloat() < 0.2F) {
                    particle(level, ParticleTypes.FLAME, x, y, z, 0, 0.02D, 0);
                }
            }
        }

        private void spawnShockRing(RandomSource random, int age, int t) {
            float ringT = smooth(age / 175.0F);
            double radius = ringRadius * ringT;
            int points = 20 + t * 8;
            // Dense falling curtain under the expanding skirt.
            int fallCount = (int) ((14 + t * 6) * (0.45F + 0.55F * ringT));
            for (int i = 0; i < points; i++) {
                double angle = (Math.PI * 2.0D * i) / points + random.nextDouble() * 0.08D;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double x = center.x + cos * radius;
                double z = center.z + sin * radius;
                double rimY = center.y + 0.7D + random.nextDouble() * 0.8D;
                // Slower outward drift so the skirt expands more gradually
                smoke(level, x, rimY, z, cos * 0.22D, 0.018D, sin * 0.22D);
                if (age < 50 && random.nextFloat() < 0.25F) {
                    particle(
                            level,
                            ParticleTypes.FLAME,
                            x,
                            center.y + 0.5D,
                            z,
                            cos * 0.2D,
                            0.03D,
                            sin * 0.2D
                    );
                }
            }
            // Falling smoke curtain trailing under the expanding horizontal ring
            for (int i = 0; i < fallCount; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double r = radius * (0.72D + random.nextDouble() * 0.35D);
                double x = center.x + cos * r;
                double z = center.z + sin * r;
                double y = center.y + 0.2D + random.nextDouble() * 2.4D;
                double outward = 0.03D + ringT * 0.07D;
                double fall = -0.025D - random.nextDouble() * 0.055D;
                smoke(level, x, y, z, cos * outward, fall, sin * outward);
                if (random.nextFloat() < 0.42F) {
                    particle(level, ParticleTypes.LARGE_SMOKE, x, y, z, cos * outward * 0.7D, fall * 0.9D, sin * outward * 0.7D);
                }
                if (random.nextFloat() < 0.32F) {
                    particle(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y - 0.15D, z, cos * 0.02D, fall * 0.7D, sin * 0.02D);
                }
            }
        }

        private void spawnAshFall(RandomSource random, double stemTop, float life, int t) {
            double radius = (capRadius * 0.8D + ringRadius * 0.2D) * (0.55D + 0.45D * (1.0F - life));
            int count = 6 + t * 3;
            for (int i = 0; i < count; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double dist = Math.sqrt(random.nextDouble()) * radius;
                double x = center.x + Math.cos(angle) * dist;
                double z = center.z + Math.sin(angle) * dist;
                double y = Mth.lerp(random.nextFloat(), (float) center.y + 6.0F, (float) stemTop + 2.0F);
                particle(level, ParticleTypes.ASH, x, y, z, 0, -0.015D, 0);
            }
        }

        private static float smooth(float t) {
            t = Mth.clamp(t, 0.0F, 1.0F);
            return t * t * (3.0F - 2.0F * t);
        }
    }
}
