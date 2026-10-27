package dev.b173.client.visual;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.b173.core.pack.BetaJarSearch;
import dev.b173.core.pack.BetaSounds;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class BetaSoundsLocator {
    private BetaSoundsLocator() {
    }

    public record Found(BetaSounds.Resources resources, String fingerprint, String where) {
    }

    public static Found find(BetaJarSearch.Environment env, Path betaJar, List<String> tried) {
        Set<Path> places = new LinkedHashSet<>();
        String configured = BetaJarLocator.configured(env.configFile(), "betaResources");
        if (configured != null) {
            places.add(Path.of(configured));
        }
        Set<Path> roots = new LinkedHashSet<>();
        Path root = launcherRoot(betaJar);
        if (root != null) {
            roots.add(root);
        }
        roots.addAll(BetaJarSearch.launcherRoots(env));
        for (Path launcher : roots) {
            if (!Files.isDirectory(launcher)) {
                continue;
            }
            places.add(launcher.resolve("assets"));
            places.add(launcher.resolve("resources"));
            places.add(launcher.resolve("meta").resolve("assets"));
            places.add(launcher.resolve("Install").resolve("assets"));
            places.add(launcher.resolve("data").resolve("assets"));
            Path instances = launcher.resolve("instances");
            if (Files.isDirectory(instances)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(instances)) {
                    for (Path instance : stream) {
                        places.add(instance.resolve(".minecraft").resolve("resources"));
                        places.add(instance.resolve("minecraft").resolve("resources"));
                    }
                } catch (IOException ignored) {
                }
            }
        }

        for (Path place : places) {
            if (!Files.isDirectory(place)) {
                continue;
            }
            try {
                Found found = open(place);
                if (found != null) {
                    tried.add("found  " + found.where());
                    return found;
                }
                tried.add("none   " + place + " (no Beta sounds in it)");
            } catch (IOException | RuntimeException e) {
                tried.add("error  " + place + ": " + e);
            }
        }
        return null;
    }

    private static Path launcherRoot(Path jar) {
        if (jar == null) {
            return null;
        }
        Path path = jar.toAbsolutePath().normalize();
        int count = path.getNameCount();
        if (count >= 6 && path.getName(count - 6).toString().equalsIgnoreCase("libraries")) {
            return path.getParent().getParent().getParent().getParent().getParent().getParent();
        }
        if (count >= 3 && path.getName(count - 3).toString().equalsIgnoreCase("versions")) {
            return path.getParent().getParent().getParent();
        }
        if (count >= 2 && path.getName(count - 2).toString().equalsIgnoreCase("versions")) {
            return path.getParent().getParent();
        }
        return null;
    }

    private static Found open(Path place) throws IOException {
        if (!Files.isDirectory(place)) {
            return null;
        }
        Path index = place.resolve("indexes").resolve("pre-1.6.json");
        if (Files.isRegularFile(index) && Files.isDirectory(place.resolve("objects"))) {
            return index(index, place.resolve("objects"));
        }
        if (Files.isDirectory(place.resolve("newsound"))) {
            return folder(place);
        }
        return null;
    }

    private static Found index(Path index, Path objects) throws IOException {
        Map<String, String> hashes = new LinkedHashMap<>();
        try (Reader reader = Files.newBufferedReader(index, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("objects").entrySet()) {
                if (BetaSounds.wanted(entry.getKey())) {
                    hashes.put(entry.getKey(), entry.getValue().getAsJsonObject().get("hash").getAsString().toLowerCase());
                }
            }
        }
        long present = hashes.values().stream().filter(hash -> Files.isRegularFile(object(objects, hash))).count();
        if (present < hashes.size() / 2 || hashes.isEmpty()) {
            return null;
        }
        List<String> names = List.copyOf(hashes.keySet());
        BetaSounds.Resources resources = new BetaSounds.Resources() {
            @Override
            public List<String> list() {
                return names;
            }

            @Override
            public byte[] read(String path) throws IOException {
                String hash = hashes.get(path);
                if (hash == null) {
                    return null;
                }
                Path file = object(objects, hash);
                if (!Files.isRegularFile(file)) {
                    return null;
                }
                byte[] data = Files.readAllBytes(file);
                return hash.equals(sha1(data)) ? data : null;
            }
        };
        StringBuilder print = new StringBuilder();
        hashes.forEach((name, hash) -> print.append(name).append('=').append(hash).append('\n'));
        return new Found(resources, sha1(print.toString().getBytes(StandardCharsets.UTF_8)),
                index.toString() + " (" + present + " of " + hashes.size() + " sounds present)");
    }

    private static Found folder(Path folder) throws IOException {
        List<String> names = new ArrayList<>();
        StringBuilder print = new StringBuilder();
        try (Stream<Path> walk = Files.walk(folder)) {
            for (Path file : (Iterable<Path>) walk.filter(Files::isRegularFile).sorted()::iterator) {
                String name = folder.relativize(file).toString().replace('\\', '/');
                if (BetaSounds.wanted(name)) {
                    names.add(name);
                    print.append(name).append('=').append(Files.size(file)).append('\n');
                }
            }
        }
        if (names.isEmpty()) {
            return null;
        }
        List<String> listing = List.copyOf(names);
        BetaSounds.Resources resources = new BetaSounds.Resources() {
            @Override
            public List<String> list() {
                return listing;
            }

            @Override
            public byte[] read(String path) throws IOException {
                if (!listing.contains(path)) {
                    return null;
                }
                return Files.readAllBytes(folder.resolve(path));
            }
        };
        return new Found(resources, sha1(print.toString().getBytes(StandardCharsets.UTF_8)),
                folder + " (" + names.size() + " sounds)");
    }

    private static Path object(Path objects, String hash) {
        return objects.resolve(hash.substring(0, 2)).resolve(hash);
    }

    private static String sha1(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
