package com.createnucleararmaments.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Deterministic per-cloud variation so smoke puffs stay stable across frames. */
final class MushroomCloudVariation {
    static final Params STEM = new Params(0.88F, 1.12F, 0.03F, 0.12F, 0.0F);
    static final Params CAP = new Params(0.75F, 1.25F, 0.05F, 0.30F, 0.10F);
    static final Params ROLL = new Params(0.80F, 1.20F, 0.06F, 0.20F, 0.10F);
    static final Params CAP_CORE = new Params(0.92F, 1.08F, 0.0F, 0.08F, 0.0F);

    private MushroomCloudVariation() {
    }

    record Params(float sizeMin, float sizeMax, float radiusJitter, float yJitter, float lobeAmp) {
    }

    static int seedFrom(Vec3 center, long startTick) {
        int x = Mth.floor(center.x * 31.0D);
        int y = Mth.floor(center.y * 17.0D);
        int z = Mth.floor(center.z * 23.0D);
        long mixed = startTick ^ ((long) x << 20) ^ ((long) y << 10) ^ (z & 0x3FFL);
        return (int) (mixed ^ (mixed >>> 32));
    }

    static float hash01(int seed, int salt) {
        int hash = seed ^ salt * 0x9E3779B9;
        hash ^= hash >>> 16;
        hash *= 0x7FEB352D;
        hash ^= hash >>> 15;
        hash *= 0x846CA68B;
        hash ^= hash >>> 16;
        return (hash & 0xFFFF) / 65535.0F;
    }

    static float sizeMultiplier(int seed, int salt, Params params) {
        return Mth.lerp(hash01(seed, salt), params.sizeMin, params.sizeMax);
    }

    static double ringRadius(double baseRadius, double angle, int seed, int salt, int tier, Params params) {
        double radius = baseRadius;
        if (params.lobeAmp() > 0.0F) {
            double lobes = 5.0D + tier * 0.5D;
            double wobble = params.lobeAmp()
                    * (Math.sin(lobes * angle) * 0.65D + Math.sin(lobes * 1.73D * angle + 1.2D) * 0.35D);
            wobble += params.lobeAmp() * 0.35D * (hash01(seed, salt + 31) * 2.0F - 1.0F);
            radius *= 1.0D + wobble;
        }
        if (params.radiusJitter() > 0.0F) {
            radius *= 1.0D + (hash01(seed, salt + 53) * 2.0F - 1.0F) * params.radiusJitter();
        }
        return radius;
    }

    static double yOffset(int seed, int salt, Params params) {
        if (params.yJitter() <= 0.0F) {
            return 0.0D;
        }
        return (hash01(seed, salt + 71) * 2.0F - 1.0F) * params.yJitter();
    }
}
