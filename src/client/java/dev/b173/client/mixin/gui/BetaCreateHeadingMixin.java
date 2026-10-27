package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaLoading;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
abstract class BetaCreateHeadingMixin {
    @Inject(method = "onCreate", at = @At("HEAD"))
    private void b173$generating(CallbackInfo ci) {
        BetaLoading.opening("Generating level");
    }
}
