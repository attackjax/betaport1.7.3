package dev.b173.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public final class BetaKeys {
    public static final KeyMapping FOG = new KeyMapping("key.fog", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("b173", "beta")));

    private static final Map<Integer, String> NAMES = new HashMap<>();

    static {
        for (int c = GLFW.GLFW_KEY_A; c <= GLFW.GLFW_KEY_Z; c++) {
            NAMES.put(c, String.valueOf((char) c));
        }
        for (int c = GLFW.GLFW_KEY_0; c <= GLFW.GLFW_KEY_9; c++) {
            NAMES.put(c, String.valueOf((char) c));
        }
        for (int i = 1; i <= 15; i++) {
            NAMES.put(GLFW.GLFW_KEY_F1 + i - 1, "F" + i);
        }
        for (int i = 0; i <= 9; i++) {
            NAMES.put(GLFW.GLFW_KEY_KP_0 + i, "NUMPAD" + i);
        }
        Object[][] named = {
                {GLFW.GLFW_KEY_SPACE, "SPACE"}, {GLFW.GLFW_KEY_LEFT_SHIFT, "LSHIFT"}, {GLFW.GLFW_KEY_RIGHT_SHIFT, "RSHIFT"},
                {GLFW.GLFW_KEY_LEFT_CONTROL, "LCONTROL"}, {GLFW.GLFW_KEY_RIGHT_CONTROL, "RCONTROL"},
                {GLFW.GLFW_KEY_LEFT_ALT, "LMENU"}, {GLFW.GLFW_KEY_RIGHT_ALT, "RMENU"},
                {GLFW.GLFW_KEY_LEFT_SUPER, "LMETA"}, {GLFW.GLFW_KEY_RIGHT_SUPER, "RMETA"},
                {GLFW.GLFW_KEY_TAB, "TAB"}, {GLFW.GLFW_KEY_ENTER, "RETURN"}, {GLFW.GLFW_KEY_BACKSPACE, "BACK"},
                {GLFW.GLFW_KEY_ESCAPE, "ESCAPE"}, {GLFW.GLFW_KEY_CAPS_LOCK, "CAPITAL"},
                {GLFW.GLFW_KEY_UP, "UP"}, {GLFW.GLFW_KEY_DOWN, "DOWN"}, {GLFW.GLFW_KEY_LEFT, "LEFT"},
                {GLFW.GLFW_KEY_RIGHT, "RIGHT"}, {GLFW.GLFW_KEY_INSERT, "INSERT"}, {GLFW.GLFW_KEY_DELETE, "DELETE"},
                {GLFW.GLFW_KEY_HOME, "HOME"}, {GLFW.GLFW_KEY_END, "END"}, {GLFW.GLFW_KEY_PAGE_UP, "PRIOR"},
                {GLFW.GLFW_KEY_PAGE_DOWN, "NEXT"}, {GLFW.GLFW_KEY_GRAVE_ACCENT, "GRAVE"}, {GLFW.GLFW_KEY_MINUS, "MINUS"},
                {GLFW.GLFW_KEY_EQUAL, "EQUALS"}, {GLFW.GLFW_KEY_LEFT_BRACKET, "LBRACKET"},
                {GLFW.GLFW_KEY_RIGHT_BRACKET, "RBRACKET"}, {GLFW.GLFW_KEY_SEMICOLON, "SEMICOLON"},
                {GLFW.GLFW_KEY_APOSTROPHE, "APOSTROPHE"}, {GLFW.GLFW_KEY_BACKSLASH, "BACKSLASH"},
                {GLFW.GLFW_KEY_COMMA, "COMMA"}, {GLFW.GLFW_KEY_PERIOD, "PERIOD"}, {GLFW.GLFW_KEY_SLASH, "SLASH"},
                {GLFW.GLFW_KEY_KP_MULTIPLY, "MULTIPLY"}, {GLFW.GLFW_KEY_KP_ADD, "ADD"},
                {GLFW.GLFW_KEY_KP_SUBTRACT, "SUBTRACT"}, {GLFW.GLFW_KEY_KP_DECIMAL, "DECIMAL"},
                {GLFW.GLFW_KEY_KP_DIVIDE, "DIVIDE"}, {GLFW.GLFW_KEY_KP_ENTER, "NUMPADENTER"},
                {GLFW.GLFW_KEY_KP_EQUAL, "NUMPADEQUALS"}, {GLFW.GLFW_KEY_NUM_LOCK, "NUMLOCK"},
                {GLFW.GLFW_KEY_SCROLL_LOCK, "SCROLL"}, {GLFW.GLFW_KEY_PAUSE, "PAUSE"},
                {GLFW.GLFW_KEY_PRINT_SCREEN, "SYSRQ"}, {GLFW.GLFW_KEY_MENU, "APPS"},
        };
        for (Object[] pair : named) {
            NAMES.put((Integer) pair[0], (String) pair[1]);
        }
    }

    private BetaKeys() {
    }

    public static void register() {
        KeyMappingHelper.registerKeyMapping(FOG);
    }

    public static KeyMapping[] beta(Options options) {
        return new KeyMapping[] {options.keyUp, options.keyLeft, options.keyDown, options.keyRight, options.keyJump,
                options.keyInventory, options.keyDrop, options.keyChat, FOG, options.keyShift};
    }

    public static String name(KeyMapping mapping) {
        InputConstants.Key key = KeyMappingHelper.getBoundKeyOf(mapping);
        if (key.getType() == InputConstants.Type.KEYSYM) {
            String name = NAMES.get(key.getValue());
            if (name != null) {
                return name;
            }
            if (key.getValue() == GLFW.GLFW_KEY_UNKNOWN) {
                return "NONE";
            }
        }
        return key.getDisplayName().getString().toUpperCase(java.util.Locale.ROOT);
    }

    public static void tick(Minecraft minecraft) {
        if (!BetaScreens.active()) {
            return;
        }
        while (FOG.consumeClick()) {
            long window = minecraft.getWindow().handle();
            boolean back = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                    || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            BetaVideoSettingsScreen.stepRenderDistance(minecraft.options, back ? -1 : 1);
        }
    }

    public static void dropOffhandSwap(Options options) {
        if (BetaScreens.active()) {
            while (options.keySwapOffhand.consumeClick()) {
            }
        }
    }
}
