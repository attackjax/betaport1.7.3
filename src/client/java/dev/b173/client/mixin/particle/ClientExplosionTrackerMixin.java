package dev.b173.client.mixin.particle;

import dev.b173.client.visual.BetaParticles;
import net.minecraft.client.multiplayer.ClientExplosionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientExplosionTracker.class)
abstract class ClientExplosionTrackerMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 512))
    private int b173$betaBurst(int perTick) {
        return BetaParticles.active() ? BetaParticles.EXPLOSION_PARTICLES_PER_TICK : perTick;
    }
}
