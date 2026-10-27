package dev.b173.core;

import dev.b173.core.pack.BetaJarSearch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public final class LocatorTest {
    private LocatorTest() {
    }

    public static void run(TestRunner t) throws IOException {
        t.section("Finding the Beta 1.7.3 jar in every launcher's layout");
        Path base = Files.createTempDirectory("b173-locator");
        try {
            layouts(t, base);
            given(t, base);
            fingerprint(t, base);
        } finally {
            try (Stream<Path> walk = Files.walk(base)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    private static Path fake(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "not really Beta " + file, StandardCharsets.UTF_8);
        return file;
    }

    private static BetaJarSearch.Environment env(Path home, Path game, String os, String appData) {
        return new BetaJarSearch.Environment(game, game.resolve("config/b173.properties"),
                game.resolve("b173/located.properties"), home.toString(), appData, null, null, os, null, null);
    }

    private static void expect(TestRunner t, String name, BetaJarSearch.Environment env, Path jar) {
        BetaJarSearch.Result result = BetaJarSearch.search(env, false);
        boolean ok = result.found() != null && result.found().jar().equals(jar.toAbsolutePath().normalize());
        t.check(name, ok, result.found() == null ? "nothing found" : String.valueOf(result.found().jar()));
    }

    private static void layouts(TestRunner t, Path base) throws IOException {
        int n = 0;
        Path home = base.resolve("h" + n++);
        Path prism = home.resolve("Some Games/My Prism");
        Path jar = fake(prism.resolve("libraries/com/mojang/minecraft/b1.7.3/minecraft-b1.7.3-client.jar"));
        expect(t, "a portable Prism Launcher, from inside its instance", env(home,
                prism.resolve("instances/Beta port/minecraft"), "Windows 11", null), jar);

        home = base.resolve("h" + n++);
        Path multimc = home.resolve("Desktop/MultiMC");
        jar = fake(multimc.resolve("libraries/com/mojang/minecraft/b1.7.3/minecraft-b1.7.3-client.jar"));
        expect(t, "MultiMC, game started from somewhere else entirely", env(home, base.resolve("elsewhere/game"),
                "Windows 10", home.resolve("AppData/Roaming").toString()), jar);

        home = base.resolve("h" + n++);
        jar = fake(home.resolve("AppData/Roaming/.minecraft/versions/b1.7.3/b1.7.3.jar"));
        expect(t, "the Minecraft Launcher on Windows, game in another folder", env(home,
                base.resolve("D drive/Modded/26.1.2"), "Windows 11", home.resolve("AppData/Roaming").toString()), jar);
        home = base.resolve("h" + n++);
        jar = fake(home.resolve("Library/Application Support/minecraft/versions/b1.7.3/b1.7.3.jar"));
        expect(t, "the Minecraft Launcher on macOS", env(home, base.resolve("mac game"), "Mac OS X", null), jar);
        home = base.resolve("h" + n++);
        jar = fake(home.resolve(".minecraft/versions/b1.7.3/b1.7.3.jar"));
        expect(t, "the Minecraft Launcher on Linux", env(home, base.resolve("linux game"), "Linux", null), jar);

        home = base.resolve("h" + n++);
        jar = fake(home.resolve(".var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/libraries/com/mojang/minecraft/b1.7.3/minecraft-b1.7.3-client.jar"));
        expect(t, "Prism Launcher installed through Flatpak", env(home, base.resolve("flat game"), "Linux", null), jar);

        home = base.resolve("h" + n++);
        jar = fake(home.resolve("AppData/Roaming/ModrinthApp/meta/versions/b1.7.3/b1.7.3.jar"));
        expect(t, "the Modrinth App", env(home, base.resolve("m game"), "Windows 11", home.resolve("AppData/Roaming").toString()), jar);
        home = base.resolve("h" + n++);
        jar = fake(home.resolve("curseforge/minecraft/Install/versions/b1.7.3/b1.7.3.jar"));
        expect(t, "the CurseForge app", env(home, base.resolve("c game"), "Windows 11", home.resolve("AppData/Roaming").toString()), jar);

        home = base.resolve("h" + n++);
        jar = fake(home.resolve(".betacraft/versions/b1.7.3.jar"));
        expect(t, "Betacraft", env(home, base.resolve("b game"), "Linux", null), jar);
        home = base.resolve("h" + n++);
        jar = fake(home.resolve("AppData/Roaming/ATLauncher/versions/b1.7.3/b1.7.3.jar"));
        expect(t, "ATLauncher", env(home, base.resolve("a game"), "Windows 11", home.resolve("AppData/Roaming").toString()), jar);

        home = base.resolve("h" + n++);
        Files.createDirectories(home);
        BetaJarSearch.Result none = BetaJarSearch.search(env(home, base.resolve("empty game"), "Linux", null), true);
        t.check("no jar anywhere: nothing found, the fingerprint search ran and every folder is listed",
                none.found() == null && none.scan().ran() && none.roots().size() > 10,
                none.roots().size() + " folders, scan " + none.scan().ran());
    }

    private static void given(TestRunner t, Path base) throws IOException {
        Path home = base.resolve("given");
        Path game = home.resolve("game");
        Path mine = fake(home.resolve("odd place/my beta.jar"));
        fake(home.resolve(".minecraft/versions/b1.7.3/b1.7.3.jar"));
        Files.createDirectories(game.resolve("config"));
        Files.writeString(game.resolve("config/b173.properties"), "betaJar=" + mine + "\n", StandardCharsets.UTF_8);
        expect(t, "betaJar in the config wins over every launcher, whatever the jar", env(home, game, "Linux", null), mine);

        Path folder = home.resolve("Old Launcher Folder");
        Path inside = fake(folder.resolve("versions/b1.7.3/b1.7.3.jar"));
        Files.writeString(game.resolve("config/b173.properties"), "betaJar=\"" + folder + "\"\n", StandardCharsets.UTF_8);
        expect(t, "betaJar naming a launcher folder (in quotes) finds the jar inside it", env(home, game, "Linux", null), inside);

        Files.writeString(game.resolve("config/b173.properties"), "betaJar=" + home.resolve("gone.jar") + "\n",
                StandardCharsets.UTF_8);
        BetaJarSearch.Result result = BetaJarSearch.search(env(home, game, "Linux", null), false);
        boolean listed = result.attempts().stream().anyMatch(a -> a.outcome() == BetaJarSearch.Outcome.ABSENT
                && a.place().endsWith("gone.jar"));
        t.check("a betaJar that is not there is reported and the search goes on", listed && result.found() != null,
                String.valueOf(result.found()));
    }

    private static void fingerprint(TestRunner t, Path base) throws IOException {
        String configured = System.getProperty("b173.testBetaJar",
                "/mnt/user-data/uploads/MultiMC/libraries/com/mojang/minecraft/b1.7.3/minecraft-b1.7.3-client.jar");
        Path real = Path.of(configured);
        if (!Files.isRegularFile(real) || !BetaJarSearch.OFFICIAL_SHA1.equals(BetaJarSearch.sha1(real))) {
            System.out.println("  skip  no published Beta jar on this computer for the fingerprint checks");
            return;
        }
        Path home = base.resolve("scan");
        Path renamed = home.resolve("Downloads/old stuff/minecraft beta (1).jar");
        Files.createDirectories(renamed.getParent());
        Files.copy(real, renamed);
        Path game = home.resolve("game");
        BetaJarSearch.Environment env = env(home, game, "Linux", null);
        BetaJarSearch.Result result = BetaJarSearch.search(env, true);
        t.check("a renamed published jar in Downloads is found by its fingerprint",
                result.found() != null && result.found().official() && result.found().jar().equals(renamed),
                String.valueOf(result.found()));
        BetaJarSearch.Result again = BetaJarSearch.search(env, false);
        t.check("and remembered, so the next start goes straight to it",
                again.found() != null && again.found().how().contains("earlier"), again.found() + " " + again.attempts()
                        + " memory=" + BetaJarSearch.configured(env.memoryFile(), "jar") + " first=" + result.scan());

        Path home2 = base.resolve("prefer");
        Path other = fake(home2.resolve(".minecraft/versions/b1.7.3/b1.7.3.jar"));
        Path official = home2.resolve(".betacraft/versions/b1.7.3.jar");
        Files.createDirectories(official.getParent());
        Files.copy(real, official);
        BetaJarSearch.Result preferred = BetaJarSearch.search(env(home2, home2.resolve("game"), "Linux", null), false);
        t.check("the published jar is preferred to another jar found first",
                preferred.found() != null && preferred.found().jar().equals(official)
                        && preferred.attempts().stream().anyMatch(a -> a.place().equals(other.toString())
                        && a.outcome() == BetaJarSearch.Outcome.OTHER_JAR), String.valueOf(preferred.found()));
    }
}
