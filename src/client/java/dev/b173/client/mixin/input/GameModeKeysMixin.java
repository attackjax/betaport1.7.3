package dev.b173.client.mixin.input;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
abstract class GameModeKeysMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void b173$noGameModes(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.options.keyDebugSpectate.matches(event)
                || this.minecraft.options.keyDebugSwitchGameMode.matches(event)) {
            cir.setReturnValue(true);
        }
    }
}
