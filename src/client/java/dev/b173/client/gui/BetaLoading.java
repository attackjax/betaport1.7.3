package dev.b173.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

public final class BetaLoading {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_background.png");

    private static volatile String opening;

    private BetaLoading() {
    }

    public static void opening(String heading) {
        opening = heading;
    }

    public static String opening() {
        return opening;
    }

    public static void opened() {
        opening = null;
    }

    public static void draw(GuiGraphicsExtractor graphics, int width, int height, String heading, String line,
                            int progress) {
        Font font = Minecraft.getInstance().font;
        Screen.extractMenuBackgroundTexture(graphics, BACKGROUND, 0, 0, 0.0F, 0.0F, width, height);
        if (progress >= 0) {
            int left = width / 2 - 100 / 2;
            int top = height / 2 + 16;
            graphics.fill(left, top, left + 100, top + 2, 0xFF808080);
            graphics.fill(left, top, left + progress, top + 2, 0xFF80FF80);
        }
        graphics.text(font, heading, (width - font.width(heading)) / 2, height / 2 - 4 - 16, 0xFFFFFFFF);
        graphics.text(font, line, (width - font.width(line)) / 2, height / 2 - 4 + 8, 0xFFFFFFFF);
    }
}
