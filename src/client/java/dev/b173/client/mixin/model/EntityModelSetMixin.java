package dev.b173.client.mixin.model;

import dev.b173.client.model.BetaModels;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityModelSet.class)
abstract class EntityModelSetMixin {
    @Inject(method = "bakeLayer", at = @At("RETURN"))
    private void b173$finishBetaModel(ModelLayerLocation layer, CallbackInfoReturnable<ModelPart> cir) {
        BetaModels.finishBake(layer, cir.getReturnValue());
    }
}
