package dev.b173.mixin;

import dev.b173.rules.BetaRiding;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityRidingMixin {
    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            at = @At("HEAD"), cancellable = true)
    private void b173$betaSeat(Entity passenger, Entity.MoveFunction callback, CallbackInfo info) {
        Entity self = (Entity) (Object) this;
        if (!BetaRules.betaWorld(self.level())) {
            return;
        }
        Vec3 position = BetaRiding.riderPosition(self, passenger);
        if (position != null) {
            callback.accept(passenger, position.x, position.y, position.z);
            info.cancel();
        }
    }
}
