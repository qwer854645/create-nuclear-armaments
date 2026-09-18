package com.createnucleararmaments.compat;

import net.neoforged.fml.ModList;

public final class CreateCompat {
    public static final String CREATE_MOD_ID = "create";

    private CreateCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(CREATE_MOD_ID);
    }
}
