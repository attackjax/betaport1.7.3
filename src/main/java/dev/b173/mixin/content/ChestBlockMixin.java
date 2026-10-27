package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.content.BetaChests;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChestBlock.class)
abstract class ChestBlockMixin {
    @ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
    private BlockState b173$betaPlacement(BlockState state, BlockPlaceContext context) {
        if (state == null) {
            return null;
        }
        if (!BetaChests.canPlace(context.getLevel(), context.getClickedPos())) {
            return null;
        }
        return BetaChests.state(context.getLevel(), context.getClickedPos(), state);
    }

    @ModifyReturnValue(method = "updateShape", at = @At("RETURN"))
    private BlockState b173$betaShape(BlockState state, BlockState original, LevelReader level, ScheduledTickAccess ticks,
                                      BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbour,
                                      RandomSource random) {
        return BetaChests.state(level, pos, state);
    }

    @ModifyReturnValue(method = "isBlockedChestByBlock", at = @At("RETURN"))
    private static boolean b173$normalCubeAbove(boolean blocked, BlockGetter level, BlockPos pos) {
        return BetaChests.blockedAbove(level, pos);
    }
}
