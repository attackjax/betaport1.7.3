package dev.b173.mixin.vehicle;

import dev.b173.rules.BetaVehicles;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pig.class)
abstract class PigRidingMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void b173$betaGetOff(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
        InteractionResult beta = BetaVehicles.interactPig((Pig) (Object) this, player);
        if (beta != null) {
            info.setReturnValue(beta);
        }
    }
}
