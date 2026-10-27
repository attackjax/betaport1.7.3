package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.b173.content.BetaMechanics;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SaplingBlock.class)
abstract class SaplingGrowMixin {
    @WrapOperation(method = "advanceTree", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/grower/TreeGrower;growTree(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;)Z"))
    private boolean b173$betaTree(TreeGrower grower, ServerLevel level, ChunkGenerator generator, BlockPos pos,
                                  BlockState state, RandomSource random, Operation<Boolean> original) {
        if (BetaMechanics.saplingKind(state) < 0) {
            return original.call(grower, level, generator, pos, state, random);
        }
        return BetaMechanics.growTree(level, pos, state, random);
    }
}
