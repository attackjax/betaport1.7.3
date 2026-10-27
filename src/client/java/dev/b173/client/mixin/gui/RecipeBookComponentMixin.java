package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookComponent.class)
abstract class RecipeBookComponentMixin {
    @Inject(method = "isVisibleAccordingToBookData", at = @At("HEAD"), cancellable = true)
    private void b173$shut(CallbackInfoReturnable<Boolean> cir) {
        if (BetaScreens.active()) {
            cir.setReturnValue(false);
        }
    }
}
