package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityBetaBodyMixin {
    @ModifyReturnValue(method = "increaseAirSupply", at = @At("RETURN"))
    private int b173$breathAtOnce(int air) {
        LivingEntity self = (LivingEntity) (Object) this;
        return BetaRules.betaWorld(self.level()) ? self.getMaxAirSupply() : air;
    }

    @WrapMethod(method = "stopSleeping")
    private void b173$keepFacing(Operation<Void> original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player && BetaRules.betaWorld(self.level())) {
            float yaw = self.getYRot();
            float pitch = self.getXRot();
            original.call();
            self.setYRot(yaw);
            self.setXRot(pitch);
        } else {
            original.call();
        }
    }
}
