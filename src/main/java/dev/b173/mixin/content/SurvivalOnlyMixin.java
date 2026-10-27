package dev.b173.mixin.content;

import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
abstract class SurvivalOnlyMixin {
    @ModifyVariable(method = "setGameModeForPlayer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private GameType b173$survival(GameType gameType) {
        return GameType.SURVIVAL;
    }

    @Inject(method = "changeGameModeForPlayer", at = @At("HEAD"), cancellable = true)
    private void b173$onlySurvival(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        if (gameType != GameType.SURVIVAL) {
            cir.setReturnValue(false);
        }
    }
}
