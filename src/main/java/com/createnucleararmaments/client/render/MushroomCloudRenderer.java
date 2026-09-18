package com.createnucleararmaments.client.render;

import com.createnucleararmaments.CreateNuclearArmaments;
import com.createnucleararmaments.munitions.MushroomCloudProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Compact billboard mushroom using a large soft smoke atlas ({@code smoke_puff.png} 512px).
 * Fewer oversized quads read as mass; uses {@link RenderType#entityTranslucent} only.
 */
public final class MushroomCloudRenderer {
    private static final ResourceLocation SMOKE = ResourceLocation.fromNamespaceAndPath(
            CreateNuclearArmaments.MOD_ID, "textures/effect/smoke_puff.png");
    private static final ResourceLocation FIRE = ResourceLocation.fromNamespaceAndPath(
            CreateNuclearArmaments.MOD_ID, "textures/effect/explosion_7.png");

    private MushroomCloudRenderer() {
    }

    public static void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            MushroomCloudEffect cloud,
            long gameTime,
            float partialTick
    ) {
        float age = cloud.ageTicks(gameTime, partialTick);
        if (age < 0.0F || age > cloud.profile().endTicks()) {
            return;
        }

        MushroomCloudProfile profile = cloud.profile();
        float alpha = cloud.globalAlpha(age);
        if (alpha <= 0.02F) {
            return;
        }

        Vec3 center = cloud.center();
        Vec3 cam = camera.getPosition();
        int light = LightTexture.FULL_BRIGHT;
        int seed = cloud.seed();
        int tier = profile.tier().tier();

        float stemT = smoothstep(0.0F, profile.stemRiseTicks(), age);
        double stemTopY = center.y + profile.stemHeight() * stemT;
        float fireT = 1.0F - Mth.clamp(age / Math.max(1.0F, profile.fireballTicks()), 0.0F, 1.0F);

        VertexConsumer smoke = buffer.getBuffer(RenderType.entityTranslucent(SMOKE));
        VertexConsumer fire = buffer.getBuffer(RenderType.entityTranslucent(FIRE));

        if (fireT > 0.05F) {
            float fireSize = (float) (14.0D + tier * 5.0D) * (0.6F + fireT * 0.9F);
            billboard(poseStack, fire, cam, center.x, center.y + 3.0D, center.z, fireSize, 1.0F, 0.55F, 0.2F, fireT * alpha, light);
        }

        // Fewer, much larger quads = solid mass silhouette instead of glitter.
        int stemLayers = 6 + tier;
        int stemSegs = 6 + tier;
        for (int layer = 0; layer < stemLayers; layer++) {
            float lt = (layer + 0.5F) / stemLayers;
            if (lt > stemT) {
                continue;
            }
            double y = Mth.lerp(lt, center.y, stemTopY);
            double radius = Mth.lerp(lt, profile.stemBaseRadius(), profile.stemTopRadius()) * (1.0D + 0.15D * stemT);
            float layerAlpha = alpha * (0.78F + 0.18F * (1.0F - lt));
            float size = (float) (10.0D + tier * 2.5D + radius * 1.1D);
            for (int s = 0; s < stemSegs; s++) {
                double angle = (Math.PI * 2.0D * s) / stemSegs + layer * 0.17D;
                double rr = radius * (0.7D + 0.3D * MushroomCloudVariation.hash01(seed, layer * 31 + s));
                double x = center.x + Math.cos(angle) * rr;
                double z = center.z + Math.sin(angle) * rr;
                billboard(poseStack, smoke, cam, x, y, z, size, 0.22F, 0.22F, 0.2F, layerAlpha, light);
            }
        }

        if (age >= profile.capStartTick()) {
            float capT = smoothstep(profile.capStartTick(), profile.capStartTick() + profile.capExpandTicks(), age);
            double capRadius = Mth.lerp(capT, profile.stemTopRadius() * 1.4D, profile.capRadius());
            double capY = stemTopY + capT * (4.0D + tier * 1.2D);
            int rings = 4 + tier;
            int segs = 8 + tier * 2;
            for (int ring = 0; ring < rings; ring++) {
                float rt = (ring + 0.5F) / rings;
                double ringR = capRadius * Math.sqrt(rt);
                double y = capY - rt * profile.rollDrop() * capT;
                float size = (float) (12.0D + tier * 2.0D + ringR * 0.28D);
                float ringAlpha = alpha * (0.82F - rt * 0.2F);
                for (int s = 0; s < segs; s++) {
                    double angle = (Math.PI * 2.0D * s) / segs + ring * 0.11D;
                    double x = center.x + Math.cos(angle) * ringR;
                    double z = center.z + Math.sin(angle) * ringR;
                    billboard(poseStack, smoke, cam, x, y, z, size, 0.2F, 0.2F, 0.18F, ringAlpha, light);
                }
            }
            billboard(
                    poseStack,
                    smoke,
                    cam,
                    center.x,
                    capY + 2.0D,
                    center.z,
                    (float) (capRadius * 0.9D + 10.0D),
                    0.18F,
                    0.18F,
                    0.16F,
                    alpha * 0.75F,
                    light
            );
        }

        if (age <= profile.shockRingEndTicks()) {
            float ringT = smoothstep(0.0F, profile.shockRingEndTicks(), age);
            double radius = profile.shockRingMaxRadius() * ringT;
            float ringAlpha = alpha * (1.0F - ringT) * 0.75F;
            if (radius > 2.0D && ringAlpha > 0.04F) {
                int segs = 14 + tier * 3;
                float size = (float) (8.0D + tier * 1.5D);
                for (int s = 0; s < segs; s++) {
                    double angle = (Math.PI * 2.0D * s) / segs;
                    double x = center.x + Math.cos(angle) * radius;
                    double z = center.z + Math.sin(angle) * radius;
                    billboard(poseStack, smoke, cam, x, center.y + 1.2D, z, size, 0.3F, 0.28F, 0.26F, ringAlpha, light);
                }
            }
        }
    }

    private static void billboard(
            PoseStack poseStack,
            VertexConsumer consumer,
            Vec3 camera,
            double x,
            double y,
            double z,
            float size,
            float r,
            float g,
            float b,
            float a,
            int light
    ) {
        if (a <= 0.02F || size <= 0.1F || !Float.isFinite(size) || !Float.isFinite(a)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        float yaw = (float) Math.atan2(camera.x - x, camera.z - z);
        poseStack.mulPose(Axis.YP.rotation(yaw));

        PoseStack.Pose pose = poseStack.last();
        float h = size * 0.5F;
        int overlay = OverlayTexture.NO_OVERLAY;

        // Float color path avoids packed ARGB/ABGR mismatch crashes on 1.21 vertex formats.
        quad(consumer, pose, -h, -h, 0.0F, 0.0F, 1.0F, r, g, b, a, overlay, light);
        quad(consumer, pose, -h, h, 0.0F, 0.0F, 0.0F, r, g, b, a, overlay, light);
        quad(consumer, pose, h, h, 0.0F, 1.0F, 0.0F, r, g, b, a, overlay, light);
        quad(consumer, pose, h, -h, 0.0F, 1.0F, 1.0F, r, g, b, a, overlay, light);

        poseStack.popPose();
    }

    private static void quad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            float r,
            float g,
            float b,
            float a,
            int overlay,
            int light
    ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = Mth.clamp((value - edge0) / Math.max(1.0E-4F, edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
