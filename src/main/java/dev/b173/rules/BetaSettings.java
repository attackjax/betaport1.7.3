package dev.b173.rules;

import dev.b173.core.pack.BetaJarSearch;
import net.fabricmc.loader.api.FabricLoader;

public final class BetaSettings {
    private static final boolean STRICT_MINING = "true".equalsIgnoreCase(
            BetaJarSearch.configured(FabricLoader.getInstance().getConfigDir().resolve("b173.properties"), "strictMining"));

    private BetaSettings() {
    }

    public static boolean strictMining() {
        return STRICT_MINING;
    }
}
