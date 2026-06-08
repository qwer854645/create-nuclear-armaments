package com.createnucleararmaments;

import com.createnucleararmaments.index.CNAPlacedNuclearDevices;
import com.createnucleararmaments.index.CNACreativeTab;
import com.createnucleararmaments.index.CNAItems;
import com.createnucleararmaments.index.CNAMunitions;
import com.createnucleararmaments.munitions.EdgeFractureScheduler;
import com.createnucleararmaments.munitions.RadiationZoneScheduler;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
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
        CNAMunitions.register();
        CNAPlacedNuclearDevices.register();

        NeoForge.EVENT_BUS.addListener(CreateNuclearArmaments::onServerTick);

        LOGGER.info("Create Nuclear Armaments loading");
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            EdgeFractureScheduler.tick(level);
            RadiationZoneScheduler.tick(level);
        }
    }
}
