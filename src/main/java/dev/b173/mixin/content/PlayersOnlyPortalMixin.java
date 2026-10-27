package dev.b173.mixin.content;

import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class PlayersOnlyPortalMixin {
    @Inject(method = "canUsePortal", at = @At("HEAD"), cancellable = true)
    private void b173$playersOnly(boolean allowPassengers, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player) && BetaRules.betaWorld(self.level())) {
            cir.setReturnValue(false);
        }
    }
}
