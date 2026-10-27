package dev.b173.mixin.vehicle;

import dev.b173.rules.BetaRules;
import dev.b173.rules.BetaVehicles;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityRideTurnMixin {
    @Unique
    private final double[] b173$owed = new double[2];

    @Inject(method = "rideTick", at = @At("TAIL"))
    private void b173$turnWithVehicle(CallbackInfo info) {
        Entity self = (Entity) (Object) this;
        Entity vehicle = self.getVehicle();
        if (vehicle == null || !self.isLocalInstanceAuthoritative() || !BetaRules.betaWorld(self.level())) {
            return;
        }
        BetaVehicles.followVehicle(self, vehicle, this.b173$owed);
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void b173$forgetTurn(CallbackInfo info) {
        this.b173$owed[0] = 0.0D;
        this.b173$owed[1] = 0.0D;
    }
}
