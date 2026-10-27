package dev.b173.client.mixin.compat;

import dev.b173.client.visual.BetaAtmosphere;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "me.cortex.voxy.client.core.NormalRenderPipeline")
abstract class VoxyFogMixin {
    @ModifyArg(method = "finish", require = 0, index = 3, at = @At(value = "INVOKE", ordinal = 0,
            target = "Lorg/lwjgl/opengl/GL30C;glUniform4f(IFFFF)V"))
    private float b173$fogToTheEdge(float limit) {
        return BetaAtmosphere.farTerrainFog() ? 1.0F : limit;
    }
}
