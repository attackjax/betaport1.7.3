package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.b173.rules.BetaCombat;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerCombatMixin {
    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double b173$betaDamage(double modern) {
        Player self = (Player) (Object) this;
        if (!BetaRules.betaWorld(self.level())) {
            return modern;
        }
        int damage = BetaCombat.weaponDamage(self.getWeaponItem());
        if (damage < 0) {
            return modern;
        }
        return damage + (BetaCombat.fallingPoint(self) ? 1 : 0);
    }

    @Inject(method = "canCriticalAttack", at = @At("HEAD"), cancellable = true)
    private void b173$noCriticalHits(Entity target, CallbackInfoReturnable<Boolean> callback) {
        if (BetaRules.betaWorld(((Player) (Object) this).level())) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "isSweepAttack", at = @At("HEAD"), cancellable = true)
    private void b173$noSweeps(boolean fullStrength, boolean critical, boolean knockback,
                               CallbackInfoReturnable<Boolean> callback) {
        if (BetaRules.betaWorld(((Player) (Object) this).level())) {
            callback.setReturnValue(false);
        }
    }
}
