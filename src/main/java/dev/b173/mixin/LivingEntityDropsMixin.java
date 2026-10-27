package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.b173.rules.BetaDrops;
import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDropsMixin {
    @WrapOperation(method = "dropAllDeathLoot", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/entity/LivingEntity;dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V"))
    private void b173$betaDrops(LivingEntity self, ServerLevel level, DamageSource source, boolean killedByPlayer,
                                Operation<Void> original) {
        if (BetaRules.active(level.getServer()) && BetaDrops.dropOnDeath(level, self)) {
            return;
        }
        original.call(self, level, source, killedByPlayer);
    }
}
