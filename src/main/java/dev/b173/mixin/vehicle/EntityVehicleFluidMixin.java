package dev.b173.mixin.vehicle;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
abstract class EntityVehicleFluidMixin {
    @ModifyReturnValue(method = "isPushedByFluid", at = @At("RETURN"))
    private boolean b173$betaVehicles(boolean modern) {
        Entity self = (Entity) (Object) this;
        return modern && !((self instanceof AbstractMinecart || self instanceof Boat) && BetaRules.betaWorld(self.level()));
    }
}
