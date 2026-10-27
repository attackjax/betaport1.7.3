package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public abstract class ArrowPhysicsMixin {
    @ModifyReturnValue(method = "getDefaultGravity", at = @At("RETURN"))
    private double b173$betaGravity(double gravity) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        return self instanceof Arrow && BetaRules.betaWorld(self.level()) ? 0.03F : gravity;
    }

    @ModifyReturnValue(method = "getWaterInertia", at = @At("RETURN"))
    private float b173$betaWaterDrag(float inertia) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        return self instanceof Arrow && BetaRules.betaWorld(self.level()) ? 0.8F : inertia;
    }
}
