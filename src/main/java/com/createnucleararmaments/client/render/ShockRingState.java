package com.createnucleararmaments.client.render;

import com.createnucleararmaments.munitions.MushroomCloudProfile;
import net.minecraft.util.Mth;

/** Shock ring expands linearly until {@link MushroomCloudProfile#shockRingEndTicks()}, fading after passing radiation. */
public final class ShockRingState {
    private static final double START_RADIUS = 1.2D;

    private ShockRingState() {
    }

    public static double frontRadius(MushroomCloudProfile profile, float age) {
        if (age <= 0.0F) {
            return START_RADIUS;
        }
        float expand = Mth.clamp(age / profile.shockRingEndTicks(), 0.0F, 1.0F);
        return Mth.lerp(expand, START_RADIUS, profile.shockRingMaxRadius());
    }

    public static float frontAlpha(MushroomCloudProfile profile, float age, float globalAlpha) {
        if (age < 0.0F || age > profile.shockRingEndTicks()) {
            return 0.0F;
        }

        double radius = frontRadius(profile, age);
        double radiationRadius = profile.tier().radiationRadius();
        float alpha = globalAlpha;

        if (age < 5.0F) {
            alpha *= age / 5.0F;
        }

        if (radius > radiationRadius) {
            double fadeSpan = profile.shockRingMaxRadius() - radiationRadius;
            if (fadeSpan > 0.001D) {
                float fade = Mth.clamp((float) ((radius - radiationRadius) / fadeSpan), 0.0F, 1.0F);
                alpha *= 1.0F - fade;
            }
        }

        return alpha;
    }

    public static boolean isVisible(MushroomCloudProfile profile, float age, float globalAlpha) {
        return frontAlpha(profile, age, globalAlpha) > 0.02F;
    }
}
