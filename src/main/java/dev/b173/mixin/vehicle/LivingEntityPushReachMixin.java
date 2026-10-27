package dev.b173.mixin.vehicle;

import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
abstract class LivingEntityPushReachMixin {
    @ModifyArg(method = "pushEntities", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getPushableEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"),
            index = 1)
    private AABB b173$betaReach(AABB box) {
        return BetaRules.betaWorld(((LivingEntity) (Object) this).level())
                ? box.inflate((double) 0.2F, 0.0D, (double) 0.2F) : box;
    }
}
