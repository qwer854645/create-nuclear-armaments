package com.createnucleararmaments.client;

import com.createnucleararmaments.client.render.MushroomCloudEffect;
import com.createnucleararmaments.munitions.MushroomCloudProfile;
import com.createnucleararmaments.munitions.NuclearTier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class ClientMushroomCloudManager {
    private static final int MAX_CLOUDS = 3;
    private static final List<MushroomCloudEffect> CLOUDS = new ArrayList<>();

    private ClientMushroomCloudManager() {
    }

    public static void schedule(Vec3 center, NuclearTier tier, long startTick) {
        if (CLOUDS.size() >= MAX_CLOUDS) {
            CLOUDS.remove(0);
        }
        CLOUDS.add(new MushroomCloudEffect(center, MushroomCloudProfile.forTier(tier), startTick));
    }

    public static void tick(Level level) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }

        long gameTime = level.getGameTime();
        Iterator<MushroomCloudEffect> iterator = CLOUDS.iterator();
        while (iterator.hasNext()) {
            MushroomCloudEffect cloud = iterator.next();
            if (cloud.isExpired(gameTime)) {
                iterator.remove();
                continue;
            }
            cloud.tickAccentParticles(clientLevel, gameTime);
        }
    }

    public static List<MushroomCloudEffect> activeClouds() {
        return Collections.unmodifiableList(CLOUDS);
    }
}
