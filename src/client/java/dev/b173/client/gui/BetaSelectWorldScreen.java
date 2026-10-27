package dev.b173.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

public final class BetaSelectWorldScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("Betaport");
    private static final Identifier LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_background.png");
    private static final int TOP = 32;
    private static final int SLOT = 36;
    private static final ExecutorService SIZES = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Betaport world sizes");
        thread.setDaemon(true);
        return thread;
    });
    private static final Map<String, Long> SIZE_CACHE = new ConcurrentHashMap<>();

    private final Screen parent;
    private final DateFormat dates = betaDates();
    private List<LevelSummary> worlds = List.of();
    private int selected = -1;
    private double scroll;
    private boolean dragging;
    private Button play;
    private Button rename;
    private Button delete;

    public BetaSelectWorldScreen(Screen parent) {
        super(Component.translatable("b173.selectWorld.title"));
        this.parent = parent;
    }

    private int bottom() {
        return this.height - 64;
    }

    @Override
    protected void init() {
        load();
        int centre = this.width / 2;
        this.play = addRenderableWidget(Button.builder(Component.translatable("b173.selectWorld.select"), b -> play())
                .bounds(centre - 154, this.height - 52, 150, 20).build());
        this.rename = addRenderableWidget(Button.builder(Component.translatable("b173.selectWorld.rename"), b -> rename())
                .bounds(centre - 154, this.height - 28, 70, 20).build());
        this.delete = addRenderableWidget(Button.builder(Component.translatable("b173.selectWorld.delete"), b -> delete())
                .bounds(centre - 74, this.height - 28, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("b173.selectWorld.create"),
                b -> CreateWorldScreen.openFresh(this.minecraft, () -> this.minecraft.setScreen(this)))
                .bounds(centre + 4, this.height - 52, 150, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("b173.gui.cancel"), b -> onClose())
                .bounds(centre + 4, this.height - 28, 150, 20).build());
        updateButtons();
    }

    private void load() {
        LevelStorageSource source = this.minecraft.getLevelSource();
        try {
            this.worlds = new ArrayList<>(source.loadLevelSummaries(source.findLevelCandidates()).join());
        } catch (Exception e) {
            LOGGER.warn("b173: could not list worlds", e);
            this.worlds = new ArrayList<>();
        }
        this.worlds.sort((a, b) -> Long.compare(b.getLastPlayed(), a.getLastPlayed()));
        if (this.selected >= this.worlds.size()) {
            this.selected = -1;
        }
        for (LevelSummary world : this.worlds) {
            String id = world.getLevelId();
            if (!SIZE_CACHE.containsKey(id)) {
                SIZE_CACHE.put(id, -1L);
                Path folder = source.getLevelPath(id);
                SIZES.execute(() -> SIZE_CACHE.put(id, size(folder)));
            }
        }
    }

    private static long size(Path folder) {
        try (Stream<Path> walk = Files.walk(folder)) {
            return walk.filter(Files::isRegularFile).mapToLong(file -> {
                try {
                    return Files.size(file);
                } catch (IOException e) {
                    return 0L;
                }
            }).sum();
        } catch (IOException | RuntimeException e) {
            return 0L;
        }
    }

    private void updateButtons() {
        boolean any = this.selected >= 0 && this.selected < this.worlds.size();
        this.play.active = any;
        this.rename.active = any;
        this.delete.active = any;
    }

    private LevelSummary current() {
        return this.selected >= 0 && this.selected < this.worlds.size() ? this.worlds.get(this.selected) : null;
    }

    private void play() {
        LevelSummary world = current();
        if (world != null) {
            this.minecraft.createWorldOpenFlows().openWorld(world.getLevelId(), () -> this.minecraft.setScreen(this));
        }
    }

    private void rename() {
        LevelSummary world = current();
        if (world != null) {
            this.minecraft.setScreen(new BetaRenameWorldScreen(this, world.getLevelId(), world.getLevelName()));
        }
    }

    private void delete() {
        LevelSummary world = current();
        if (world == null) {
            return;
        }
        String id = world.getLevelId();
        String name = displayName(world, this.selected);
        this.minecraft.setScreen(new BetaYesNoScreen(confirmed -> {
            if (confirmed) {
                try (LevelStorageSource.LevelStorageAccess access = this.minecraft.getLevelSource().createAccess(id)) {
                    access.deleteLevel();
                    SIZE_CACHE.remove(id);
                } catch (IOException e) {
                    LOGGER.warn("b173: could not delete world {}", id, e);
                }
                this.selected = -1;
            }
            this.minecraft.setScreen(this);
        }, Component.translatable("b173.selectWorld.deleteQuestion").getString(),
                "'" + name + "' " + Component.translatable("b173.selectWorld.deleteWarning").getString(),
                Component.translatable("b173.selectWorld.deleteButton").getString(),
                Component.translatable("b173.gui.cancel").getString()));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private int contentHeight() {
        return this.worlds.size() * SLOT;
    }

    private void clampScroll() {
        int max = Math.max(0, contentHeight() - (bottom() - TOP - 4));
        this.scroll = Math.max(0, Math.min(max, this.scroll));
    }

    private int slotAt(double x, double y) {
        int centre = this.width / 2;
        if (x < centre - 110 || x > centre + 110 || y < TOP || y > bottom()) {
            return -1;
        }
        int inside = (int) (y - TOP - 4 + this.scroll);
        int index = inside / SLOT;
        return inside >= 0 && index < this.worlds.size() ? index : -1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        int bar = this.width / 2 + 124;
        if (event.x() >= bar && event.x() < bar + 6 && event.y() >= TOP && event.y() <= bottom()) {
            this.dragging = true;
            return true;
        }
        int index = slotAt(event.x(), event.y());
        if (index >= 0) {
            boolean again = doubleClick && index == this.selected;
            this.selected = index;
            updateButtons();
            if (again) {
                play();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.dragging) {
            int track = bottom() - TOP;
            int content = contentHeight();
            int thumb = Math.max(32, Math.min(track - 8, track * track / Math.max(1, content)));
            int room = Math.max(1, track - thumb);
            this.scroll += dy * Math.max(1, content - track + 4) / room;
            clampScroll();
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.dragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        this.scroll -= dy * SLOT / 2.0;
        clampScroll();
        return true;
    }

    private String displayName(LevelSummary world, int index) {
        String name = world.getLevelName();
        return name == null || name.isBlank() ? Component.translatable("b173.selectWorld.world").getString() + " " + (index + 1)
                : name;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        clampScroll();
        int top = TOP;
        int bottom = bottom();
        int centre = this.width / 2;
        Screen.extractMenuBackgroundTexture(graphics, LIST_BACKGROUND, 0, top, 0.0F, (float) (top + this.scroll),
                this.width, bottom - top);
        int x = centre - 92 - 16;
        int y0 = top + 4 - (int) this.scroll;
        for (int i = 0; i < this.worlds.size(); i++) {
            int y = y0 + i * SLOT;
            if (y + SLOT - 4 < top || y > bottom) {
                continue;
            }
            int h = SLOT - 4;
            if (i == this.selected) {
                graphics.fill(centre - 110, y - 2, centre + 110, y + h + 2, 0xFF808080);
                graphics.fill(centre - 109, y - 1, centre + 109, y + h + 1, 0xFF000000);
            }
            LevelSummary world = this.worlds.get(i);
            String line2 = world.getLevelId() + " (" + this.dates.format(new Date(world.getLastPlayed()));
            long size = SIZE_CACHE.getOrDefault(world.getLevelId(), -1L);
            if (size >= 0) {
                line2 += ", " + (float) (size / 1024L * 100L / 1024L) / 100.0F + " MB)";
            } else {
                line2 += ")";
            }
            String line3 = world.requiresManualConversion()
                    ? Component.translatable("b173.selectWorld.conversion").getString() + " " : "";
            graphics.text(this.font, displayName(world, i), x + 2, y + 1, 0xFFFFFFFF);
            graphics.text(this.font, line2, x + 2, y + 12, 0xFF808080);
            graphics.text(this.font, line3, x + 2, y + 12 + 10, 0xFF808080);
        }
        Screen.extractMenuBackgroundTexture(graphics, BACKGROUND, 0, 0, 0.0F, 0.0F, this.width, top);
        Screen.extractMenuBackgroundTexture(graphics, BACKGROUND, 0, bottom, 0.0F, (float) bottom, this.width,
                this.height - bottom);
        graphics.fillGradient(0, top, this.width, top + 4, 0xFF000000, 0x00000000);
        graphics.fillGradient(0, bottom - 4, this.width, bottom, 0x00000000, 0xFF000000);
        int content = contentHeight();
        int track = bottom - top;
        if (content > track - 4) {
            int bar = centre + 124;
            int thumb = Math.max(32, Math.min(track - 8, track * track / content));
            int max = Math.max(1, content - track + 4);
            int thumbTop = top + (int) (this.scroll * (track - thumb) / max);
            graphics.fill(bar, top, bar + 6, bottom, 0xFF000000);
            graphics.fill(bar, thumbTop, bar + 6, thumbTop + thumb, 0xFF808080);
            graphics.fill(bar, thumbTop, bar + 5, thumbTop + thumb - 1, 0xFFC0C0C0);
        }
        graphics.centeredText(this.font, this.title, centre, 20, 0xFFFFFFFF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private static DateFormat betaDates() {
        String pattern = new SimpleDateFormat().toPattern()
                .replace('\u202F', ' ')
                .replace('\u00A0', ' ')
                .replace(", ", " ")
                .replaceAll("(?<!y)y(?!y)", "yy");
        try {
            return new SimpleDateFormat(pattern);
        } catch (IllegalArgumentException e) {
            return new SimpleDateFormat();
        }
    }
}
