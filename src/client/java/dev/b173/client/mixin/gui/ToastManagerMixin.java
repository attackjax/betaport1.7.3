package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
abstract class ToastManagerMixin {
    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void b173$noModernToasts(Toast toast, CallbackInfo ci) {
        if ((toast instanceof RecipeToast || toast instanceof AdvancementToast || toast instanceof TutorialToast)
                && BetaScreens.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "showNowPlayingToast", at = @At("HEAD"), cancellable = true)
    private void b173$noNowPlaying(CallbackInfo ci) {
        if (BetaScreens.active()) {
            ci.cancel();
        }
    }
}
