package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.rules.BetaLeaves;
import dev.b173.rules.BetaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
abstract class BetaLeavesMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void b173$betaDecay(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (BetaRules.betaWorld(level)) {
            ci.cancel();
            BetaLeaves.randomTick(level, pos, state);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void b173$noDistance(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (BetaRules.betaWorld(level)) {
            ci.cancel();
        }
    }

    @WrapWithCondition(method = "updateShape", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/ScheduledTickAccess;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
    private boolean b173$noDistanceUpdate(ScheduledTickAccess ticks, BlockPos pos, Block block, int delay,
            @Local(argsOnly = true) LevelReader level) {
        return !(level instanceof Level real && BetaRules.betaWorld(real));
    }

    @ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
    private BlockState b173$placedMarked(BlockState state, BlockPlaceContext context) {
        return state != null && BetaRules.betaWorld(context.getLevel()) ? BetaLeaves.marked(state) : state;
    }
}
