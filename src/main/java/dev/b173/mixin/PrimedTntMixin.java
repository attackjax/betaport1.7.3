package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.b173.rules.BetaExplosions;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PrimedTnt.class)
abstract class PrimedTntMixin {
    @ModifyExpressionValue(method = "explode", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/PrimedTnt;getY(D)D"))
    private double b173$betaMiddle(double y) {
        PrimedTnt self = (PrimedTnt) (Object) this;
        return BetaRules.betaWorld(self.level()) ? self.getY() + BetaExplosions.TNT_MIDDLE : y;
    }
}
