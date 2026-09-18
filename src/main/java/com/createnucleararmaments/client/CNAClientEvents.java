package com.createnucleararmaments.client;

import com.createnucleararmaments.CreateNuclearArmaments;
import com.createnucleararmaments.client.render.MushroomCloudRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = CreateNuclearArmaments.MOD_ID, value = Dist.CLIENT)
public final class CNAClientEvents {
    private CNAClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            ClientMushroomCloudManager.tick(minecraft.level);
        } else {
            ClientMushroomCloudManager.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        if (ClientMushroomCloudManager.activeClouds().isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        var poseStack = event.getPoseStack();
        var camera = event.getCamera();
        var buffer = minecraft.renderBuffers().bufferSource();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        long gameTime = minecraft.level.getGameTime();
        var camPos = camera.getPosition();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        try {
            for (var cloud : ClientMushroomCloudManager.activeClouds()) {
                try {
                    MushroomCloudRenderer.render(poseStack, buffer, camera, cloud, gameTime, partialTick);
                } catch (Throwable t) {
                    CreateNuclearArmaments.LOGGER.warn("Skipped one mushroom cloud frame", t);
                }
            }
            buffer.endBatch();
        } finally {
            poseStack.popPose();
        }
    }
}
