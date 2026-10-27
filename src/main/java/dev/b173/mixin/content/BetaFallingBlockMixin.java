package dev.b173.mixin.content;

import dev.b173.rules.BetaRules;
import dev.b173.world.BetaBlockStates;
import dev.b173.core.BetaBlocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlock.class)
abstract class BetaFallingBlockMixin {
    @Inject(method = "getDelayAfterPlace", at = @At("HEAD"), cancellable = true)
    private void b173$betaDelay(CallbackInfoReturnable<Integer> cir) {
        if (BetaRules.betaGame()) {
            cir.setReturnValue(3);
        }
    }

    @Inject(method = "isFree", at = @At("HEAD"), cancellable = true)
    private static void b173$betaFree(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (BetaRules.betaGame()) {
            int id = BetaBlockStates.id(BetaBlockStates.beta(state));
            cir.setReturnValue(id == 0 || id == BetaBlocks.FIRE || BetaBlocks.isWater(id) || BetaBlocks.isLava(id));
        }
    }
}
