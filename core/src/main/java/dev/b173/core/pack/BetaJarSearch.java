package dev.b173.core.pack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class BetaJarSearch {
    public static final String OFFICIAL_SHA1 = "43db9b498cb67058d2e12d394e6507722e71bb45";
    public static final long OFFICIAL_SIZE = 1_465_375L;

    static final List<String> JAR_LAYOUTS = List.of(
            "versions/b1.7.3/b1.7.3.jar",
            "libraries/com/mojang/minecraft/b1.7.3/minecraft-b1.7.3-client.jar",
            "meta/versions/b1.7.3/b1.7.3.jar",
            "Install/versions/b1.7.3/b1.7.3.jar",
            "install/versions/b1.7.3/b1.7.3.jar",
            "data/versions/b1.7.3/b1.7.3.jar",
            "versions/b1.7.3.jar",
            ".minecraft/versions/b1.7.3/b1.7.3.jar",
            "minecraft/versions/b1.7.3/b1.7.3.jar",
            "bin/minecraft.jar",
            "b1.7.3.jar",
            "minecraft-b1.7.3-client.jar");

    private static final int ANCESTORS = 8;
    private static final int SCAN_DEPTH_LAUNCHER = 8;
    private static final int SCAN_DEPTH_PERSONAL = 4;
    private static final int SCAN_MAX_ENTRIES = 250_000;
    private static final long SCAN_MAX_MILLIS = 5_000L;

    private BetaJarSearch() {
    }

    public record Environment(Path gameDir, Path configFile, Path memoryFile, String home, String appData,
                              String localAppData, String xdgDataHome, String os, String property, String variable) {
        public static Environment of(Path gameDir, Path configFile, Path memoryFile) {
            return new Environment(gameDir, configFile, memoryFile, System.getProperty("user.home", ""),
                    System.getenv("APPDATA"), System.getenv("LOCALAPPDATA"), System.getenv("XDG_DATA_HOME"),
                    System.getProperty("os.name", ""), System.getProperty("b173.betaJar"), System.getenv("B173_BETA_JAR"));
        }

        boolean windows() {
            return os.toLowerCase(Locale.ROOT).contains("win");
        }

        boolean mac() {
            return os.toLowerCase(Locale.ROOT).contains("mac");
        }
    }

    public record Attempt(String place, String why, Outcome outcome, String detail) {
    }

    public enum Outcome {
        FOUND,
        OTHER_JAR,
        ABSENT,
        UNREADABLE
    }

    public record Found(Path jar, String sha1, String how) {
        public boolean official() {
            return OFFICIAL_SHA1.equals(sha1);
        }
    }

    public record Root(String folder, String why, boolean exists) {
    }

    public record Result(Found found, List<Attempt> attempts, List<Root> roots, int layouts, Scan scan) {
    }

    public record Scan(boolean ran, String why, List<String> roots, int folders, int files, int candidates,
                       long millis, boolean cutShort) {
        static Scan skipped(String why) {
            return new Scan(false, why, List.of(), 0, 0, 0, 0, false);
        }
    }

    public static Result search(Environment env, boolean allowScan) {
        List<Attempt> attempts = new ArrayList<>();
        Map<Path, Boolean> seen = new LinkedHashMap<>();

        Found given = given(env.property(), "the b173.betaJar Java setting", attempts);
        if (given == null) {
            given = given(env.variable(), "the B173_BETA_JAR environment variable", attempts);
        }
        if (given == null) {
            given = given(configured(env.configFile(), "betaJar"), "betaJar in " + env.configFile().getFileName(), attempts);
        }
        if (given != null) {
            return new Result(given, attempts, List.of(), JAR_LAYOUTS.size(), Scan.skipped("a path was given"));
        }

        String remembered = configured(env.memoryFile(), "jar");
        if (remembered != null) {
            Path last = path(remembered);
            if (last != null && !Files.isRegularFile(last)) {
                attempts.add(new Attempt(last.toString(), "where an earlier search found it", Outcome.ABSENT,
                        "no longer there"));
            }
            Found found = check(last, "where an earlier search found it", attempts, seen);
            if (found != null && found.official()) {
                return new Result(found, attempts, List.of(), JAR_LAYOUTS.size(),
                        Scan.skipped("found where an earlier search had found it"));
            }
        }

        Set<Path> roots = launcherRoots(env);
        List<Root> rootList = new ArrayList<>();
        Found fallback = null;
        for (Path root : roots) {
            rootList.add(new Root(root.toString(), describe(root, env), Files.isDirectory(root)));
            for (String layout : JAR_LAYOUTS) {
                Found found = check(root.resolve(layout), describe(root, env), attempts, seen);
                if (found != null) {
                    if (found.official()) {
                        remember(env, found);
                        return new Result(found, attempts, rootList, JAR_LAYOUTS.size(),
                                Scan.skipped("found in a launcher folder"));
                    }
                    if (fallback == null) {
                        fallback = found;
                    }
                }
            }
        }

        Scan scan;
        if (!allowScan) {
            scan = Scan.skipped("an earlier search of these folders found nothing; \"Search again\" repeats it");
        } else {
            List<Path> scanRoots = new ArrayList<>();
            List<Integer> depths = new ArrayList<>();
            for (Path root : roots) {
                if (Files.isDirectory(root) && looksLikeLauncher(root)) {
                    scanRoots.add(root);
                    depths.add(SCAN_DEPTH_LAUNCHER);
                }
            }
            for (Path folder : personalFolders(env)) {
                if (Files.isDirectory(folder)) {
                    scanRoots.add(folder);
                    depths.add(SCAN_DEPTH_PERSONAL);
                }
            }
            ScanState state = new ScanState(System.currentTimeMillis());
            Found found = null;
            for (int i = 0; i < scanRoots.size() && found == null && !state.cutShort; i++) {
                found = scan(scanRoots.get(i), depths.get(i), state, attempts, seen);
            }
            List<String> shown = new ArrayList<>();
            for (Path root : scanRoots) {
                shown.add(root.toString());
            }
            scan = new Scan(true, "", shown, state.folders, state.files, state.candidates,
                    System.currentTimeMillis() - state.start, state.cutShort);
            if (found != null) {
                remember(env, found);
                return new Result(found, attempts, rootList, JAR_LAYOUTS.size(), scan);
            }
            if (!state.cutShort) {
                rememberEmptyScan(env);
            }
        }
        return new Result(fallback, attempts, rootList, JAR_LAYOUTS.size(), scan);
    }

    public static Set<Path> launcherRoots(Environment env) {
        Set<Path> roots = new LinkedHashSet<>();
        Path dir = env.gameDir().toAbsolutePath().normalize();
        for (int i = 0; dir != null && i <= ANCESTORS; i++, dir = dir.getParent()) {
            roots.add(dir);
        }
        String home = env.home();
        List<Path> data = new ArrayList<>();
        if (env.appData() != null && !env.appData().isBlank()) {
            data.add(Path.of(env.appData()));
        }
        if (env.localAppData() != null && !env.localAppData().isBlank()) {
            data.add(Path.of(env.localAppData()));
        }
        if (!home.isEmpty()) {
            if (env.mac()) {
                data.add(Path.of(home, "Library", "Application Support"));
            } else if (!env.windows()) {
                data.add(env.xdgDataHome() != null && !env.xdgDataHome().isBlank()
                        ? Path.of(env.xdgDataHome()) : Path.of(home, ".local", "share"));
            }
            if (env.windows()) {
                data.add(Path.of(home, "AppData", "Roaming"));
                data.add(Path.of(home, "AppData", "Local"));
            }
        }
        for (Path base : data) {
            for (String name : new String[]{".minecraft", "minecraft", "PrismLauncher", "PolyMC", "MultiMC", "multimc",
                    "ModrinthApp", "com.modrinth.theseus", "ATLauncher", "atlauncher", "gdlauncher_carbon",
                    "gdlauncher_next", ".technic", "technic", ".betacraft", "betacraft", "BetaCraft",
                    ".tlauncher", "Programs/PrismLauncher", "Programs/MultiMC"}) {
                roots.add(base.resolve(name));
            }
            roots.add(base.resolve("gdlauncher_carbon").resolve("data"));
        }
        if (!home.isEmpty()) {
            Path h = Path.of(home);
            roots.add(h.resolve(".minecraft"));
            roots.add(h.resolve(".betacraft"));
            roots.add(h.resolve(".atlauncher"));
            roots.add(h.resolve(".technic"));
            roots.add(h.resolve("curseforge").resolve("minecraft"));
            roots.add(h.resolve("Documents").resolve("Curseforge").resolve("Minecraft"));
            roots.add(h.resolve(".var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher"));
            roots.add(h.resolve(".var/app/org.polymc.PolyMC/data/PolyMC"));
            roots.add(h.resolve(".var/app/com.modrinth.ModrinthApp/data/ModrinthApp"));
            roots.add(h.resolve(".var/app/com.mojang.Minecraft/.minecraft"));
            roots.add(h.resolve(".var/app/org.betacraft.BetaCraft/data/betacraft"));
            for (String folder : new String[]{"MultiMC", "PrismLauncher", "PolyMC", "ATLauncher", "Desktop/MultiMC",
                    "Desktop/PrismLauncher", "Desktop/ATLauncher", "Downloads/MultiMC", "Downloads/PrismLauncher",
                    "Documents/MultiMC", "Documents/PrismLauncher", "Games/MultiMC", "Games/PrismLauncher",
                    "Games/Minecraft", "Games/.minecraft"}) {
                roots.add(h.resolve(folder));
            }
        }
        if (env.windows()) {
            for (String folder : new String[]{"C:/MultiMC", "C:/PrismLauncher", "C:/Games/MultiMC",
                    "C:/Games/PrismLauncher", "C:/Games/Minecraft", "C:/Minecraft", "C:/ATLauncher",
                    "D:/MultiMC", "D:/PrismLauncher", "D:/Games/MultiMC", "D:/Games/Minecraft"}) {
                roots.add(Path.of(folder));
            }
        }
        return roots;
    }

    static List<Path> personalFolders(Environment env) {
        List<Path> out = new ArrayList<>();
        if (env.home().isEmpty()) {
            return out;
        }
        Path h = Path.of(env.home());
        for (String folder : new String[]{"Desktop", "Downloads", "Documents", "Games", "OneDrive/Desktop",
                "OneDrive/Documents"}) {
            out.add(h.resolve(folder));
        }
        return out;
    }

    private static String describe(Path root, Environment env) {
        Path game = env.gameDir().toAbsolutePath().normalize();
        if (game.startsWith(root)) {
            return root.equals(game) ? "the game folder" : "a folder holding the game folder";
        }
        return "a launcher's usual folder";
    }

    private static Found given(String value, String why, List<Attempt> attempts) {
        if (value == null || value.isBlank()) {
            return null;
        }
        Path path = path(value.strip());
        if (path == null) {
            attempts.add(new Attempt(value, why, Outcome.UNREADABLE, "not a path this computer understands"));
            return null;
        }
        if (Files.isDirectory(path)) {
            for (String layout : JAR_LAYOUTS) {
                Path jar = path.resolve(layout);
                if (Files.isRegularFile(jar)) {
                    return read(jar, why + " (a folder)", attempts, true);
                }
            }
            attempts.add(new Attempt(path.toString(), why, Outcome.ABSENT, "a folder with no Beta jar in any known layout"));
            return null;
        }
        if (!Files.isRegularFile(path)) {
            attempts.add(new Attempt(path.toString(), why, Outcome.ABSENT, "no such file"));
            return null;
        }
        return read(path, why, attempts, true);
    }

    private static Found check(Path path, String why, List<Attempt> attempts, Map<Path, Boolean> seen) {
        if (path == null) {
            return null;
        }
        Path key = path.toAbsolutePath().normalize();
        if (seen.putIfAbsent(key, Boolean.TRUE) != null) {
            return null;
        }
        if (!Files.isRegularFile(key)) {
            return null;
        }
        return read(key, why, attempts, false);
    }

    private static Found read(Path jar, String why, List<Attempt> attempts, boolean chosen) {
        try {
            String sha1 = sha1(jar);
            boolean official = OFFICIAL_SHA1.equals(sha1);
            attempts.add(new Attempt(jar.toString(), why, official || chosen ? Outcome.FOUND : Outcome.OTHER_JAR,
                    official ? "the published Beta 1.7.3 client" : "SHA-1 " + sha1 + ", not the published jar"));
            return new Found(jar, sha1, why);
        } catch (IOException | RuntimeException e) {
            attempts.add(new Attempt(jar.toString(), why, Outcome.UNREADABLE, e.toString()));
            return null;
        }
    }

    private static final class ScanState {
        final long start;
        int folders;
        int files;
        int candidates;
        boolean cutShort;

        ScanState(long start) {
            this.start = start;
        }

        boolean over() {
            if (folders + files > SCAN_MAX_ENTRIES || System.currentTimeMillis() - start > SCAN_MAX_MILLIS) {
                cutShort = true;
            }
            return cutShort;
        }
    }

    private static Found scan(Path root, int maxDepth, ScanState state, List<Attempt> attempts, Map<Path, Boolean> seen) {
        ArrayDeque<Path> queue = new ArrayDeque<>();
        ArrayDeque<Integer> depths = new ArrayDeque<>();
        queue.add(root);
        depths.add(0);
        while (!queue.isEmpty() && !state.over()) {
            Path dir = queue.poll();
            int depth = depths.poll();
            state.folders++;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path entry : stream) {
                    if (state.over()) {
                        break;
                    }
                    BasicFileAttributes attributes;
                    try {
                        attributes = Files.readAttributes(entry, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                    } catch (IOException e) {
                        continue;
                    }
                    if (attributes.isDirectory()) {
                        if (depth + 1 <= maxDepth && !skipped(entry)) {
                            queue.add(entry);
                            depths.add(depth + 1);
                        }
                    } else if (attributes.isRegularFile()) {
                        state.files++;
                        if (attributes.size() == OFFICIAL_SIZE
                                && entry.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                            state.candidates++;
                            Path key = entry.toAbsolutePath().normalize();
                            if (seen.putIfAbsent(key, Boolean.TRUE) != null) {
                                continue;
                            }
                            try {
                                if (OFFICIAL_SHA1.equals(sha1(key))) {
                                    attempts.add(new Attempt(key.toString(), "a search for the published jar's fingerprint",
                                            Outcome.FOUND, "the published Beta 1.7.3 client"));
                                    return new Found(key, OFFICIAL_SHA1, "a search for the published jar's fingerprint");
                                }
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
            } catch (IOException | RuntimeException ignored) {
            }
        }
        return null;
    }

    private static boolean looksLikeLauncher(Path root) {
        for (String child : new String[]{"instances", "versions", "libraries", "meta", "Install", "install", "bin"}) {
            if (Files.isDirectory(root.resolve(child))) {
                return true;
            }
        }
        return false;
    }

    private static boolean skipped(Path dir) {
        String name = dir.getFileName() == null ? "" : dir.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.equals("node_modules") || name.equals(".git") || name.equals("objects") || name.equals("saves")
                || name.equals("screenshots") || name.equals("logs") || name.equals("crash-reports")
                || name.equals("$recycle.bin") || name.equals("cache") || name.equals("caches") || name.equals("temp")
                || name.equals("tmp") || name.equals("b173") || name.equals("voxy") || name.equals("region")
                || name.equals("shaderpacks") || name.equals("java") || name.equals("runtime") || name.equals("jre");
    }

    private static Path path(String value) {
        try {
            String text = value.strip();
            if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
                text = text.substring(1, text.length() - 1);
            }
            if (text.startsWith("~/") || text.startsWith("~\\")) {
                text = System.getProperty("user.home", "") + text.substring(1);
            }
            return Path.of(text).toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void remember(Environment env, Found found) {
        if (env.memoryFile() == null) {
            return;
        }
        try {
            Files.createDirectories(env.memoryFile().getParent());
            Files.writeString(env.memoryFile(), "# Where Betaport last found your Beta 1.7.3 jar.\njar="
                    + found.jar() + "\n", StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static boolean scannedEmptyRecently(Environment env) {
        String when = configured(env.memoryFile(), "emptyScan");
        if (when == null) {
            return false;
        }
        try {
            return System.currentTimeMillis() - Long.parseLong(when) < 24L * 3600 * 1000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void rememberEmptyScan(Environment env) {
        if (env.memoryFile() == null) {
            return;
        }
        try {
            Files.createDirectories(env.memoryFile().getParent());
            Files.writeString(env.memoryFile(), "# Betaport's last search for your Beta 1.7.3 jar found nothing.\nemptyScan="
                    + System.currentTimeMillis() + "\n", StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static String configured(Path file, String key) {
        if (file == null || !Files.isRegularFile(file)) {
            return null;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
        for (String line : lines) {
            String text = line.strip();
            if (text.startsWith("#") || !text.startsWith(key)) {
                continue;
            }
            int equals = text.indexOf('=');
            if (equals < 0 || !text.substring(0, equals).strip().equals(key)) {
                continue;
            }
            String value = text.substring(equals + 1).strip();
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    public static String sha1(Path file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[65536];
            for (int n; (n = in.read(buffer)) > 0; ) {
                digest.update(buffer, 0, n);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
