package dev.b173.mixin.content;

import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
abstract class BetaContainerClicksMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void b173$betaClicks(int slot, int button, ContainerInput input, Player player, CallbackInfo ci) {
        boolean beta = (input == ContainerInput.PICKUP || input == ContainerInput.QUICK_MOVE) && (button == 0 || button == 1);
        if (!beta && BetaRules.betaWorld(player.level())) {
            ci.cancel();
        }
    }
}
