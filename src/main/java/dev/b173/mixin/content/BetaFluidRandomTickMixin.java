package dev.b173.mixin.content;

import dev.b173.rules.BetaFluidFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Fluid.class)
abstract class BetaFluidRandomTickMixin {
    @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
    private void b173$waterTicks(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof WaterFluid) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void b173$betaWater(ServerLevel level, BlockPos pos, FluidState fluid, RandomSource random, CallbackInfo ci) {
        BlockState state = level.getBlockState(pos);
        if (BetaFluidFlow.handles(level, state)) {
            BetaFluidFlow.randomTick(level, pos, state, random);
        }
    }
}
