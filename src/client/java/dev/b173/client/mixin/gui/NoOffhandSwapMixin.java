package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaKeys;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class NoOffhandSwapMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void b173$noSwap(CallbackInfo ci) {
        BetaKeys.dropOffhandSwap(((Minecraft) (Object) this).options);
    }
}
