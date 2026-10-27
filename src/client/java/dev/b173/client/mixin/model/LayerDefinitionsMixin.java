package dev.b173.client.mixin.model;

import dev.b173.client.model.BetaModels;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LayerDefinitions.class)
abstract class LayerDefinitionsMixin {
    @Inject(method = "createRoots", at = @At("RETURN"), cancellable = true)
    private static void b173$betaModels(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> cir) {
        if (BetaModels.packLoaded()) {
            cir.setReturnValue(BetaModels.replace(cir.getReturnValue()));
        } else {
            BetaModels.clear();
        }
    }
}
