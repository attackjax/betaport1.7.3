package dev.b173.client;

import dev.b173.core.BetaClimate;
import dev.b173.core.ClimateCache;
import dev.b173.net.BetaWorldPayload;
import dev.b173.rules.BetaMobs;
import dev.b173.rules.BetaRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public final class BetaClientState {
    private static volatile @Nullable Session session;

    private BetaClientState() {
    }

    static void accept(BetaWorldPayload payload) {
        session = new Session(payload.seed(), Set.copyOf(payload.dimensions()), Set.copyOf(payload.nethers()),
                new ClimateCache(new BetaClimate(payload.seed())));
        BetaRules.clientInBetaWorld = true;
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            for (Entity entity : level.entitiesForRendering()) {
                refreshSize(entity);
            }
        }
    }

    static void refreshSize(Entity entity) {
        if (BetaRules.betaSizes() && BetaMobs.hasBetaSize(entity.getType())) {
            entity.refreshDimensions();
        }
    }

    static void clear() {
        session = null;
        BetaRules.clientInBetaWorld = false;
    }

    public static @Nullable ClimateCache climate() {
        Session current = session;
        if (current == null) {
            return null;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        return current.isBeta(level.dimension()) ? current.cache() : null;
    }

    public static boolean onBetaServer() {
        return session != null;
    }

    public static boolean isBetaDimension(ClientLevel level) {
        Session current = session;
        return current != null && current.isBeta(level.dimension());
    }

    public static boolean isBetaNether(ClientLevel level) {
        Session current = session;
        return current != null && current.nethers().contains(level.dimension().identifier().toString());
    }

    private record Session(long seed, Set<String> dimensions, Set<String> nethers, ClimateCache cache) {
        boolean isBeta(ResourceKey<Level> dimension) {
            return dimensions.contains(dimension.identifier().toString());
        }
    }
}
