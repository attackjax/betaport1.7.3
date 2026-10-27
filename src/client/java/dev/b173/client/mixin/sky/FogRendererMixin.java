package dev.b173.client.mixin.sky;

import dev.b173.client.visual.BetaAtmosphere;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FogRenderer.class, priority = 900)
abstract class FogRendererMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void b173$betaFog(Camera camera, int renderDistance, DeltaTracker deltaTracker, float darken,
                              ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        BetaAtmosphere.fog(cir.getReturnValue(), camera, renderDistance,
                deltaTracker.getGameTimeDeltaPartialTick(false), level);
    }
}
