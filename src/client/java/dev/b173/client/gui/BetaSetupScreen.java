package dev.b173.client.gui;

import dev.b173.client.visual.BetaPack;
import dev.b173.client.visual.BetaSetupReport;
import dev.b173.core.pack.BetaJarSearch;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BetaSetupScreen extends Screen {
    private static final int TOP = 34;
    private static final int MARGIN = 20;

    private final Screen parent;
    private final BetaSetupReport report;
    private final List<FormattedCharSequence> lines = new ArrayList<>();
    private int scroll;
    private boolean searching;
    private boolean details;

    public BetaSetupScreen(Screen parent, BetaSetupReport report) {
        super(Component.literal("Betaport"));
        this.parent = parent;
        this.report = report;
    }

    private int bottom() {
        return this.height - 62;
    }

    @Override
    protected void init() {
        lines.clear();
        int width = this.width - 2 * MARGIN - 8;
        for (Component paragraph : paragraphs()) {
            if (paragraph.getString().isEmpty()) {
                lines.add(FormattedCharSequence.EMPTY);
            } else {
                List<FormattedCharSequence> split = this.font.split(paragraph, width);
                if (split.size() > 1 && blank(split.get(0))) {
                    split = split.subList(1, split.size());
                }
                lines.addAll(split);
            }
        }
        scroll = Math.min(scroll, maxScroll());
        int centre = this.width / 2;
        addRenderableWidget(Button.builder(Component.literal(report.complete() ? "Continue" : "Continue anyway"),
                b -> onClose()).bounds(centre - 154, this.height - 52, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal(searching ? "Searching..." : "Search again"),
                b -> searchAgain()).bounds(centre + 4, this.height - 52, 150, 20).build()).active = !searching;
        addRenderableWidget(Button.builder(Component.literal("Config folder"),
                b -> openConfigFolder()).bounds(centre - 154, this.height - 28, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal(details ? "Less" : "Details"),
                b -> {
                    details = !details;
                    rebuildWidgets();
                }).bounds(centre - 52, this.height - 28, 48, 20).build());
        addRenderableWidget(CycleButton.builder(BetaSetupNotice.Mode::label, BetaSetupNotice.mode())
                .withValues(BetaSetupNotice.Mode.values())
                .displayOnlyValue()
                .create(centre + 4, this.height - 28, 150, 20, Component.literal("Show at start"),
                        (button, mode) -> BetaSetupNotice.setMode(mode)));
    }

    private void searchAgain() {
        searching = true;
        rebuildWidgets();
        BetaPack.searchAgainNextTime();
        Screen back = parent;
        this.minecraft.reloadResourcePacks().thenRun(() -> this.minecraft.execute(() -> {
            BetaSetupReport fresh = BetaPack.report();
            this.minecraft.setScreen(fresh == null ? back : new BetaSetupScreen(back, fresh));
        }));
    }

    private void openConfigFolder() {
        Path folder = report.configFile().getParent();
        try {
            Files.createDirectories(folder);
        } catch (java.io.IOException ignored) {
        }
        Util.getPlatform().openPath(folder);
    }

    @Override
    public void onClose() {
        BetaSetupNotice.seen(report);
        this.minecraft.setScreen(parent);
    }

    private static boolean blank(FormattedCharSequence line) {
        StringBuilder text = new StringBuilder();
        line.accept((index, style, codePoint) -> {
            text.appendCodePoint(codePoint);
            return true;
        });
        return text.toString().isBlank();
    }

    private int maxScroll() {
        int visible = (bottom() - TOP) / this.font.lineHeight;
        return Math.max(0, lines.size() - visible);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        scroll = (int) Math.max(0, Math.min(maxScroll(), scroll - Math.signum(dy) * 3));
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int key = event.key();
        if (key == 264) {
            scroll = Math.min(maxScroll(), scroll + 1);
            return true;
        }
        if (key == 265) {
            scroll = Math.max(0, scroll - 1);
            return true;
        }
        if (key == 267) {
            scroll = Math.min(maxScroll(), scroll + (bottom() - TOP) / this.font.lineHeight - 1);
            return true;
        }
        if (key == 266) {
            scroll = Math.max(0, scroll - (bottom() - TOP) / this.font.lineHeight + 1);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
        int bottom = bottom();
        graphics.fill(MARGIN - 4, TOP - 4, this.width - MARGIN + 4, bottom + 4, 0x90000000);
        graphics.enableScissor(MARGIN - 4, TOP - 4, this.width - MARGIN + 4, bottom + 4);
        int y = TOP;
        for (int i = scroll; i < lines.size() && y + this.font.lineHeight <= bottom + 2; i++) {
            graphics.text(this.font, lines.get(i), MARGIN, y, 0xFFE0E0E0);
            y += this.font.lineHeight;
        }
        graphics.disableScissor();
        int max = maxScroll();
        if (max > 0) {
            int track = bottom - TOP;
            int visible = track / this.font.lineHeight;
            int thumb = Math.max(10, track * visible / lines.size());
            int top = TOP + (track - thumb) * scroll / max;
            graphics.fill(this.width - MARGIN - 2, top, this.width - MARGIN + 2, top + thumb, 0xFFA0A0A0);
        }
    }

    private static MutableComponent text(String s) {
        return Component.literal(s);
    }

    private static MutableComponent heading(String s) {
        return Component.literal(s).withStyle(ChatFormatting.YELLOW);
    }

    private static MutableComponent good(String s) {
        return Component.literal(s).withStyle(ChatFormatting.GREEN);
    }

    private static MutableComponent bad(String s) {
        return Component.literal(s).withStyle(ChatFormatting.RED);
    }

    private static MutableComponent quiet(String s) {
        return Component.literal(s).withStyle(ChatFormatting.GRAY);
    }

    private List<Component> paragraphs() {
        List<Component> out = new ArrayList<>();
        BetaJarSearch.Result search = report.jar();
        BetaJarSearch.Found jar = search.found();
        String config = report.configFile().getFileName().toString();

        out.add(text("Betaport doesn't come with any of Mojang's files. It gets Beta's textures and sounds "
                + "from your own copy of Beta 1.7.3, and nothing is downloaded or sent anywhere."));
        out.add(text(""));
        if (jar == null) {
            out.add(bad("Couldn't find Beta 1.7.3."));
        } else {
            out.add(good("Found Beta 1.7.3: ").append(quiet(hide(jar.jar()))));
            if (!jar.official()) {
                out.add(Component.literal("This jar doesn't match the original release, but it'll be used anyway.")
                        .withStyle(ChatFormatting.GOLD));
            }
            if (report.sounds() != null) {
                out.add(good("Found the sounds: ").append(quiet(hide(report.sounds().where()))));
            } else {
                out.add(bad("Couldn't find Beta's sounds."));
            }
        }
        if (jar != null && report.packFolder() == null && report.problem() != null) {
            out.add(bad("Couldn't build the pack: " + hide(report.problem())));
        }

        if (jar == null || report.sounds() == null) {
            out.add(text(""));
            out.add(text("To fix it, play Beta 1.7.3 once from your launcher (in the Minecraft Launcher, turn on "
                    + "Historical versions under Installations), then press Search again. If it's somewhere "
                    + "unusual, set betaJar and betaResources in " + config + " in the config folder."));
            out.add(text("Until then the game plays like Beta but looks and sounds like modern Minecraft."));
        }

        if (details) {
            out.add(text(""));
            out.add(heading("Where it looked"));
            for (BetaJarSearch.Attempt attempt : search.attempts()) {
                out.add(mark(attempt).append(quiet(" " + hide(attempt.place()))));
            }
            for (BetaJarSearch.Root root : search.roots()) {
                if (root.exists()) {
                    out.add(quiet("   " + hide(root.folder())));
                }
            }
            BetaJarSearch.Scan scan = search.scan();
            if (scan.ran()) {
                out.add(quiet("   searched " + scan.files() + " files in " + scan.folders() + " folders"
                        + (scan.cutShort() ? " (stopped early)" : "")));
            }
            if (jar != null) {
                out.add(heading("Sounds"));
                for (String place : report.soundPlaces()) {
                    out.add(quiet("   " + hide(place)));
                }
            }
        }
        return out;
    }

    private static String hide(Object path) {
        String text = String.valueOf(path);
        String home = System.getProperty("user.home");
        if (home == null || home.length() < 2) {
            return text;
        }
        return text.replace(home, "~").replace(home.replace('\\', '/'), "~");
    }

    private static MutableComponent mark(BetaJarSearch.Attempt attempt) {
        return switch (attempt.outcome()) {
            case FOUND -> good("   [found]");
            case OTHER_JAR -> Component.literal("   [another jar]").withStyle(ChatFormatting.GOLD);
            case ABSENT -> bad("   [not there]");
            case UNREADABLE -> bad("   [unreadable]");
        };
    }
}
