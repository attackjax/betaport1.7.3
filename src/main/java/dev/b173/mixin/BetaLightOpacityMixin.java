package dev.b173.mixin;

import dev.b173.rules.BetaRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class BetaLightOpacityMixin {
    @Shadow
    public abstract Block getBlock();

    @Inject(method = "getLightDampening", at = @At("HEAD"), cancellable = true)
    private void b173$betaOpacity(CallbackInfoReturnable<Integer> cir) {
        if (!BetaRules.betaGame()) {
            return;
        }
        Block block = getBlock();
        if (block == Blocks.WATER || block == Blocks.ICE) {
            cir.setReturnValue(3);
        } else if (block == Blocks.LAVA || block == Blocks.CHEST || block == Blocks.FARMLAND) {
            cir.setReturnValue(15);
        } else if (block == Blocks.SPAWNER) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "propagatesSkylightDown", at = @At("HEAD"), cancellable = true)
    private void b173$betaSkyDown(CallbackInfoReturnable<Boolean> cir) {
        if (!BetaRules.betaGame()) {
            return;
        }
        Block block = getBlock();
        if (block == Blocks.CHEST || block == Blocks.FARMLAND) {
            cir.setReturnValue(false);
        } else if (block == Blocks.SPAWNER) {
            cir.setReturnValue(true);
        }
    }
}
