package dev.b173.client.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class BetaChests {
    private static final String[] SIDES = {"down", "up", "north", "south", "west", "east"};

    private BetaChests() {
    }

    public static String variant(BlockGetter level, BlockPos pos, BlockState state) {
        int[] look = dev.b173.content.BetaChests.look(level, pos);
        String front = SIDES[look[0]];
        if (look[1] < 0) {
            return "single_" + front;
        }
        return (look[1] == 2 || look[1] == 3 ? "z_" : "x_") + SIDES[look[1]] + "_" + front;
    }
}
