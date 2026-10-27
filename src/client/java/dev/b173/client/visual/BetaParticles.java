package dev.b173.client.visual;

import dev.b173.client.BetaClientState;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.EggItem;

import java.util.Set;

public final class BetaParticles {
    private static final Set<ParticleType<?>> BETA = Set.of(
            ParticleTypes.BUBBLE, ParticleTypes.SMOKE, ParticleTypes.NOTE, ParticleTypes.PORTAL,
            ParticleTypes.POOF, ParticleTypes.FLAME, ParticleTypes.LAVA, ParticleTypes.SPLASH,
            ParticleTypes.LARGE_SMOKE, ParticleTypes.DUST, ParticleTypes.ITEM_SNOWBALL, ParticleTypes.ITEM_SLIME,
            ParticleTypes.HEART, ParticleTypes.RAIN);

    public static final int EXPLOSION_PARTICLES_PER_BLOCK = 2;
    public static final int EXPLOSION_PARTICLES_PER_TICK = 8192;

    private BetaParticles() {
    }

    public static boolean active() {
        return BetaClientState.onBetaServer();
    }

    public static ParticleOptions beta(ParticleOptions options) {
        ParticleType<?> type = options.getType();
        if (BETA.contains(type)) {
            return options;
        }
        if (type == ParticleTypes.ITEM && options instanceof ItemParticleOption item
                && item.getItem().item().value() instanceof EggItem) {
            return ParticleTypes.ITEM_SNOWBALL;
        }
        return null;
    }
}
