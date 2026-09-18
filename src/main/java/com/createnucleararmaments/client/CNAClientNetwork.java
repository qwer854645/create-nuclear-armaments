package com.createnucleararmaments.client;

import com.createnucleararmaments.CreateNuclearArmaments;
import com.createnucleararmaments.munitions.NuclearTier;
import com.createnucleararmaments.network.MushroomCloudPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class CNAClientNetwork {
    private CNAClientNetwork() {
    }

    public static void handleMushroomCloud(MushroomCloudPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            NuclearTier tier = NuclearTier.VALUES[Mth.clamp(payload.tier(), 0, NuclearTier.VALUES.length - 1)];
            long startTick = minecraft.level.getGameTime();
            ClientMushroomCloudManager.schedule(
                    new Vec3(payload.x(), payload.y(), payload.z()),
                    tier,
                    startTick
            );
            CreateNuclearArmaments.LOGGER.info(
                    "Client mushroom cloud scheduled at {},{},{} tier={}",
                    payload.x(), payload.y(), payload.z(), tier.suffix()
            );
        });
    }
}
