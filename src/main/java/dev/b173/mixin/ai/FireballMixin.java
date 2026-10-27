package dev.b173.mixin.ai;

import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LargeFireball.class)
abstract class FireballMixin {
    @ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 6.0F))
    private float b173$betaDirectHit(float damage) {
        return BetaRules.betaWorld(((LargeFireball) (Object) this).level()) ? 0.0F : damage;
    }
}
