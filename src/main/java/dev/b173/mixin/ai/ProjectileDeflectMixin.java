package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Projectile.class)
abstract class ProjectileDeflectMixin {
    @WrapWithCondition(method = "deflect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/Projectile;setOwner(Lnet/minecraft/world/entity/EntityReference;)V"))
    private boolean b173$keepGhastOwner(Projectile self, EntityReference<?> owner) {
        return !(self instanceof LargeFireball && BetaRules.betaWorld(self.level()));
    }
}
