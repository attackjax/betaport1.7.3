package dev.b173.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public final class BetaControlsScreen extends Screen {
    private final Screen parent;
    private final Options options;
    private KeyMapping[] keys;
    private Button[] buttons;
    private int waiting = -1;

    public BetaControlsScreen(Screen parent, Options options) {
        super(Component.translatable("b173.controls.title"));
        this.parent = parent;
        this.options = options;
    }

    private int left() {
        return this.width / 2 - 155;
    }

    private int y(int i) {
        return this.height / 6 + 24 * (i >> 1);
    }

    @Override
    protected void init() {
        this.keys = BetaKeys.beta(this.options);
        this.buttons = new Button[this.keys.length];
        for (int i = 0; i < this.keys.length; i++) {
            int index = i;
            this.buttons[i] = addRenderableWidget(Button.builder(Component.literal(BetaKeys.name(this.keys[i])), b -> {
                for (int j = 0; j < this.keys.length; j++) {
                    this.buttons[j].setMessage(Component.literal(BetaKeys.name(this.keys[j])));
                }
                this.waiting = index;
                b.setMessage(Component.literal("> " + BetaKeys.name(this.keys[index]) + " <"));
            }).bounds(left() + i % 2 * 160, y(i), 70, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("b173.gui.done"), b -> onClose())
                .bounds(this.width / 2 - 100, this.height / 6 + 168, 200, 20).build());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.waiting >= 0) {
            KeyMapping key = this.keys[this.waiting];
            key.setKey(InputConstants.getKey(event));
            KeyMapping.resetMapping();
            this.buttons[this.waiting].setMessage(Component.literal(BetaKeys.name(key)));
            this.waiting = -1;
            return true;
        }
        return super.keyPressed(event);
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
        for (int i = 0; i < this.keys.length; i++) {
            graphics.text(this.font, Component.translatable(this.keys[i].getName()),
                    left() + i % 2 * 160 + 70 + 6, y(i) + 7, 0xFFFFFFFF);
        }
    }
}
