package dev.b173.client.mixin;

import dev.b173.client.visual.BetaLight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "extract", at = @At("TAIL"))
    private void b173$betaLight(LightmapRenderState state, float partialTick, CallbackInfo callback) {
        BetaLight.apply(state, minecraft);
    }
}
