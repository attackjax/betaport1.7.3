package dev.b173.mixin.content;

import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerLevel.class)
abstract class RandomTickRateMixin {
    @ModifyVariable(method = "tickChunk", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int b173$betaRandomTicks(int perSection) {
        return perSection == 3 && BetaRules.betaWorld((ServerLevel) (Object) this) ? 10 : perSection;
    }
}
