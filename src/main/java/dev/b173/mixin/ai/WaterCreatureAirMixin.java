package dev.b173.mixin.ai;

import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableWaterCreature.class)
abstract class WaterCreatureAirMixin {
    @Inject(method = "handleAirSupply", at = @At("HEAD"), cancellable = true)
    private void b173$betaAir(int airSupply, CallbackInfo callback) {
        AgeableWaterCreature self = (AgeableWaterCreature) (Object) this;
        if (self.getType() == EntityType.SQUID && BetaRules.betaWorld(self.level())) {
            self.setAirSupply(self.getMaxAirSupply());
            callback.cancel();
        }
    }
}
