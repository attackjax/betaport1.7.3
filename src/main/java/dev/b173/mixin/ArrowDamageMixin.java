package dev.b173.mixin;

import dev.b173.rules.BetaCombat;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractArrow.class)
public abstract class ArrowDamageMixin {
    @ModifyArg(method = "onHitEntity", index = 1, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float b173$betaArrowDamage(float damage) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        return self instanceof Arrow && BetaRules.betaWorld(self.level()) ? BetaCombat.ARROW_DAMAGE : damage;
    }
}
