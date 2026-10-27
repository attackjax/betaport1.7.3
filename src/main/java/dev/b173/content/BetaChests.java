package dev.b173.content;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaChest;
import dev.b173.world.BetaBlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class BetaChests {
    private static final Direction[] SIDES = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH,
            Direction.WEST, Direction.EAST};

    private BetaChests() {
    }

    public static BetaChest.Blocks ids(BlockGetter level) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        return (x, y, z) -> BetaBlockStates.id(BetaBlockStates.beta(level.getBlockState(cursor.set(x, y, z))));
    }

    public static BetaChest.Blocks loadedIds(ServerLevel level) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        return (x, y, z) -> {
            LevelChunk chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
            return chunk == null ? 0 : BetaBlockStates.id(BetaBlockStates.beta(chunk.getBlockState(cursor.set(x, y, z))));
        };
    }

    public static int[] look(BlockGetter level, BlockPos pos) {
        return BetaChest.look(ids(level), pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean canPlace(BlockGetter level, BlockPos pos) {
        return BetaChest.canPlace(ids(level), pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockState state(BlockGetter level, BlockPos pos, BlockState chest) {
        return state(ids(level), pos, chest);
    }

    public static BlockState state(BetaChest.Blocks world, BlockPos pos, BlockState chest) {
        if (!chest.is(Blocks.CHEST)) {
            return chest;
        }
        int[] look = BetaChest.look(world, pos.getX(), pos.getY(), pos.getZ());
        Direction front = SIDES[look[0]];
        ChestType type = ChestType.SINGLE;
        if (look[1] >= 0) {
            type = front.getClockWise() == SIDES[look[1]] ? ChestType.LEFT : ChestType.RIGHT;
        }
        return chest.setValue(ChestBlock.FACING, front).setValue(ChestBlock.TYPE, type);
    }

    public static boolean blockedAbove(BlockGetter level, BlockPos pos) {
        return BetaBlocks.isNormalCube(BetaBlockStates.id(BetaBlockStates.beta(level.getBlockState(pos.above()))));
    }
}
