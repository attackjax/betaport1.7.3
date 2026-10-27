package dev.b173.rules;

import dev.b173.core.BetaBlocks;
import dev.b173.world.BetaBlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BetaLeaves {
    private static final int RANGE = 4;
    private static final int SIZE = 32;
    private static final int HALF = SIZE / 2;
    private static final int[] SURROUNDINGS = new int[SIZE * SIZE * SIZE];

    private BetaLeaves() {
    }

    public static boolean leaves(BlockState state) {
        return BetaBlockStates.id(BetaBlockStates.beta(state)) == BetaBlocks.LEAVES && state.getBlock() instanceof LeavesBlock;
    }

    public static BlockState marked(BlockState state) {
        return state.setValue(LeavesBlock.PERSISTENT, false).setValue(LeavesBlock.DISTANCE, LeavesBlock.DECAY_DISTANCE);
    }

    public static boolean isMarked(BlockState state) {
        return !state.getValue(LeavesBlock.PERSISTENT) && state.getValue(LeavesBlock.DISTANCE) == LeavesBlock.DECAY_DISTANCE;
    }

    private static BlockState cleared(BlockState state) {
        return state.setValue(LeavesBlock.DISTANCE, 1);
    }

    public static void removed(ServerLevel level, BlockPos pos, BlockState old) {
        int id = BetaBlockStates.id(BetaBlockStates.beta(old));
        int radius = id == BetaBlocks.LEAVES ? 1 : id == BetaBlocks.LOG ? RANGE : -1;
        if (radius < 0) {
            return;
        }
        int reach = radius + 1;
        if (!level.hasChunksAt(pos.offset(-reach, -reach, -reach), pos.offset(reach, reach, reach))) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (leaves(state) && !isMarked(state)) {
                        level.setBlock(cursor, marked(state), Block.UPDATE_NONE);
                    }
                }
            }
        }
    }

    public static void randomTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (!isMarked(state)) {
            return;
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int reach = RANGE + 1;
        if (level.hasChunksAt(pos.offset(-reach, -reach, -reach), pos.offset(reach, reach, reach))) {
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int dx = -RANGE; dx <= RANGE; dx++) {
                for (int dy = -RANGE; dy <= RANGE; dy++) {
                    for (int dz = -RANGE; dz <= RANGE; dz++) {
                        int id = BetaBlockStates.id(BetaBlockStates.beta(level.getBlockState(cursor.set(x + dx, y + dy, z + dz))));
                        SURROUNDINGS[index(dx, dy, dz)] = id == BetaBlocks.LOG ? 0 : id == BetaBlocks.LEAVES ? -2 : -1;
                    }
                }
            }
            for (int step = 1; step <= 4; step++) {
                for (int dx = -RANGE; dx <= RANGE; dx++) {
                    for (int dy = -RANGE; dy <= RANGE; dy++) {
                        for (int dz = -RANGE; dz <= RANGE; dz++) {
                            if (SURROUNDINGS[index(dx, dy, dz)] != step - 1) {
                                continue;
                            }
                            spread(dx - 1, dy, dz, step);
                            spread(dx + 1, dy, dz, step);
                            spread(dx, dy - 1, dz, step);
                            spread(dx, dy + 1, dz, step);
                            spread(dx, dy, dz - 1, step);
                            spread(dx, dy, dz + 1, step);
                        }
                    }
                }
            }
        }
        if (SURROUNDINGS[index(0, 0, 0)] >= 0) {
            level.setBlock(pos, cleared(state), Block.UPDATE_NONE);
        } else {
            Block.dropResources(state, level, pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void spread(int dx, int dy, int dz, int step) {
        int i = index(dx, dy, dz);
        if (SURROUNDINGS[i] == -2) {
            SURROUNDINGS[i] = step;
        }
    }

    private static int index(int dx, int dy, int dz) {
        return (dx + HALF) * SIZE * SIZE + (dy + HALF) * SIZE + dz + HALF;
    }
}
