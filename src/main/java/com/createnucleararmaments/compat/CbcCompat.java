package com.createnucleararmaments.compat;

import com.createnucleararmaments.CreateNuclearArmaments;
import net.neoforged.fml.ModList;

/**
 * Soft gates for Create Big Cannons / CBC More Shells.
 * Classes that hard-reference CBC APIs must only be loaded through {@link #registerMunitions()}.
 */
public final class CbcCompat {
    public static final String CBC_MOD_ID = "createbigcannons";
    public static final String CBCMS_MOD_ID = "cbcmoreshells";

    private CbcCompat() {
    }

    public static boolean isCbcLoaded() {
        return ModList.get().isLoaded(CBC_MOD_ID);
    }

    public static boolean isCbcmsLoaded() {
        return ModList.get().isLoaded(CBCMS_MOD_ID);
    }

    public static boolean isAnyMunitionHostLoaded() {
        return isCbcLoaded() || isCbcmsLoaded();
    }

    /**
     * Registers nuclear CBC / CBCMS munitions when at least one host mod is present.
     * Uses reflection so the main mod class never classloads CBC types when absent.
     */
    public static void registerMunitions() {
        if (!isAnyMunitionHostLoaded()) {
            CreateNuclearArmaments.LOGGER.info("CBC / CBC More Shells not present; skipping nuclear munition registration");
            return;
        }
        try {
            Class<?> munitions = Class.forName("com.createnucleararmaments.index.CNAMunitions");
            munitions.getMethod("register").invoke(null);
            CreateNuclearArmaments.LOGGER.info(
                    "Registered nuclear munitions (CBC={}, CBCMS={})",
                    isCbcLoaded(),
                    isCbcmsLoaded()
            );
        } catch (ReflectiveOperationException ex) {
            CreateNuclearArmaments.LOGGER.error("Failed to register CBC / CBCMS nuclear munitions", ex);
        }
    }
}
