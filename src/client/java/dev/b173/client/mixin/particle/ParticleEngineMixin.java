package dev.b173.client.mixin.particle;

import dev.b173.client.visual.BetaParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
abstract class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void b173$onlyBeta(ParticleOptions options, double x, double y, double z, double xd, double yd, double zd,
                               CallbackInfoReturnable<Particle> cir) {
        if (!BetaParticles.active()) {
            return;
        }
        ParticleOptions beta = BetaParticles.beta(options);
        if (beta == null) {
            cir.setReturnValue(null);
        } else if (beta != options) {
            cir.setReturnValue(((ParticleEngine) (Object) this).createParticle(beta, x, y, z, xd, yd, zd));
        }
    }
}
