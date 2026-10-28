package dev.b173.client.mixin.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.b173.client.visual.BetaAtmosphere;
import dev.b173.client.visual.BetaSkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
abstract class SkyRendererMixin {
    @Unique
    private BetaSkyRenderer.Buffers b173$beta;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void b173$build(TextureManager textures, AtlasManager atlases, CallbackInfo ci) {
        this.b173$beta = BetaSkyRenderer.Buffers.build();
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void b173$free(CallbackInfo ci) {
        if (this.b173$beta != null) {
            this.b173$beta.close();
            this.b173$beta = null;
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void b173$begin(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
        BetaAtmosphere.beginSky();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void b173$betaSky(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
        BetaAtmosphere.sky(level, partialTick, camera, state);
    }

    @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
    private void b173$betaSkyPlane(int color, CallbackInfo ci) {
        if (BetaAtmosphere.skyActive() && this.b173$beta != null) {
            this.b173$beta.drawSky(BetaAtmosphere.skyColor());
            ci.cancel();
        }
    }

    @Inject(method = "renderDarkDisc", at = @At("HEAD"), cancellable = true)
    private void b173$betaDarkPlane(CallbackInfo ci) {
        if (BetaAtmosphere.skyActive() && this.b173$beta != null) {
            this.b173$beta.drawDarkPlane(BetaAtmosphere.skyColor());
            ci.cancel();
        }
    }

    @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
    private void b173$betaStars(float brightness, PoseStack poseStack, CallbackInfo ci) {
        if (BetaAtmosphere.skyActive() && this.b173$beta != null) {
            this.b173$beta.drawStars(brightness, poseStack);
            ci.cancel();
        }
    }

    @ModifyArg(method = "renderSunMoonAndStars", at = @At(value = "INVOKE",
            target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;"))
    private float b173$betaSunPath(float degrees) {
        return BetaAtmosphere.skyActive() && !dev.b173.client.visual.BetaIris.shadersInUse() ? 0.0F : degrees;
    }

    @ModifyArg(method = "renderSunriseAndSunset", at = @At(value = "INVOKE",
            target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;", ordinal = 1))
    private float b173$betaGlow(float degrees) {
        return BetaAtmosphere.skyActive() && !dev.b173.client.visual.BetaIris.shadersInUse() ? degrees - 90.0F : degrees;
    }
}
