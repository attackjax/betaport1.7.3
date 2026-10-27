package dev.b173.mixin.content;

import net.minecraft.server.commands.DefaultGameModeCommands;
import net.minecraft.server.commands.GameModeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameModeCommand.class, DefaultGameModeCommands.class})
abstract class GameModeCommandsMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void b173$none(CallbackInfo ci) {
        ci.cancel();
    }
}
