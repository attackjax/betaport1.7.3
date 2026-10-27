package dev.b173.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.IntPredicate;

public final class BetaTextField extends AbstractWidget {
    private static final int EDGE = 0xFFA0A0A0;
    private static final int INSIDE = 0xFF000000;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int TEXT_OFF = 0xFF707070;

    private final Font font;
    private final int maxLength;
    private final IntPredicate allowed;
    private String text = "";
    private int cursorCounter;
    private Consumer<String> responder = text -> { };

    public BetaTextField(Font font, int x, int y, int width, int height, int maxLength, IntPredicate allowed,
                         Component name) {
        super(x, y, width, height, name);
        this.font = font;
        this.maxLength = maxLength;
        this.allowed = allowed;
    }

    public String getValue() {
        return this.text;
    }

    public void setValue(String text) {
        this.text = text;
    }

    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
    }

    public void tick() {
        this.cursorCounter++;
    }

    @Override
    public void setFocused(boolean focused) {
        if (focused && !isFocused()) {
            this.cursorCounter = 0;
        }
        super.setFocused(focused);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.active || !isFocused()) {
            return false;
        }
        if (event.isPaste()) {
            String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
            StringBuilder kept = new StringBuilder();
            clip.codePoints().filter(this.allowed).forEach(kept::appendCodePoint);
            int room = Math.min(32 - this.text.length(), kept.length());
            if (room > 0) {
                change(this.text + kept.substring(0, room));
            }
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
            if (!this.text.isEmpty()) {
                change(this.text.substring(0, this.text.length() - 1));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!this.active || !isFocused()) {
            return false;
        }
        int character = event.codepoint();
        if (Character.isBmpCodePoint(character) && this.allowed.test(character)
                && (this.text.length() < this.maxLength || this.maxLength == 0)) {
            change(this.text + (char) character);
        }
        return true;
    }

    private void change(String text) {
        this.text = text;
        this.responder.accept(text);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        graphics.fill(x - 1, y - 1, x + this.width + 1, y + this.height + 1, EDGE);
        graphics.fill(x, y, x + this.width, y + this.height, INSIDE);
        int textY = y + (this.height - 8) / 2;
        if (this.active) {
            boolean blink = isFocused() && this.cursorCounter / 6 % 2 == 0;
            graphics.text(this.font, this.text + (blink ? "_" : ""), x + 4, textY, TEXT);
        } else {
            graphics.text(this.font, this.text, x + 4, textY, TEXT_OFF);
        }
    }

    @Override
    public void playDownSound(SoundManager sounds) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.translatable("narration.edit_box", this.text));
    }
}
