package dev.b173.client.gui;

import dev.b173.core.pack.BetaJarSearch;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.Difficulty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BetaDifficulty {
    private BetaDifficulty() {
    }

    public static Difficulty get() {
        String value = BetaClientConfig.get("difficulty");
        if (value != null) {
            Difficulty parsed = Difficulty.byName(value.toLowerCase(Locale.ROOT));
            if (parsed != null) {
                return parsed;
            }
        }
        return Difficulty.NORMAL;
    }

    public static void set(Minecraft minecraft, Difficulty difficulty) {
        write(difficulty);
        apply(minecraft);
    }

    public static void apply(Minecraft minecraft) {
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server != null) {
            Difficulty wanted = get();
            server.execute(() -> server.setDifficulty(wanted, true));
        }
    }

    private static void write(Difficulty difficulty) {
        BetaClientConfig.set("difficulty", difficulty.getSerializedName(),
                "Beta's difficulty, an option for every world as in Beta: peaceful, easy, normal or hard.");
    }
}
