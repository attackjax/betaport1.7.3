package dev.b173.client.mixin.sky;

import dev.b173.client.visual.BetaAtmosphere;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Shadow
    private @Nullable ClientLevel level;

    @Inject(method = "extractLevel", at = @At("HEAD"))
    private void b173$begin(DeltaTracker deltaTracker, Camera camera, float partialTick, CallbackInfo ci) {
        BetaAtmosphere.beginClouds();
    }

    @Inject(method = "extractLevel", at = @At("TAIL"))
    private void b173$betaClouds(DeltaTracker deltaTracker, Camera camera, float partialTick, CallbackInfo ci) {
        BetaAtmosphere.clouds(this.levelRenderState, partialTick, this.level);
    }
}
