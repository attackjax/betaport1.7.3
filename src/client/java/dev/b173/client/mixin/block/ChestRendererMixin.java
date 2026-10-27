package dev.b173.client.mixin.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.b173.client.model.BetaModels;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestRenderer.class)
abstract class ChestRendererMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private void b173$betaChest(ChestRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                CameraRenderState camera, CallbackInfo ci) {
        if (state.blockEntityType == BlockEntityType.CHEST && BetaModels.packActive()) {
            ci.cancel();
        }
    }
}
