package dev.b173.client.mixin.model;

import dev.b173.client.model.BetaAnimations;
import dev.b173.client.model.BetaModels;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
abstract class HumanoidModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private void b173$betaPose(HumanoidRenderState state, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        if (BetaModels.isBeta(model.root())) {
            model.resetPose();
            BetaAnimations.biped(model, state, state instanceof AvatarRenderState ? state.attackTime : 0.0F);
            ci.cancel();
        }
    }
}
