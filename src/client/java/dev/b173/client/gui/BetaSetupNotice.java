package dev.b173.client.gui;

import dev.b173.client.visual.BetaPack;
import dev.b173.client.visual.BetaSetupReport;
import dev.b173.core.pack.BetaJarSearch;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BetaSetupNotice {
    private static boolean shownThisRun;

    private BetaSetupNotice() {
    }

    public enum Mode {
        CHANGES("Show at start: On change"),
        ALWAYS("Show at start: Always"),
        NEVER("Show at start: Never");

        private final String label;

        Mode(String label) {
            this.label = label;
        }

        public Component label() {
            return Component.literal(label);
        }
    }

    private static Path file() {
        return FabricLoader.getInstance().getGameDir().resolve("b173").resolve("notice.properties");
    }

    public static Mode mode() {
        String value = BetaJarSearch.configured(file(), "show");
        if (value != null) {
            for (Mode mode : Mode.values()) {
                if (mode.name().equalsIgnoreCase(value)) {
                    return mode;
                }
            }
        }
        return Mode.CHANGES;
    }

    public static void setMode(Mode mode) {
        write(mode, BetaJarSearch.configured(file(), "seen"));
    }

    public static void seen(BetaSetupReport report) {
        write(mode(), report.signature());
    }

    private static void write(Mode mode, String seen) {
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), "# changes, always or never\nshow=" + mode.name().toLowerCase() + "\n"
                    + (seen == null ? "" : "seen=" + seen + "\n"), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static void maybeShow(Minecraft minecraft, Screen title) {
        if (shownThisRun) {
            return;
        }
        BetaSetupReport report = BetaPack.report();
        if (report == null) {
            return;
        }
        shownThisRun = true;
        boolean due = switch (mode()) {
            case ALWAYS -> true;
            case NEVER -> false;
            case CHANGES -> !report.complete() || !report.signature().equals(BetaJarSearch.configured(file(), "seen"));
        };
        if (due) {
            minecraft.setScreen(new BetaSetupScreen(title, report));
        }
    }
}
