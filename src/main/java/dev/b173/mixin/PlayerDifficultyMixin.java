package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import dev.b173.rules.BetaCombat;
import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerDifficultyMixin {
    @WrapOperation(method = "hurtServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/DamageSource;scalesWithDifficulty()Z"))
    private boolean b173$betaDifficulty(DamageSource source, Operation<Boolean> original,
                                        @Local(argsOnly = true) ServerLevel level,
                                        @Local(argsOnly = true) LocalFloatRef amount) {
        if (!BetaRules.active(level.getServer())) {
            return original.call(source);
        }
        amount.set(BetaCombat.scaleForDifficulty(level.getDifficulty(), source, amount.get()));
        return false;
    }
}
