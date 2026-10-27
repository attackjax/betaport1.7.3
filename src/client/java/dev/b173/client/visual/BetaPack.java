package dev.b173.client.visual;

import dev.b173.core.pack.BetaJarSearch;
import dev.b173.core.pack.BetaPackBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class BetaPack {
    private static final Logger LOGGER = LoggerFactory.getLogger("Betaport");
    private static final String STAMP_FILE = "b173-pack.properties";
    static final String LIGHTMAP = "assets/minecraft/shaders/core/lightmap.fsh";
    static final String CLOUDS = "assets/minecraft/shaders/core/rendertype_clouds.fsh";

    private static volatile Path folder;
    private static volatile boolean betaLight;
    private static volatile boolean betaClouds;
    private static String lastProblem = "";

    private BetaPack() {
    }

    public static boolean active() {
        return folder != null;
    }

    public static boolean betaLight() {
        return folder != null && betaLight;
    }

    public static boolean betaClouds() {
        return folder != null && betaClouds;
    }

    private static volatile BetaSetupReport report;
    private static volatile boolean scanNext;

    public static BetaSetupReport report() {
        return report;
    }

    public static void searchAgainNextTime() {
        scanNext = true;
    }

    static synchronized Path prepare() {
        long started = System.currentTimeMillis();
        FabricLoader loader = FabricLoader.getInstance();
        Path gameDir = loader.getGameDir();
        Path config = loader.getConfigDir().resolve("b173.properties");
        BetaJarSearch.Environment env = BetaJarSearch.Environment.of(gameDir, config,
                gameDir.resolve("b173").resolve("located.properties"));
        boolean scan = scanNext || !BetaJarSearch.scannedEmptyRecently(env);
        scanNext = false;
        BetaJarSearch.Result search = BetaJarSearch.search(env, scan);
        BetaJarSearch.Found jar = search.found();
        if (jar == null) {
            writeConfigTemplate(config);
            String message = "No Beta 1.7.3 jar found, so Beta's textures are off. Set betaJar in " + config
                    + " to your own b1.7.3 client jar. " + summary(search);
            problem(message);
            report = new BetaSetupReport(search, null, List.of(), null, message, config,
                    System.currentTimeMillis() - started);
            folder = null;
            return null;
        }
        LOGGER.info("b173: Beta 1.7.3 jar {} ({}, {}). {}", jar.jar(), jar.how(),
                jar.official() ? "the published client" : "SHA-1 " + jar.sha1(), summary(search));
        if (!jar.official()) {
            LOGGER.warn("b173: {} has SHA-1 {}, not the published Beta 1.7.3 client ({}); using its textures anyway",
                    jar.jar(), jar.sha1(), BetaJarSearch.OFFICIAL_SHA1);
        }

        List<String> soundPlaces = new ArrayList<>();
        BetaSoundsLocator.Found sounds = BetaSoundsLocator.find(env, jar.jar(), soundPlaces);
        if (sounds == null) {
            problem("Beta 1.7.3's sounds were not found, so the game keeps modern sounds. Launch Beta 1.7.3 once in "
                    + "your launcher so it downloads them, or set betaResources in " + config + ". Looked in:\n  "
                    + String.join("\n  ", soundPlaces));
        }

        Path root = gameDir.resolve("b173");
        Path pack = root.resolve("pack");
        String stamp = "source=" + jar.sha1() + "\nsounds=" + (sounds == null ? "none" : sounds.fingerprint())
                + "\nrevision=" + BetaPackBuilder.REVISION + "\n";
        try {
            if (!stamp.equals(readStamp(pack))) {
                build(jar, sounds, root, pack, stamp);
            }
            betaLight = writeShader(pack, LIGHTMAP, BetaPack::patchLightShader, "light");
            betaClouds = writeShader(pack, CLOUDS, BetaPack::patchCloudShader, "cloud");
            folder = pack;
            lastProblem = "";
            report = new BetaSetupReport(search, sounds, List.copyOf(soundPlaces), pack, null, config,
                    System.currentTimeMillis() - started);
            return pack;
        } catch (IOException | RuntimeException e) {
            String message = "Could not build the Beta texture pack from " + jar.jar() + ": " + e;
            problem(message);
            report = new BetaSetupReport(search, sounds, List.copyOf(soundPlaces), null, message, config,
                    System.currentTimeMillis() - started);
            folder = null;
            return null;
        }
    }

    private static String summary(BetaJarSearch.Result search) {
        long folders = search.roots().stream().filter(BetaJarSearch.Root::exists).count();
        BetaJarSearch.Scan scan = search.scan();
        return "Tried " + search.layouts() + " layouts in " + search.roots().size() + " folders (" + folders
                + " on this computer)" + (scan.ran() ? "; fingerprint search: " + scan.folders() + " folders, "
                + scan.files() + " files, " + scan.millis() + " ms" + (scan.cutShort() ? ", cut short" : "") : "") + ".";
    }

    private static void build(BetaJarSearch.Found jar, BetaSoundsLocator.Found sounds, Path root, Path pack,
                              String stamp) throws IOException {
        long start = System.nanoTime();
        Path fresh = root.resolve("pack-new");
        deleteTree(fresh);
        Files.createDirectories(fresh);
        BetaPackBuilder.Result result;
        try (ZipFile zip = new ZipFile(jar.jar().toFile())) {
            result = BetaPackBuilder.build(path -> {
                ZipEntry entry = zip.getEntry(path);
                if (entry == null) {
                    return null;
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    return in.readAllBytes();
                }
            }, sounds == null ? null : sounds.resources(), (path, data) -> {
                Path file = fresh.resolve(path).normalize();
                if (!file.startsWith(fresh)) {
                    throw new IOException("refusing to write outside the pack: " + path);
                }
                Files.createDirectories(file.getParent());
                Files.write(file, data);
            });
        }
        Files.writeString(fresh.resolve(STAMP_FILE), stamp, StandardCharsets.UTF_8);
        deleteTree(pack);
        Files.move(fresh, pack);
        LOGGER.info("b173: built the Beta pack from {} ({}) in {} ms: {} files{}",
                jar.jar(), jar.how(), (System.nanoTime() - start) / 1_000_000, result.files(),
                result.missing().isEmpty() ? "" : ", the jar lacked " + result.missing());
        if (result.sounds() != null) {
            LOGGER.info("b173: Beta's sounds from {}: {} sound events, {} files{}", sounds.where(),
                    result.sounds().events(), result.sounds().files(),
                    result.sounds().missing().isEmpty() ? "" : ", missing " + result.sounds().missing());
        }
    }

    private static boolean writeShader(Path pack, String path, java.util.function.UnaryOperator<String> patch,
                                       String what) throws IOException {
        Path target = pack.resolve(path);
        String vanilla = null;
        try (InputStream in = BetaPack.class.getClassLoader().getResourceAsStream(path)) {
            if (in != null) {
                vanilla = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        String patched = vanilla == null ? null : patch.apply(vanilla);
        if (patched == null) {
            Files.deleteIfExists(target);
            LOGGER.warn("b173: this game's {} shader is not the one the Beta patch expects; keeping the modern one", what);
            return false;
        }
        Files.createDirectories(target.getParent());
        if (!Files.exists(target) || !patched.equals(Files.readString(target, StandardCharsets.UTF_8))) {
            Files.writeString(target, patched, StandardCharsets.UTF_8);
        }
        return true;
    }

    static String patchLightShader(String vanilla) {
        String main = "void main() {";
        if (!vanilla.contains(main) || !vanilla.contains("LightmapInfo") || !vanilla.contains("SkyFactor")
                || !vanilla.contains("BlockFactor") || !vanilla.contains("BrightnessFactor")
                || !vanilla.contains("vec3 notGamma(") || !vanilla.contains("texCoord") || !vanilla.contains("fragColor")) {
            return null;
        }
        String beta = """
                void main() {
                    if (lightmapInfo.SkyFactor < 0.0) {
                        float b173Darkening = -lightmapInfo.SkyFactor - 1.0;
                        float b173Block = floor(texCoord.x * 16.0);
                        float b173Sky = max(floor(texCoord.y * 16.0) - b173Darkening, 0.0);
                        float b173Level = max(b173Sky, b173Block) / 15.0;
                        float b173Dimmest = lightmapInfo.BlockFactor;
                        vec3 b173Color = vec3(b173Level / (4.0 - 3.0 * b173Level) * (1.0 - b173Dimmest) + b173Dimmest);
                        b173Color = mix(b173Color, notGamma(b173Color), lightmapInfo.BrightnessFactor);
                        fragColor = vec4(b173Color, 1.0);
                        return;
                    }
                """;
        return vanilla.replaceFirst(java.util.regex.Pattern.quote(main), Matcher.quoteReplacement(beta.stripTrailing()));
    }

    static String patchCloudShader(String vanilla) {
        String fade = "color.a *= 1.0f - linear_fog_value(vertexDistance, 0, FogCloudsEnd);";
        if (!vanilla.contains(fade) || !vanilla.contains("fog.glsl") || !vanilla.contains("vertexDistance")) {
            return null;
        }
        String beta = String.join("\n",
                "if (FogCloudsEnd < 0.0) {",
                "        // Beta 1.7.3 (added by Betaport): the clouds take on the fog",
                "        // colour with distance, as the world did, and keep their opacity.",
                "        float b173End = -FogCloudsEnd;",
                "        color.rgb = mix(color.rgb, FogColor.rgb,",
                "                linear_fog_value(vertexDistance, b173End * 0.25, b173End));",
                "    } else {",
                "        " + fade,
                "    }");
        return vanilla.replace(fade, beta);
    }

    private static String readStamp(Path pack) {
        try {
            return Files.readString(pack.resolve(STAMP_FILE), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private static void writeConfigTemplate(Path config) {
        if (Files.exists(config)) {
            return;
        }
        try {
            Files.createDirectories(config.getParent());
            Files.writeString(config, """
                    # Betaport settings.
                    #
                    # The mod draws Beta's textures from your own Beta 1.7.3 client jar; it ships
                    # none of Mojang's art. It looks in the game folder and every folder above it,
                    # in the usual folders of the Minecraft Launcher, MultiMC, Prism Launcher
                    # (Flatpak too), PolyMC, the Modrinth App, the CurseForge app, ATLauncher,
                    # GDLauncher, Technic and Betacraft, and then searches those folders and your
                    # Desktop, Downloads, Documents and Games folders for the published jar's
                    # fingerprint. If yours is still not found, put the full path of the jar, or
                    # of the launcher folder holding it, here, for example
                    #   betaJar=C:\\Users\\you\\MultiMC\\libraries\\com\\mojang\\minecraft\\b1.7.3\\minecraft-b1.7.3-client.jar
                    # The B173_BETA_JAR environment variable and -Db173.betaJar=... work too.
                    betaJar=
                    #
                    # Beta's sounds and music are not in the jar; the launcher downloads them
                    # when Beta 1.7.3 is first played. The mod finds them next to the jar. If
                    # they are elsewhere, give the launcher's assets folder or a resources folder:
                    #   betaResources=C:\\Users\\you\\MultiMC\\assets
                    betaResources=
                    """, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("b173: could not write {}: {}", config, e.toString());
        }
    }

    private static void problem(String message) {
        if (!message.equals(lastProblem)) {
            LOGGER.warn("b173: {}", message);
            lastProblem = message;
        }
    }

    private static void deleteTree(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                if (e != null) {
                    throw e;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
