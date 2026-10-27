package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaMobs;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.animal.chicken.Chicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Chicken.class)
public abstract class ChickenDespawnMixin {
    @ModifyReturnValue(method = "removeWhenFarAway", at = @At("RETURN"))
    private boolean b173$betaDespawn(boolean remove) {
        Chicken self = (Chicken) (Object) this;
        return remove || BetaMobs.despawnsLikeBeta(self) && BetaRules.betaWorld(self.level());
    }
}
