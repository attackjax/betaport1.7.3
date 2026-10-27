package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownEgg.class)
public abstract class ThrownEggMixin {
    @WrapWithCondition(method = "onHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/chicken/Chicken;setAge(I)V"))
    private boolean b173$grownChicks(Chicken chicken, int age) {
        return !BetaRules.betaWorld(chicken.level());
    }
}
