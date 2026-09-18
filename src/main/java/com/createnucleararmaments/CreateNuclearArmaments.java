package com.createnucleararmaments;

import com.createnucleararmaments.config.CNAConfig;
import com.createnucleararmaments.compat.CbcCompat;
import com.createnucleararmaments.index.CNAPlacedNuclearDevices;
import com.createnucleararmaments.index.CNACreativeTab;
import com.createnucleararmaments.index.CNAItems;
import com.createnucleararmaments.index.CNAParticles;
import com.createnucleararmaments.munitions.EdgeFractureScheduler;
import com.createnucleararmaments.network.CNANetwork;
import com.createnucleararmaments.munitions.FalloutVegetationScheduler;
import com.createnucleararmaments.munitions.MushroomCloudParticleScheduler;
import com.createnucleararmaments.munitions.RadiationZoneScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CreateNuclearArmaments.MOD_ID)
public class CreateNuclearArmaments {
    public static final String MOD_ID = "createnucleararmaments";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public CreateNuclearArmaments(IEventBus modEventBus, ModContainer modContainer) {
        CNArmaments.REGISTRATE.registerEventListeners(modEventBus);
        CNACreativeTab.TABS.register(modEventBus);

        CNAItems.register();
        CbcCompat.registerMunitions();
        CNAPlacedNuclearDevices.register();
        CNAParticles.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, CNAConfig.SERVER_SPEC);
        modEventBus.addListener(CNANetwork::register);

        NeoForge.EVENT_BUS.addListener(CreateNuclearArmaments::onServerTick);
        NeoForge.EVENT_BUS.addListener(CreateNuclearArmaments::onServerStopping);
        NeoForge.EVENT_BUS.addListener(CreateNuclearArmaments::onLevelUnload);

        LOGGER.info("Create Nuclear Armaments loading");
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            MushroomCloudParticleScheduler.tick(level);
            EdgeFractureScheduler.tick(level);
            RadiationZoneScheduler.tick(level);
            FalloutVegetationScheduler.tick(level);
        }
    }

    private static void onServerStopping(ServerStoppingEvent event) {
        MushroomCloudParticleScheduler.clearAll();
        EdgeFractureScheduler.clearAll();
        RadiationZoneScheduler.clearAll();
        FalloutVegetationScheduler.clearAll();
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        LevelAccessor level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            MushroomCloudParticleScheduler.clearLevel(serverLevel);
            EdgeFractureScheduler.clearLevel(serverLevel);
            RadiationZoneScheduler.clearLevel(serverLevel);
            FalloutVegetationScheduler.clearLevel(serverLevel);
        }
    }
}
