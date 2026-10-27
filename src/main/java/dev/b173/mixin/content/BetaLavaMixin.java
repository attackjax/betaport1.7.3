package dev.b173.mixin.content;

import dev.b173.rules.BetaFluidFlow;
import dev.b173.rules.BetaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LavaFluid.class)
abstract class BetaLavaMixin {
    @Inject(method = "getTickDelay", at = @At("HEAD"), cancellable = true)
    private void b173$betaRate(LevelReader level, CallbackInfoReturnable<Integer> cir) {
        if (level instanceof Level real && BetaRules.betaWorld(real)) {
            cir.setReturnValue(BetaFluidFlow.tickRate(true));
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void b173$betaRandomTick(ServerLevel level, BlockPos pos, FluidState fluid, RandomSource random, CallbackInfo ci) {
        BlockState state = level.getBlockState(pos);
        if (BetaFluidFlow.handles(level, state)) {
            ci.cancel();
            BetaFluidFlow.randomTick(level, pos, state, random);
        } else if (BetaRules.betaWorld(level)) {
            ci.cancel();
        }
    }
}
