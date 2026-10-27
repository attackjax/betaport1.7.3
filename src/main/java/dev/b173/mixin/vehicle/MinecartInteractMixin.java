package dev.b173.mixin.vehicle;

import dev.b173.rules.BetaVehicles;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecart.class)
abstract class MinecartInteractMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void b173$betaClick(Player player, InteractionHand hand, Vec3 at, CallbackInfoReturnable<InteractionResult> info) {
        InteractionResult beta = BetaVehicles.interact((Minecart) (Object) this, player);
        if (beta != null) {
            info.setReturnValue(beta);
        }
    }
}
