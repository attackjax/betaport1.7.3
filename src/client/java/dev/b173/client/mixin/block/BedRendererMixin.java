package dev.b173.client.mixin.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.b173.client.model.BetaModels;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.state.BedRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedRenderer.class)
abstract class BedRendererMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BedRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private void b173$betaBed(BedRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                              CameraRenderState camera, CallbackInfo ci) {
        if (BetaModels.packActive()) {
            ci.cancel();
        }
    }
}
