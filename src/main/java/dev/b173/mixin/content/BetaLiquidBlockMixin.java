package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.rules.BetaFluidFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlock.class)
abstract class BetaLiquidBlockMixin {
    @Inject(method = "shouldSpreadLiquid", at = @At("HEAD"), cancellable = true)
    private void b173$betaWake(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (BetaFluidFlow.handles(level, state)) {
            cir.setReturnValue(BetaFluidFlow.shouldUpdate(level, pos, state));
        }
    }

    @Inject(method = "onPlace", at = @At("HEAD"))
    private void b173$placed(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved, CallbackInfo ci) {
        if (BetaFluidFlow.handles(level, state)) {
            BetaFluidFlow.placed(level, pos);
        }
    }

    @WrapWithCondition(method = "updateShape", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/ScheduledTickAccess;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"))
    private boolean b173$noShapeWake(ScheduledTickAccess ticks, BlockPos pos, Fluid fluid, int delay,
            @Local(argsOnly = true, ordinal = 0) BlockState state, @Local(argsOnly = true) LevelReader level) {
        return !(level instanceof Level real && BetaFluidFlow.handles(real, state));
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void b173$oneRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (BetaFluidFlow.handles(level, state)) {
            ci.cancel();
        }
    }
}
