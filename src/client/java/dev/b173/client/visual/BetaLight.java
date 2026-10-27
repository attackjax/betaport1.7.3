package dev.b173.client.visual;

import dev.b173.client.BetaClientState;
import dev.b173.core.BetaSky;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.world.level.Level;

public final class BetaLight {
    private BetaLight() {
    }

    public static void apply(LightmapRenderState state, Minecraft minecraft) {
        if (!BetaPack.betaLight() || !BetaClientState.onBetaServer()) {
            return;
        }
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        if (level.dimension() == Level.NETHER) {
            state.skyFactor = -1.0F;
            state.blockFactor = 0.1F;
        } else if (BetaClientState.isBetaDimension(level)) {
            state.skyFactor = -1.0F - (float) BetaSky.skyDarkening(level.getOverworldClockTime(), 1.0F,
                    level.getRainLevel(1.0F), level.getThunderLevel(1.0F));
            state.blockFactor = 0.05F;
        }
    }
}
