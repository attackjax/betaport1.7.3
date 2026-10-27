package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.rules.BetaRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TntBlock.class)
abstract class TntBreakMixin {
    @ModifyExpressionValue(method = "playerWillDestroy", at = @At(value = "INVOKE",
            target = "Ljava/lang/Boolean;booleanValue()Z"))
    private boolean b173$lightWhenBroken(boolean unstable, @Local(argsOnly = true) Level level) {
        return unstable || BetaRules.betaWorld(level);
    }
}
