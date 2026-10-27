package dev.b173.mixin;

import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerAttackStrengthMixin {
    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void b173$fullStrength(float partialTick, CallbackInfoReturnable<Float> callback) {
        if (BetaRules.betaWorld(((Player) (Object) this).level())) {
            callback.setReturnValue(1.0F);
        }
    }
}
