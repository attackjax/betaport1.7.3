package dev.b173.client.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;

import java.lang.reflect.Constructor;

public final class ModVideoSettings {
    private static final String IRIS_SCREEN = "net.irisshaders.iris.gui.screen.ShaderPackScreen";

    private ModVideoSettings() {
    }

    public static Screen more(Screen parent, Minecraft minecraft, Options options) {
        if (FabricLoader.getInstance().isModLoaded("sodium")) {
            try {
                return Sodium.screen(parent);
            } catch (LinkageError | RuntimeException ignored) {
            }
        }
        return new VideoSettingsScreen(parent, minecraft, options);
    }

    public static boolean hasShaders() {
        return shaderScreen() != null;
    }

    public static Screen shaders(Screen parent) {
        Constructor<?> constructor = shaderScreen();
        if (constructor == null) {
            return null;
        }
        try {
            return (Screen) constructor.newInstance(parent);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static Constructor<?> shaderScreen() {
        if (!FabricLoader.getInstance().isModLoaded("iris")) {
            return null;
        }
        try {
            return Class.forName(IRIS_SCREEN).getConstructor(Screen.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    private static final class Sodium {
        static Screen screen(Screen parent) {
            return net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen.createScreen(parent);
        }
    }
}
