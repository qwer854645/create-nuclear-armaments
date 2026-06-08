package com.createnucleararmaments.client;

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
            NuclearTier tier = NuclearTier.VALUES[Mth.clamp(payload.tier(), 0, NuclearTier.VALUES.length - 1)];
            ClientMushroomCloudManager.schedule(
                    new Vec3(payload.x(), payload.y(), payload.z()),
                    tier,
                    payload.startTick()
            );
        });
    }
}
