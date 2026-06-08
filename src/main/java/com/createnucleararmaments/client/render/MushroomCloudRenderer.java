package com.createnucleararmaments.client.render;

import com.createnucleararmaments.CreateNuclearArmaments;
import com.createnucleararmaments.munitions.MushroomCloudProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class MushroomCloudRenderer {
    private static final ResourceLocation SMOKE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CreateNuclearArmaments.MOD_ID,
            "textures/effect/smoke_puff.png"
    );
    private static final ResourceLocation FIRE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CreateNuclearArmaments.MOD_ID,
            "textures/effect/explosion_7.png"
    );

    private MushroomCloudRenderer() {
    }

    public static void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            ClientLevel level,
            MushroomCloudEffect cloud,
            long gameTime,
            float partialTick
    ) {
        float age = cloud.ageTicks(gameTime, partialTick);
        if (age < 0.0F || age > cloud.profile().endTicks()) {
            return;
        }

        MushroomCloudProfile profile = cloud.profile();
        Vec3 center = cloud.center();
        float alpha = cloud.globalAlpha(age);
        int seed = cloud.seed();
        int tier = profile.tier().tier();
        int stemLayers = 18 + tier * 5 + (int) (profile.stemHeight() * 0.08D);
        int stemSegments = 26 + tier * 7 + (int) (profile.stemBaseRadius() * 2.5D);
        int capShells = 7 + tier * 2;
        int capSegments = 28 + tier * 9 + (int) (profile.capRadius() * 0.22D);
        int rollSegments = 22 + tier * 6 + (int) (profile.capRadius() * 0.12D);
        float quadSize = Mth.clamp((float) (profile.capRadius() * 0.11F + 1.8F), 4.8F, 10.5F);

        if (ShockRingState.isVisible(profile, age, alpha)) {
            renderShockRing(
                    poseStack,
                    buffer,
                    camera,
                    center,
                    profile,
                    age,
                    alpha,
                    tier,
                    SMOKE_TEXTURE
            );
        }

        renderShockRingTrail(
                poseStack,
                buffer,
                camera,
                level,
                cloud,
                center,
                profile,
                age,
                alpha,
                seed,
                tier,
                quadSize,
                SMOKE_TEXTURE
        );

        if (age <= profile.fireballTicks()) {
            renderFireball(poseStack, buffer, camera, center, age, profile, alpha, quadSize, FIRE_TEXTURE);
        }

        float stemProgress = smoothstep(0.0F, profile.stemRiseTicks(), age);
        float stemAlpha = stemAlpha(profile, age, alpha);
        if (stemProgress > 0.01F && stemAlpha > 0.02F) {
            renderStem(
                    poseStack,
                    buffer,
                    camera,
                    center,
                    profile,
                    stemProgress,
                    stemLayers,
                    stemSegments,
                    quadSize,
                    stemAlpha,
                    seed,
                    tier,
                    SMOKE_TEXTURE
            );
        }

        if (age >= profile.capStartTick()) {
            float capAge = age - profile.capStartTick();
            float emergePhase = profile.capExpandTicks() * 0.35F;
            float emergeT = smoothstep(0.0F, emergePhase, capAge);
            float spreadT = smoothstep(emergePhase * 0.45F, profile.capExpandTicks(), capAge);
            float capVisibility = smoothstep(0.0F, emergePhase * 0.55F, capAge);
            float stemTop = (float) (center.y + profile.stemHeight() * stemProgress);
            renderCap(
                    poseStack,
                    buffer,
                    camera,
                    center.x,
                    stemTop + spreadT * (1.0F + tier * 0.18F),
                    center.z,
                    profile,
                    emergeT,
                    spreadT,
                    capShells,
                    capSegments,
                    quadSize * 1.15F,
                    alpha * 0.88F * capVisibility,
                    seed,
                    tier,
                    SMOKE_TEXTURE
            );
        }

        if (age >= profile.rollStartTick()) {
            float rollProgress = smoothstep(0.0F, profile.rollExpandTicks(), age - profile.rollStartTick());
            float stemTop = (float) (center.y + profile.stemHeight() * Mth.clamp(stemProgress, 0.95F, 1.0F));
            renderRoll(
                    poseStack,
                    buffer,
                    camera,
                    center.x,
                    stemTop - profile.rollDrop(),
                    center.z,
                    profile,
                    rollProgress,
                    rollSegments,
                    quadSize,
                    alpha * 0.78F,
                    seed,
                    tier,
                    SMOKE_TEXTURE
            );
        }
    }

    private static float stemAlpha(MushroomCloudProfile profile, float age, float globalAlpha) {
        float alpha = globalAlpha * 0.92F;
        if (age <= profile.capStartTick()) {
            return alpha;
        }
        float capAge = age - profile.capStartTick();
        float fadeStart = profile.capExpandTicks() * 0.22F;
        float fadeEnd = profile.capExpandTicks() * 0.82F;
        float fade = smoothstep(fadeStart, fadeEnd, capAge);
        return alpha * (1.0F - fade);
    }

    /** Expanding horizontal shock ring at detonation — dense radial smoke wall. */
    private static void renderShockRing(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            Vec3 center,
            MushroomCloudProfile profile,
            float age,
            float alpha,
            int tier,
            ResourceLocation texture
    ) {
        double frontRadius = ShockRingState.frontRadius(profile, age);
        float ringAlpha = ShockRingState.frontAlpha(profile, age, alpha);
        if (ringAlpha <= 0.02F) {
            return;
        }

        double radiationRadius = profile.tier().radiationRadius();
        float expandMix = Mth.clamp((float) (frontRadius / radiationRadius), 0.0F, 1.35F);
        float hotMix = 1.0F - Mth.clamp(expandMix * 0.82F, 0.0F, 1.0F);
        float r = Mth.lerp(hotMix, 0.95F, 0.72F);
        float g = Mth.lerp(hotMix, 0.48F, 0.56F);
        float b = Mth.lerp(hotMix, 0.14F, 0.50F);

        int segments = 44 + tier * 14;
        float wallWidth = 4.0F + tier * 0.85F;
        float wallHeight = 5.2F + tier * 0.95F;
        double[] heights = {0.3D, 0.9D, 1.5D, 2.2D, 3.0D};

        for (double height : heights) {
            for (int band = -2; band <= 2; band++) {
                double radius = frontRadius + band * 1.1D;
                if (radius < 0.8D) {
                    continue;
                }
                float bandAlpha = ringAlpha * (band == 0 ? 1.0F : 0.78F);
                drawRadialRingWall(
                        poseStack,
                        buffer,
                        center.x,
                        center.y + height,
                        center.z,
                        radius,
                        segments,
                        wallWidth,
                        wallHeight,
                        r,
                        g,
                        b,
                        bandAlpha,
                        texture
                );
            }
        }

        drawRing(
                poseStack,
                buffer,
                camera,
                center.x,
                center.y + 1.1D,
                center.z,
                frontRadius,
                segments,
                wallWidth * 0.9F,
                r,
                g,
                b,
                ringAlpha * 0.85F,
                texture
        );
    }

    private static void renderShockRingTrail(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            ClientLevel level,
            MushroomCloudEffect cloud,
            Vec3 center,
            MushroomCloudProfile profile,
            float age,
            float globalAlpha,
            int seed,
            int tier,
            float quadSize,
            ResourceLocation texture
    ) {
        if (cloud.shockRingTrail().isEmpty()) {
            return;
        }

        float puffSize = quadSize * 0.68F;
        float r = 0.52F;
        float g = 0.50F;
        float b = 0.46F;
        int segments = 12 + tier * 4;
        double[] heightOffsets = {0.18D, 0.85D, 1.55D};
        MushroomCloudVariation.Params variation = MushroomCloudVariation.STEM;

        for (MushroomCloudEffect.ShockRingTrailRing ring : cloud.shockRingTrail()) {
            float ringAlpha = cloud.trailAlpha(age, ring.depositAge()) * globalAlpha;
            if (ringAlpha <= 0.02F) {
                continue;
            }

            for (int layer = 0; layer < heightOffsets.length; layer++) {
                float layerAlpha = ringAlpha * (0.92F - layer * 0.1F);
                for (int i = 0; i < segments; i++) {
                    double angle = (Math.PI * 2.0D * i) / segments;
                    int salt = (int) (ring.depositAge() * 17.0F) + layer * 131 + i * 2;
                    double ringRadius = MushroomCloudVariation.ringRadius(ring.radius(), angle, seed, salt, tier, variation);
                    double x = center.x + Math.cos(angle) * ringRadius;
                    double z = center.z + Math.sin(angle) * ringRadius;
                    int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
                    double y = groundY + heightOffsets[layer] + MushroomCloudVariation.yOffset(seed, salt + 1, variation);
                    float size = puffSize * MushroomCloudVariation.sizeMultiplier(seed, salt, variation);
                    drawBillboard(poseStack, buffer, camera, x, y, z, size, r, g, b, layerAlpha, texture);

                    if (i % 2 == 0) {
                        double midAngle = angle + Math.PI / segments;
                        int midSalt = salt + 409;
                        double midRadius = MushroomCloudVariation.ringRadius(ring.radius() * 0.93D, midAngle, seed, midSalt, tier, variation);
                        double midX = center.x + Math.cos(midAngle) * midRadius;
                        double midZ = center.z + Math.sin(midAngle) * midRadius;
                        int midGroundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(midX), Mth.floor(midZ));
                        double midY = midGroundY + heightOffsets[layer] + MushroomCloudVariation.yOffset(seed, midSalt + 1, variation);
                        float midSize = puffSize * 0.82F * MushroomCloudVariation.sizeMultiplier(seed, midSalt, variation);
                        drawBillboard(
                                poseStack,
                                buffer,
                                camera,
                                midX,
                                midY,
                                midZ,
                                midSize,
                                r,
                                g,
                                b,
                                layerAlpha * 0.85F,
                                texture
                        );
                    }
                }
            }
        }
    }

    private static void drawRadialRingWall(
            PoseStack poseStack,
            MultiBufferSource buffer,
            double centerX,
            double y,
            double centerZ,
            double radius,
            int segments,
            float halfWidth,
            float halfHeight,
            float r,
            float g,
            float b,
            float alpha,
            ResourceLocation texture
    ) {
        for (int i = 0; i < segments; i++) {
            double angle = (Math.PI * 2.0D * i) / segments;
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            drawRadialBillboard(
                    poseStack,
                    buffer,
                    x,
                    y,
                    z,
                    angle,
                    halfWidth,
                    halfHeight,
                    r,
                    g,
                    b,
                    alpha,
                    texture
            );
        }
    }

    private static void drawRadialBillboard(
            PoseStack poseStack,
            MultiBufferSource buffer,
            double x,
            double y,
            double z,
            double angle,
            float halfWidth,
            float halfHeight,
            float r,
            float g,
            float b,
            float a,
            ResourceLocation texture
    ) {
        if (a <= 0.01F) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotation((float) (-angle - Math.PI / 2.0D)));

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        int light = LightTexture.FULL_BRIGHT;

        consumer.addVertex(pose, -halfWidth, -halfHeight, 0.0F)
                .setColor(r, g, b, a)
                .setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, halfWidth, -halfHeight, 0.0F)
                .setColor(r, g, b, a)
                .setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, halfWidth, halfHeight, 0.0F)
                .setColor(r, g, b, a)
                .setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, -halfWidth, halfHeight, 0.0F)
                .setColor(r, g, b, a)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        poseStack.popPose();
    }

    private static void renderFireball(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            Vec3 center,
            float age,
            MushroomCloudProfile profile,
            float alpha,
            float quadSize,
            ResourceLocation texture
    ) {
        float life = Mth.clamp(age / profile.fireballTicks(), 0.0F, 1.0F);
        float size = quadSize * (1.4F + life * (2.2F + profile.tier().tier() * 0.45F));
        float fireAlpha = alpha * (1.0F - life * 0.25F);

        for (int shell = 0; shell < 5; shell++) {
            float shellSize = size * (0.5F + shell * 0.18F);
            float r = 1.0F;
            float g = 0.38F + shell * 0.06F;
            float b = 0.08F + shell * 0.03F;
            drawBillboard(
                    poseStack,
                    buffer,
                    camera,
                    center.x,
                    center.y + 1.0D + shell * 0.45D,
                    center.z,
                    shellSize,
                    r,
                    g,
                    b,
                    fireAlpha * (0.98F - shell * 0.12F),
                    texture
            );
        }

        drawRing(
                poseStack,
                buffer,
                camera,
                center.x,
                center.y + 1.8D,
                center.z,
                profile.stemBaseRadius() * (1.2D + life * 1.8D),
                10 + profile.tier().tier() * 4,
                quadSize * 0.75F,
                1.0F,
                0.42F,
                0.1F,
                fireAlpha * 0.85F,
                texture
        );
    }

    private static void renderStem(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            Vec3 center,
            MushroomCloudProfile profile,
            float stemProgress,
            int layers,
            int segments,
            float quadSize,
            float alpha,
            int seed,
            int tier,
            ResourceLocation texture
    ) {
        for (int layer = 0; layer < layers; layer++) {
            float layerT = layer / (float) (layers - 1);
            float layerStep = 1.0F / Math.max(1, layers - 1);
            float layerFade = smoothstep(layerT - layerStep, layerT + layerStep * 0.2F, stemProgress);
            if (layerFade <= 0.01F) {
                continue;
            }

            double y = center.y + 0.8D + profile.stemHeight() * layerT;
            double radius = Mth.lerp(layerT, profile.stemBaseRadius(), profile.stemTopRadius());
            float layerAlpha = alpha * (0.72F + layerT * 0.18F) * layerFade;
            float r = 0.42F + layerT * 0.12F;
            float g = 0.40F + layerT * 0.11F;
            float b = 0.36F + layerT * 0.10F;
            float size = quadSize * (0.95F + layerT * 0.15F);
            int layerSalt = layer * 17;

            drawOrganicRing(
                    poseStack,
                    buffer,
                    camera,
                    center.x,
                    y,
                    center.z,
                    radius,
                    segments,
                    size,
                    r,
                    g,
                    b,
                    layerAlpha,
                    texture,
                    seed,
                    layerSalt,
                    tier,
                    MushroomCloudVariation.STEM
            );
            float coreSize = size * MushroomCloudVariation.sizeMultiplier(seed, layerSalt + 900, MushroomCloudVariation.STEM);
            drawBillboard(
                    poseStack,
                    buffer,
                    camera,
                    center.x,
                    y + MushroomCloudVariation.yOffset(seed, layerSalt + 901, MushroomCloudVariation.STEM),
                    center.z,
                    coreSize * 0.85F,
                    r,
                    g,
                    b,
                    layerAlpha * 0.9F,
                    texture
            );
        }
    }

    private static void renderCap(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            double centerX,
            double capY,
            double centerZ,
            MushroomCloudProfile profile,
            float emergeT,
            float spreadT,
            int shells,
            int segments,
            float quadSize,
            float alpha,
            int seed,
            int tier,
            ResourceLocation texture
    ) {
        double stemTopRadius = profile.stemTopRadius();
        for (int shell = 0; shell < shells; shell++) {
            float shellT = shell / (float) Math.max(1, shells - 1);
            double emergeRadius = Mth.lerp(emergeT, stemTopRadius * 0.82D, stemTopRadius * 1.55D);
            double spreadRadius = profile.capRadius() * (0.18D + shellT * 0.82D) * spreadT;
            double radius = emergeRadius + spreadRadius;
            double y = capY + shellT * (1.4D + profile.tier().tier() * 0.3D) * (0.35D + spreadT * 0.65D);
            float shellAlpha = alpha * (0.85F - shellT * 0.15F);
            float r = 0.58F + shellT * 0.08F;
            float g = 0.56F + shellT * 0.07F;
            float b = 0.52F + shellT * 0.06F;
            float size = quadSize * (1.05F + shellT * 0.35F);

            drawOrganicRing(
                    poseStack,
                    buffer,
                    camera,
                    centerX,
                    y,
                    centerZ,
                    radius,
                    segments,
                    size,
                    r,
                    g,
                    b,
                    shellAlpha,
                    texture,
                    seed,
                    100 + shell * 23,
                    tier,
                    MushroomCloudVariation.CAP
            );
        }

        float coreSize = quadSize
                * (0.45F + emergeT * 0.55F + spreadT * 0.75F)
                * MushroomCloudVariation.sizeMultiplier(seed, 999, MushroomCloudVariation.CAP_CORE);
        drawBillboard(
                poseStack,
                buffer,
                camera,
                centerX,
                capY + 0.6D + MushroomCloudVariation.yOffset(seed, 998, MushroomCloudVariation.CAP_CORE),
                centerZ,
                coreSize,
                0.68F,
                0.66F,
                0.62F,
                alpha * 0.75F,
                texture
        );
    }

    private static void renderRoll(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            double centerX,
            double rollY,
            double centerZ,
            MushroomCloudProfile profile,
            float rollProgress,
            int segments,
            float quadSize,
            float alpha,
            int seed,
            int tier,
            ResourceLocation texture
    ) {
        double radius = profile.capRadius() * (0.75D + rollProgress * 0.55D);
        float rollAlpha = alpha * (0.65F + rollProgress * 0.2F);
        drawOrganicRing(
                poseStack,
                buffer,
                camera,
                centerX,
                rollY,
                centerZ,
                radius,
                segments,
                quadSize * 0.95F,
                0.50F,
                0.48F,
                0.44F,
                rollAlpha,
                texture,
                seed,
                200,
                tier,
                MushroomCloudVariation.ROLL
        );
        drawOrganicRing(
                poseStack,
                buffer,
                camera,
                centerX,
                rollY - 0.8D,
                centerZ,
                radius * 1.05D,
                Math.max(8, segments - 4),
                quadSize * 0.85F,
                0.46F,
                0.44F,
                0.40F,
                rollAlpha * 0.85F,
                texture,
                seed,
                201,
                tier,
                MushroomCloudVariation.ROLL
        );
    }

    private static void drawOrganicRing(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            double centerX,
            double y,
            double centerZ,
            double radius,
            int segments,
            float quadSize,
            float r,
            float g,
            float b,
            float alpha,
            ResourceLocation texture,
            int seed,
            int layerSalt,
            int tier,
            MushroomCloudVariation.Params variation
    ) {
        for (int i = 0; i < segments; i++) {
            double angle = (Math.PI * 2.0D * i) / segments;
            int puffSalt = layerSalt * 1000 + i * 2;
            double ringRadius = MushroomCloudVariation.ringRadius(radius, angle, seed, puffSalt, tier, variation);
            double yOffset = MushroomCloudVariation.yOffset(seed, puffSalt + 1, variation);
            float size = quadSize * MushroomCloudVariation.sizeMultiplier(seed, puffSalt, variation);
            double x = centerX + Math.cos(angle) * ringRadius;
            double z = centerZ + Math.sin(angle) * ringRadius;
            drawBillboard(poseStack, buffer, camera, x, y + yOffset, z, size, r, g, b, alpha, texture);

            double midAngle = angle + Math.PI / segments;
            int midSalt = puffSalt + 500;
            double midRadius = MushroomCloudVariation.ringRadius(radius * 0.92D, midAngle, seed, midSalt, tier, variation);
            double midX = centerX + Math.cos(midAngle) * midRadius;
            double midZ = centerZ + Math.sin(midAngle) * midRadius;
            float midSize = quadSize * 0.82F * MushroomCloudVariation.sizeMultiplier(seed, midSalt, variation);
            drawBillboard(
                    poseStack,
                    buffer,
                    camera,
                    midX,
                    y + MushroomCloudVariation.yOffset(seed, midSalt + 1, variation),
                    midZ,
                    midSize,
                    r,
                    g,
                    b,
                    alpha * 0.85F,
                    texture
            );
        }
    }

    private static void drawRing(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            double centerX,
            double y,
            double centerZ,
            double radius,
            int segments,
            float quadSize,
            float r,
            float g,
            float b,
            float alpha,
            ResourceLocation texture
    ) {
        for (int i = 0; i < segments; i++) {
            double angle = (Math.PI * 2.0D * i) / segments;
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            drawBillboard(poseStack, buffer, camera, x, y, z, quadSize, r, g, b, alpha, texture);
            double midAngle = angle + Math.PI / segments;
            double midX = centerX + Math.cos(midAngle) * radius * 0.92D;
            double midZ = centerZ + Math.sin(midAngle) * radius * 0.92D;
            drawBillboard(
                    poseStack,
                    buffer,
                    camera,
                    midX,
                    y,
                    midZ,
                    quadSize * 0.82F,
                    r,
                    g,
                    b,
                    alpha * 0.85F,
                    texture
            );
        }
    }

    private static void drawBillboard(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            double x,
            double y,
            double z,
            float halfSize,
            float r,
            float g,
            float b,
            float a,
            ResourceLocation texture
    ) {
        if (a <= 0.01F) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(camera.rotation());

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        int light = LightTexture.FULL_BRIGHT;

        consumer.addVertex(pose, -halfSize, -halfSize, 0.0F)
                .setColor(r, g, b, a)
                .setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, halfSize, -halfSize, 0.0F)
                .setColor(r, g, b, a)
                .setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, halfSize, halfSize, 0.0F)
                .setColor(r, g, b, a)
                .setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, -halfSize, halfSize, 0.0F)
                .setColor(r, g, b, a)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        poseStack.popPose();
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
