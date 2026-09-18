package com.createnucleararmaments.client;

import com.createnucleararmaments.client.render.MushroomCloudEffect;
import com.createnucleararmaments.munitions.MushroomCloudProfile;
import com.createnucleararmaments.munitions.NuclearTier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ClientMushroomCloudManager {
    private static final int MAX_CLOUDS = 3;
    private static final CopyOnWriteArrayList<MushroomCloudEffect> CLOUDS = new CopyOnWriteArrayList<>();

    private ClientMushroomCloudManager() {
    }

    public static void schedule(Vec3 center, NuclearTier tier, long startTick) {
        for (MushroomCloudEffect cloud : CLOUDS) {
            if (cloud.matches(center, tier, startTick)) {
                return;
            }
        }

        while (CLOUDS.size() >= MAX_CLOUDS) {
            CLOUDS.remove(0);
        }
        CLOUDS.add(new MushroomCloudEffect(center, MushroomCloudProfile.forTier(tier), startTick));
        ClientNuclearAtmosphere.punch(ClientNuclearAtmosphere.NuclearAtmospherePunch.forTier(tier.tier()));
    }

    public static void tick(Level level) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }

        long gameTime = level.getGameTime();
        for (MushroomCloudEffect cloud : CLOUDS) {
            if (cloud.isExpired(gameTime)) {
                CLOUDS.remove(cloud);
                continue;
            }
            try {
                cloud.tickAccentParticles(clientLevel, gameTime);
            } catch (Throwable t) {
                // Accent particles must never take down the client.
                CLOUDS.remove(cloud);
            }
        }
    }

    public static List<MushroomCloudEffect> activeClouds() {
        return CLOUDS;
    }

    public static void clear() {
        CLOUDS.clear();
    }
}
