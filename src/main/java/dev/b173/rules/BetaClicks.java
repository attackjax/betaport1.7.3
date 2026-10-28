package dev.b173.rules;

import net.fabricmc.loader.api.FabricLoader;

public final class BetaClicks {
    private static final boolean MOUSE_TWEAKS = FabricLoader.getInstance().isModLoaded("mousetweaks");

    private BetaClicks() {
    }

    public static boolean enforced() {
        return !MOUSE_TWEAKS;
    }
}
