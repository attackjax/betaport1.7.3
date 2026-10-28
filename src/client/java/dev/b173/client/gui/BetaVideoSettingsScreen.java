package dev.b173.client.gui;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public final class BetaVideoSettingsScreen extends Screen {
    private static final String[] DISTANCE = {"far", "normal", "short", "tiny"};
    private static final String[] PERFORMANCE = {"max", "balanced", "powersaver"};
    private static final String[] SCALE = {"auto", "small", "normal", "large"};

    private final Screen parent;
    private final Options options;

    public BetaVideoSettingsScreen(Screen parent, Options options) {
        super(Component.translatable("b173.options.videoTitle"));
        this.parent = parent;
        this.options = options;
    }

    public static int renderDistance(int chunks) {
        int setting = 0;
        while (setting < 3 && chunks < (16 >> setting)) {
            setting++;
        }
        return setting;
    }

    public static void stepRenderDistance(Options options, int step) {
        int next = (renderDistance(options.renderDistance().get()) + step) & 3;
        options.renderDistance().set(16 >> next);
    }

    public static int performance() {
        String value = BetaClientConfig.get("performance");
        if (value == null) {
            return 1;
        }
        try {
            return Math.floorMod(Integer.parseInt(value.trim()), 3);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void setPerformance(int value) {
        BetaClientConfig.set("performance", Integer.toString(value),
                "Beta's Performance option: 0 Max FPS, 1 Balanced, 2 Power saver (40 frames a second).");
        this.options.framerateLimit().set(value == 2 ? 40 : Options.UNLIMITED_FRAMERATE_CUTOFF);
    }

    private static boolean fancy(Options options) {
        return options.cutoutLeaves().get();
    }

    private Component label(String key, Component value) {
        return Component.translatable(key).append(": ").append(value);
    }

    private Component onOff(boolean on) {
        return Component.translatable(on ? "b173.options.on" : "b173.options.off");
    }

    private int x(int i) {
        return this.width / 2 - 155 + i % 2 * 160;
    }

    private int y(int i) {
        return this.height / 6 + 24 * (i >> 1);
    }

    private void option(int i, Supplier<Component> text, Runnable step) {
        addRenderableWidget(Button.builder(text.get(), b -> {
            step.run();
            b.setMessage(text.get());
        }).bounds(x(i), y(i), 150, 20).build());
    }

    @Override
    protected void init() {
        option(0, () -> label("b173.options.graphics",
                Component.translatable(fancy(this.options) ? "b173.options.graphics.fancy" : "b173.options.graphics.fast")), () -> {
            boolean fancy = !fancy(this.options);
            this.options.cutoutLeaves().set(fancy);
            this.options.cloudStatus().set(fancy ? CloudStatus.FANCY : CloudStatus.FAST);
            this.options.weatherRadius().set(fancy ? 10 : 5);
        });
        option(1, () -> label("b173.options.renderDistance", Component.translatable(
                "b173.options.renderDistance." + DISTANCE[renderDistance(this.options.renderDistance().get())])),
                () -> stepRenderDistance(this.options, 1));
        option(2, () -> label("b173.options.ao", onOff(this.options.ambientOcclusion().get())),
                () -> this.options.ambientOcclusion().set(!this.options.ambientOcclusion().get()));
        option(3, () -> label("b173.options.framerateLimit", Component.translatable(
                "b173.performance." + PERFORMANCE[performance()])), () -> setPerformance((performance() + 1) % 3));
        Button anaglyph = Button.builder(label("b173.options.anaglyph", onOff(false)), b -> {
        }).bounds(x(4), y(4), 150, 20).build();
        anaglyph.active = false;
        addRenderableWidget(anaglyph);
        option(5, () -> label("b173.options.viewBobbing", onOff(this.options.bobView().get())),
                () -> this.options.bobView().set(!this.options.bobView().get()));
        option(6, () -> label("b173.options.guiScale", Component.translatable(
                "b173.options.guiScale." + SCALE[Math.min(3, this.options.guiScale().get())])), () -> {
            this.options.guiScale().set((this.options.guiScale().get() + 1) & 3);
            Minecraft.getInstance().resizeGui();
        });
        Button opengl = Button.builder(label("b173.options.advancedOpengl", onOff(false)), b -> {
        }).bounds(x(7), y(7), 150, 20).build();
        opengl.active = false;
        addRenderableWidget(opengl);
        boolean shaders = ModVideoSettings.hasShaders();
        addRenderableWidget(Button.builder(Component.translatable("b173.options.more"),
                b -> this.minecraft.setScreen(ModVideoSettings.more(this, this.minecraft, this.options)))
                .bounds(shaders ? x(8) : this.width / 2 - 75, y(8), 150, 20).build());
        if (shaders) {
            addRenderableWidget(Button.builder(Component.translatable("b173.options.shaders"), b -> {
                Screen screen = ModVideoSettings.shaders(this);
                if (screen != null) {
                    this.minecraft.setScreen(screen);
                }
            }).bounds(x(9), y(9), 150, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("b173.gui.done"), b -> onClose())
                .bounds(this.width / 2 - 100, this.height / 6 + 168, 200, 20).build());
    }

    @Override
    public void onClose() {
        this.options.save();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
    }
}
