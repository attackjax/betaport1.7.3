package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaRules;
import dev.b173.rules.ai.BetaGhastBrain;
import net.minecraft.world.entity.monster.Ghast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Ghast.class)
abstract class GhastMixin {
    @ModifyReturnValue(method = "getSoundVolume", at = @At("RETURN"))
    private float b173$betaVolume(float volume) {
        return BetaRules.betaWorld(((Ghast) (Object) this).level()) ? BetaGhastBrain.VOLUME : volume;
    }
}
