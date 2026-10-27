package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.b173.rules.BetaRules;
import dev.b173.rules.ai.BetaBrains;
import dev.b173.rules.ai.BetaSquidBrain;
import net.minecraft.world.entity.animal.squid.Squid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Squid.class)
abstract class SquidMixin {
    @Inject(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/AgeableWaterCreature;aiStep()V", shift = At.Shift.AFTER),
            cancellable = true)
    private void b173$betaSwim(CallbackInfo callback) {
        if (BetaBrains.brainOf((Squid) (Object) this) instanceof BetaSquidBrain brain) {
            brain.swim();
            callback.cancel();
        }
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/squid/Squid;isInWater()Z"))
    private boolean b173$betaInWater(boolean inWater) {
        Squid self = (Squid) (Object) this;
        return BetaRules.betaWorld(self.level()) ? BetaSquidBrain.betaInWater(self) : inWater;
    }

    @WrapWithCondition(method = "hurtServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/squid/Squid;spawnInk()V"))
    private boolean b173$noInk(Squid self) {
        return !BetaRules.betaWorld(self.level());
    }
}
