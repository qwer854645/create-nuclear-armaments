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
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || ClientMushroomCloudManager.activeClouds().isEmpty()) {
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
        for (var cloud : ClientMushroomCloudManager.activeClouds()) {
            MushroomCloudRenderer.render(poseStack, buffer, camera, minecraft.level, cloud, gameTime, partialTick);
        }
        poseStack.popPose();
        buffer.endBatch();
    }
}
