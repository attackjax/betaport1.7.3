package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class NoAutosaveIndicatorMixin {
    @Inject(method = "extractSavingIndicator", at = @At("HEAD"), cancellable = true)
    private void b173$quiet(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (BetaScreens.active()) {
            ci.cancel();
        }
    }
}
