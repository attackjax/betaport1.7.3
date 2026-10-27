package dev.b173.mixin.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BubbleColumnBlock.class)
abstract class BubbleColumnMixin {
    @Inject(method = "updateColumn(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"), cancellable = true)
    private static void b173$noColumn(Block block, LevelAccessor level, BlockPos pos, BlockState state, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "updateColumn(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"), cancellable = true)
    private static void b173$noColumnFrom(Block block, LevelAccessor level, BlockPos pos, BlockState state,
                                          BlockState below, CallbackInfo ci) {
        ci.cancel();
    }
}
