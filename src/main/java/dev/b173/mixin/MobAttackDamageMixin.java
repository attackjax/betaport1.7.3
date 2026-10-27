package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.b173.rules.BetaMobs;
import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobAttackDamageMixin {
    @ModifyExpressionValue(method = "doHurtTarget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double b173$betaAttack(double damage, ServerLevel level, Entity target) {
        Mob self = (Mob) (Object) this;
        if (BetaMobs.isBeta(self.getType()) && BetaRules.active(level.getServer())) {
            return BetaMobs.attackDamage(self, (float) damage);
        }
        return damage;
    }
}
