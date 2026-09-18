package com.createnucleararmaments.index;

import com.createnucleararmaments.CreateNuclearArmaments;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CNAParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, CreateNuclearArmaments.MOD_ID);

    /** Large pixel-art mushroom smoke; always-show capable. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MUSHROOM_SMOKE =
            PARTICLE_TYPES.register("mushroom_smoke", () -> new SimpleParticleType(true));

    private CNAParticles() {
    }

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
