package dev.b173.client.gui;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;

public final class BetaOptionsScreen extends Screen {
    private final Screen parent;
    private final Options options;

    public BetaOptionsScreen(Screen parent, Options options) {
        super(Component.translatable("b173.options.title"));
        this.parent = parent;
        this.options = options;
    }

    private int x(int i) {
        return this.width / 2 - 155 + i % 2 * 160;
    }

    private int y(int i) {
        return this.height / 6 + 24 * (i >> 1);
    }

    @Override
    protected void init() {
        addRenderableWidget(this.options.getSoundSourceOptionInstance(SoundSource.MUSIC).createButton(this.options, x(0), y(0), 150));
        addRenderableWidget(new SoundSlider(x(1), y(1)));
        addRenderableWidget(Button.builder(invertLabel(), b -> {
            this.options.invertMouseY().set(!this.options.invertMouseY().get());
            b.setMessage(invertLabel());
        }).bounds(x(2), y(2), 150, 20).build());
        addRenderableWidget(this.options.sensitivity().createButton(this.options, x(3), y(3), 150));
        addRenderableWidget(Button.builder(difficultyLabel(), b -> {
            Difficulty next = Difficulty.byId((BetaDifficulty.get().getId() + 1) % 4);
            BetaDifficulty.set(this.minecraft, next);
            b.setMessage(difficultyLabel());
        }).bounds(x(4), y(4), 150, 20).build());
        int left = this.width / 2 - 100;
        addRenderableWidget(Button.builder(Component.translatable("b173.options.video"),
                b -> this.minecraft.setScreen(new BetaVideoSettingsScreen(this, this.options)))
                .bounds(left, this.height / 6 + 96 + 12, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("b173.options.controls"),
                b -> this.minecraft.setScreen(new BetaControlsScreen(this, this.options)))
                .bounds(left, this.height / 6 + 120 + 12, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("b173.gui.done"), b -> onClose())
                .bounds(left, this.height / 6 + 168, 200, 20).build());
    }

    private Component invertLabel() {
        return Component.translatable("b173.options.invertMouse").append(": ")
                .append(Component.translatable(this.options.invertMouseY().get() ? "b173.options.on" : "b173.options.off"));
    }

    private static Component difficultyLabel() {
        return Component.translatable("b173.options.difficulty").append(": ")
                .append(Component.translatable("b173.options.difficulty." + BetaDifficulty.get().getSerializedName()));
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

    private final class SoundSlider extends AbstractSliderButton {
        SoundSlider(int x, int y) {
            super(x, y, 150, 20, Component.empty(), options.getSoundSourceVolume(SoundSource.BLOCKS));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            Component name = Component.translatable("b173.options.sound");
            setMessage(this.value == 0.0D ? name.copy().append(": ").append(Component.translatable("b173.options.off"))
                    : name.copy().append(": " + (int) (this.value * 100.0D) + "%"));
        }

        @Override
        protected void applyValue() {
            for (SoundSource source : SoundSource.values()) {
                if (source != SoundSource.MUSIC && source != SoundSource.MASTER) {
                    options.getSoundSourceOptionInstance(source).set(this.value);
                }
            }
        }
    }
}
