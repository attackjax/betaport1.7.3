package dev.b173.mixin.content;

import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
abstract class LevelDataSurvivalMixin {
    @Inject(method = "getGameType", at = @At("HEAD"), cancellable = true)
    private void b173$survival(CallbackInfoReturnable<GameType> cir) {
        cir.setReturnValue(GameType.SURVIVAL);
    }

    @Inject(method = "isHardcore", at = @At("HEAD"), cancellable = true)
    private void b173$notHardcore(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
