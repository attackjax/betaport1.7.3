package dev.b173.client.mixin.model;

import dev.b173.client.model.BetaAnimations;
import dev.b173.client.model.BetaModels;
import net.minecraft.client.model.animal.chicken.AdultChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdultChickenModel.class)
abstract class AdultChickenModelMixin {
    @Shadow
    @Final
    private ModelPart head;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ChickenRenderState;)V", at = @At("TAIL"))
    private void b173$invertedPitch(ChickenRenderState state, CallbackInfo ci) {
        if (BetaModels.isBeta(((AdultChickenModel) (Object) this).root())) {
            BetaAnimations.chickenHead(this.head);
        }
    }
}
