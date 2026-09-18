package com.createnucleararmaments.client;

import com.createnucleararmaments.CreateNuclearArmaments;
import com.createnucleararmaments.client.particle.MushroomSmokeParticle;
import com.createnucleararmaments.index.CNAParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = CreateNuclearArmaments.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CNAClientParticles {
    private CNAClientParticles() {
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CNAParticles.MUSHROOM_SMOKE.get(), MushroomSmokeParticle.Provider::new);
    }
}
