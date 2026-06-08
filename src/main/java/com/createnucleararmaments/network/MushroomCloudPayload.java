package com.createnucleararmaments.network;

import com.createnucleararmaments.CNArmaments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MushroomCloudPayload(double x, double y, double z, int tier, long startTick) implements CustomPacketPayload {
    public static final Type<MushroomCloudPayload> TYPE = new Type<>(CNArmaments.id("mushroom_cloud"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MushroomCloudPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, MushroomCloudPayload::x,
            ByteBufCodecs.DOUBLE, MushroomCloudPayload::y,
            ByteBufCodecs.DOUBLE, MushroomCloudPayload::z,
            ByteBufCodecs.VAR_INT, MushroomCloudPayload::tier,
            ByteBufCodecs.VAR_LONG, MushroomCloudPayload::startTick,
            MushroomCloudPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
