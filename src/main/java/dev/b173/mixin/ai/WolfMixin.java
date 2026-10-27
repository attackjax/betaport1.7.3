package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.rules.BetaRules;
import dev.b173.rules.ai.BetaBrain;
import dev.b173.rules.ai.BetaBrains;
import dev.b173.rules.ai.BetaWolves;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void b173$betaInteract(Player player, InteractionHand hand,
                                   CallbackInfoReturnable<InteractionResult> callback) {
        Wolf self = (Wolf) (Object) this;
        if (BetaRules.betaWorld(self.level())) {
            callback.setReturnValue(BetaWolves.interact(self, player, hand));
        }
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/wolf/Wolf;isPathFinding()Z"))
    private boolean b173$betaPathing(boolean pathFinding) {
        BetaBrain brain = BetaBrains.brainOf((Wolf) (Object) this);
        return brain != null ? brain.hasPath() : pathFinding;
    }

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float b173$betaHide(float amount, @Local(argsOnly = true) ServerLevel level,
                                @Local(argsOnly = true) DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker != null && !(attacker instanceof Player) && BetaRules.active(level.getServer())) {
            return (float) (((int) amount + 1) / 2);
        }
        return amount;
    }
}
