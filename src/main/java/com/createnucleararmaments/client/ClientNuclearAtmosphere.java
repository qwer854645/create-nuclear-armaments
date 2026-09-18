package com.createnucleararmaments.client;

import com.createnucleararmaments.CreateNuclearArmaments;
import com.createnucleararmaments.client.render.MushroomCloudEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Near-blast atmosphere: denser fog, grey-red tint, light camera shake for pressure.
 */
@EventBusSubscriber(modid = CreateNuclearArmaments.MOD_ID, value = Dist.CLIENT)
public final class ClientNuclearAtmosphere {
    private static float shakeTicks;
    private static float fogStrength;

    private ClientNuclearAtmosphere() {
    }

    public static void punch(NuclearAtmospherePunch punch) {
        shakeTicks = Math.max(shakeTicks, punch.shakeTicks());
        fogStrength = Math.max(fogStrength, punch.fogStrength());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (shakeTicks > 0.0F) {
            shakeTicks = Math.max(0.0F, shakeTicks - 1.0F);
        }
        // Keep fog while any mushroom cloud is nearby / active, then ease out.
        float target = 0.0F;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            Vec3 eye = mc.player.getEyePosition();
            for (MushroomCloudEffect cloud : ClientMushroomCloudManager.activeClouds()) {
                float age = cloud.ageTicks(mc.level.getGameTime(), 0.0F);
                if (age < 0.0F || age > cloud.profile().endTicks()) {
                    continue;
                }
                double dist = eye.distanceTo(cloud.center());
                float radius = cloud.profile().tier().radiationRadius() * 1.4F;
                if (dist < radius) {
                    float near = 1.0F - (float) (dist / radius);
                    float life = 1.0F - age / cloud.profile().endTicks();
                    target = Math.max(target, near * (0.45F + 0.55F * life));
                }
            }
        }
        fogStrength = Mth.lerp(0.12F, fogStrength, Math.max(target, fogStrength * 0.92F));
        if (fogStrength < 0.01F) {
            fogStrength = 0.0F;
        }
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        if (fogStrength <= 0.01F) {
            return;
        }
        float s = fogStrength;
        float start = Mth.lerp(s, event.getNearPlaneDistance(), 2.0F);
        float end = Mth.lerp(s, event.getFarPlaneDistance(), Math.max(28.0F, event.getFarPlaneDistance() * (1.0F - 0.72F * s)));
        event.setNearPlaneDistance(start);
        event.setFarPlaneDistance(Math.max(start + 8.0F, end));
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (fogStrength <= 0.01F) {
            return;
        }
        float s = fogStrength;
        // Early red-grey flash pressure -> later dead ash grey.
        float flash = Mth.clamp(shakeTicks / 40.0F, 0.0F, 1.0F);
        float r = Mth.lerp(s, event.getRed(), Mth.lerp(flash, 0.28F, 0.55F));
        float g = Mth.lerp(s, event.getGreen(), Mth.lerp(flash, 0.26F, 0.32F));
        float b = Mth.lerp(s, event.getBlue(), Mth.lerp(flash, 0.24F, 0.22F));
        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (shakeTicks <= 0.0F) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        float intensity = Mth.clamp(shakeTicks / 18.0F, 0.0F, 1.0F) * 1.8F;
        long t = mc.level.getGameTime();
        event.setYaw(event.getYaw() + (float) Math.sin(t * 1.7D) * intensity);
        event.setPitch(event.getPitch() + (float) Math.cos(t * 2.1D) * intensity * 0.65F);
        event.setRoll((float) Math.sin(t * 2.6D) * intensity * 0.35F);
    }

    public record NuclearAtmospherePunch(float shakeTicks, float fogStrength) {
        public static NuclearAtmospherePunch forTier(int tier) {
            return new NuclearAtmospherePunch(28.0F + tier * 12.0F, 0.75F + tier * 0.08F);
        }
    }
}
