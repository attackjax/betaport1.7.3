package dev.b173.client.mixin.model;

import dev.b173.client.model.BetaAnimations;
import dev.b173.client.model.BetaModels;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombifiedPiglinModel.class)
abstract class ZombifiedPiglinModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ZombifiedPiglinRenderState;)V", at = @At("TAIL"))
    private void b173$betaArms(ZombifiedPiglinRenderState state, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        if (BetaModels.isBeta(model.root())) {
            BetaAnimations.zombieArms(model, state.ageInTicks);
        }
    }
}
