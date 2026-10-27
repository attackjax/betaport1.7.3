package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractRecipeBookScreen.class)
abstract class AbstractRecipeBookScreenMixin {
    @Inject(method = "initButton", at = @At("HEAD"), cancellable = true)
    private void b173$noRecipeButton(CallbackInfo ci) {
        if (BetaScreens.active()) {
            ci.cancel();
        }
    }
}
