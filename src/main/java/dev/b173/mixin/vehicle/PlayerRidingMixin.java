package dev.b173.mixin.vehicle;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaVehicles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
abstract class PlayerRidingMixin {
    @ModifyReturnValue(method = "wantsToStopRiding", at = @At("RETURN"))
    private boolean b173$noKeyToGetOff(boolean modern) {
        Entity vehicle = ((Player) (Object) this).getVehicle();
        return modern && !(vehicle != null && BetaVehicles.isBetaVehicle(vehicle));
    }
}
