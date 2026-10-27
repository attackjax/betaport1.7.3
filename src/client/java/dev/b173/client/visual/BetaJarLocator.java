package dev.b173.client.visual;

import dev.b173.core.pack.BetaJarSearch;

import java.nio.file.Path;

public final class BetaJarLocator {
    public static final String OFFICIAL_SHA1 = BetaJarSearch.OFFICIAL_SHA1;

    private BetaJarLocator() {
    }

    public static String configured(Path configFile, String key) {
        return BetaJarSearch.configured(configFile, key);
    }
}
