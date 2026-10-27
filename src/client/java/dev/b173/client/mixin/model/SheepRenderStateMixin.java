package dev.b173.client.mixin.model;

import dev.b173.client.model.BetaAnimations;
import dev.b173.client.model.BetaModels;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SheepRenderState.class)
abstract class SheepRenderStateMixin {
    @Shadow
    public DyeColor woolColor;

    @Inject(method = "getWoolColor", at = @At("HEAD"), cancellable = true)
    private void b173$fleece(CallbackInfoReturnable<Integer> cir) {
        if (BetaModels.isBeta(ModelLayers.SHEEP_WOOL)) {
            cir.setReturnValue(BetaAnimations.fleeceColor(this.woolColor));
        }
    }
}
