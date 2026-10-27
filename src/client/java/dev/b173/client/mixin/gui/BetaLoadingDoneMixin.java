package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaLoading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class BetaLoadingDoneMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void b173$opened(Screen screen, CallbackInfo ci) {
        if (screen == null && ((Minecraft) (Object) this).level != null) {
            BetaLoading.opened();
        }
    }
}
