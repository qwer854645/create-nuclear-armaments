package com.createnucleararmaments.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.createnucleararmaments.client.CNAClientNetwork;
import com.createnucleararmaments.munitions.MushroomCloudProfile;
import com.createnucleararmaments.munitions.NuclearTier;
import net.minecraft.server.level.ServerPlayer;

public final class CNANetwork {
    private CNANetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registrar.playToClient(
                    MushroomCloudPayload.TYPE,
                    MushroomCloudPayload.STREAM_CODEC,
                    CNAClientNetwork::handleMushroomCloud
            );
        } else {
            registrar.playToClient(
                    MushroomCloudPayload.TYPE,
                    MushroomCloudPayload.STREAM_CODEC,
                    (payload, context) -> {}
            );
        }
    }

    public static void sendMushroomCloud(ServerLevel level, Vec3 center, NuclearTier tier) {
        double viewRadius = Math.max(192.0D, tier.blastRadius() * 5.0D);
        long startTick = level.getGameTime() - MushroomCloudProfile.SYNC_LEAD_TICKS;
        MushroomCloudPayload payload = new MushroomCloudPayload(
                center.x,
                center.y,
                center.z,
                tier.ordinal(),
                startTick
        );
        double viewRadiusSqr = viewRadius * viewRadius;

        for (ServerPlayer player : level.players()) {
            if (player.level() != level) {
                continue;
            }
            if (player.position().distanceToSqr(center) <= viewRadiusSqr) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}
