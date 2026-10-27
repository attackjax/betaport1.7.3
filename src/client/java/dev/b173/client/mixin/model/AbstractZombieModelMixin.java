package dev.b173.client.mixin.model;

import dev.b173.client.model.BetaAnimations;
import dev.b173.client.model.BetaModels;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.zombie.AbstractZombieModel;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractZombieModel.class)
abstract class AbstractZombieModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;)V", at = @At("TAIL"))
    private void b173$betaArms(ZombieRenderState state, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        if (BetaModels.isBeta(model.root())) {
            BetaAnimations.zombieArms(model, state.ageInTicks);
        }
    }
}
