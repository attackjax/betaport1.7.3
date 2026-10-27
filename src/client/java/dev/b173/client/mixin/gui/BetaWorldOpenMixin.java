package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaLoading;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
abstract class BetaWorldOpenMixin {
    @Inject(method = {"createFreshLevel", "createLevelFromExistingSettings"}, at = @At("HEAD"))
    private void b173$generating(CallbackInfo ci) {
        BetaLoading.opening("Generating level");
    }

    @Inject(method = "openWorld", at = @At("HEAD"))
    private void b173$loading(CallbackInfo ci) {
        BetaLoading.opening("Loading level");
    }
}
