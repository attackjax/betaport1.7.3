package dev.b173.client.mixin.model;

import dev.b173.client.model.BetaAnimations;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfModel.class)
abstract class WolfModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/WolfRenderState;)V", at = @At("TAIL"))
    private void b173$betaWolf(WolfRenderState state, CallbackInfo ci) {
        BetaAnimations.wolf(((WolfModel) (Object) this).root(), state.isSitting);
    }
}
