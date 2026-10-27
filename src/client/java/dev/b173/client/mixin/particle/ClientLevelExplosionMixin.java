package dev.b173.client.mixin.particle;

import dev.b173.client.visual.BetaParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientLevel.class)
abstract class ClientLevelExplosionMixin {
    @ModifyVariable(method = "trackExplosionEffects", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int b173$betaPuffs(int blockCount) {
        return BetaParticles.active() ? blockCount * BetaParticles.EXPLOSION_PARTICLES_PER_BLOCK : blockCount;
    }
}
