package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.rules.BetaRiding;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
abstract class LivingEntityDismountMixin {
    @ModifyExpressionValue(method = "dismountVehicle", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getDismountLocationForPassenger(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 b173$betaDismount(Vec3 modern, @Local(argsOnly = true) Entity vehicle) {
        if (!BetaRules.betaWorld(vehicle.level())) {
            return modern;
        }
        Vec3 beta = BetaRiding.dismountPosition(vehicle);
        return beta != null ? beta : modern;
    }

    @ModifyExpressionValue(method = "dismountVehicle", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D"))
    private double b173$stayPut(double modern, @Local(argsOnly = true) Entity vehicle) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (vehicle.isRemoved() && BetaRules.betaWorld(vehicle.level()) && BetaRiding.dismountPosition(vehicle) != null) {
            return self.getY();
        }
        return modern;
    }
}
