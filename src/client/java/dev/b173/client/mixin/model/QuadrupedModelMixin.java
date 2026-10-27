package dev.b173.client.mixin.model;

import dev.b173.client.model.BetaAnimations;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QuadrupedModel.class)
abstract class QuadrupedModelMixin {
    @Shadow
    @Final
    protected ModelPart head;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)V", at = @At("TAIL"))
    private void b173$horns(LivingEntityRenderState state, CallbackInfo ci) {
        BetaAnimations.cowHorns(((QuadrupedModel<?>) (Object) this).root(), this.head);
    }
}
