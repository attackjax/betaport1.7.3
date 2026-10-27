package dev.b173.client.mixin.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
abstract class CommandSuggestionsMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private boolean commandsOnly;

    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
    private void b173$noCommandsInSinglePlayer(CallbackInfo ci) {
        if (!this.commandsOnly && this.minecraft.hasSingleplayerServer()) {
            ci.cancel();
        }
    }
}
