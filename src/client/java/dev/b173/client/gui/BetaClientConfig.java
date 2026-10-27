package dev.b173.client.gui;

import dev.b173.core.pack.BetaJarSearch;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BetaClientConfig {
    private BetaClientConfig() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("b173.properties");
    }

    public static String get(String key) {
        return BetaJarSearch.configured(file(), key);
    }

    public static void set(String key, String value, String comment) {
        Path file = file();
        List<String> lines = new ArrayList<>();
        try {
            String newline = System.lineSeparator();
            if (Files.isRegularFile(file)) {
                String text = Files.readString(file, StandardCharsets.UTF_8);
                newline = text.contains("\r\n") ? "\r\n" : text.contains("\n") ? "\n" : newline;
                lines.addAll(text.lines().toList());
            }
            boolean done = false;
            for (int i = 0; i < lines.size(); i++) {
                String text = lines.get(i).strip();
                if (!text.startsWith("#") && text.contains("=")
                        && text.substring(0, text.indexOf('=')).strip().equals(key)) {
                    lines.set(i, key + "=" + value);
                    done = true;
                }
            }
            if (!done) {
                lines.add("# " + comment);
                lines.add(key + "=" + value);
            }
            Files.createDirectories(file.getParent());
            Files.writeString(file, String.join(newline, lines) + newline, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
