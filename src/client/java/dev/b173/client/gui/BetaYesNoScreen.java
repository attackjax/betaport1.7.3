package dev.b173.client.gui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class BetaYesNoScreen extends Screen {
    private final BooleanConsumer answer;
    private final String line1;
    private final String line2;
    private final String yes;
    private final String no;

    public BetaYesNoScreen(BooleanConsumer answer, String line1, String line2, String yes, String no) {
        super(Component.literal(line1));
        this.answer = answer;
        this.line1 = line1;
        this.line2 = line2;
        this.yes = yes;
        this.no = no;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 155;
        int y = this.height / 6 + 96;
        addRenderableWidget(Button.builder(Component.literal(this.yes), b -> this.answer.accept(true))
                .bounds(left, y, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal(this.no), b -> this.answer.accept(false))
                .bounds(left + 160, y, 150, 20).build());
    }

    @Override
    public void onClose() {
        this.answer.accept(false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.line1, this.width / 2, 70, 0xFFFFFFFF);
        graphics.centeredText(this.font, this.line2, this.width / 2, 90, 0xFFFFFFFF);
    }
}
