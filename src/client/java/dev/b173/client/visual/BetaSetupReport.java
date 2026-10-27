package dev.b173.client.visual;

import dev.b173.core.pack.BetaJarSearch;

import java.nio.file.Path;
import java.util.List;

public record BetaSetupReport(BetaJarSearch.Result jar, BetaSoundsLocator.Found sounds, List<String> soundPlaces,
                              Path packFolder, String problem, Path configFile, long millis) {
    public boolean jarFound() {
        return jar.found() != null;
    }

    public boolean complete() {
        return jarFound() && jar.found().official() && sounds != null && packFolder != null;
    }

    public String signature() {
        return (jar.found() == null ? "no jar" : jar.found().jar() + "@" + jar.found().sha1())
                + "|" + (sounds == null ? "no sounds" : sounds.where())
                + "|" + (packFolder == null ? "no pack" : "pack");
    }
}
