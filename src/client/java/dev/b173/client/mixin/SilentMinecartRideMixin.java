package dev.b173.client.mixin;

import dev.b173.rules.BetaRules;
import net.minecraft.client.resources.sounds.RidingMinecartSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RidingMinecartSoundInstance.class)
abstract class SilentMinecartRideMixin {
    @Inject(method = "shoudlPlaySound", at = @At("HEAD"), cancellable = true)
    private void b173$silent(CallbackInfoReturnable<Boolean> cir) {
        if (BetaRules.clientInBetaWorld) {
            cir.setReturnValue(false);
        }
    }
}
