package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.rules.BetaCombat;
import dev.b173.rules.ai.BetaWolves;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerWolfAlertMixin {
    @Inject(method = "hurtServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Avatar;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void b173$wolvesDefend(ServerLevel level, DamageSource source, float amount,
                                   CallbackInfoReturnable<Boolean> callback) {
        if (BetaCombat.betaAttacker(source) instanceof LivingEntity attacker) {
            BetaWolves.alert((Player) (Object) this, attacker, false);
        }
    }

    @Inject(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            shift = At.Shift.AFTER))
    private void b173$wolvesJoinIn(Entity target, CallbackInfo callback) {
        if (target instanceof LivingEntity living && living.isAlive()) {
            BetaWolves.alert((Player) (Object) this, living, true);
        }
    }
}
