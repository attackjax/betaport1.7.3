package dev.b173.mixin.content;

import dev.b173.rules.BetaFluidFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.class)
abstract class BetaFlowMixin {
    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("HEAD"), cancellable = true)
    private void b173$betaFlow(ServerLevel level, BlockPos pos, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if (BetaFluidFlow.handles(level, blockState)) {
            ci.cancel();
            BetaFluidFlow.tick(level, pos, blockState);
        }
    }
}
