package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHurtingProjectile.class)
abstract class FireballFlightMixin {
    @Shadow
    private void applyInertia() {
    }

    private boolean b173$beta() {
        AbstractHurtingProjectile self = (AbstractHurtingProjectile) (Object) this;
        return self instanceof LargeFireball && BetaRules.betaWorld(self.level());
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/hurtingprojectile/AbstractHurtingProjectile;applyInertia()V"))
    private boolean b173$notYet(AbstractHurtingProjectile self) {
        return !b173$beta();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void b173$afterMoving(CallbackInfo ci) {
        if (b173$beta() && !((AbstractHurtingProjectile) (Object) this).isRemoved()) {
            applyInertia();
        }
    }
}
