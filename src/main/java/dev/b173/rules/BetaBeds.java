package dev.b173.rules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class BetaBeds {
    private BetaBeds() {
    }

    public static BlockPos emptySpot(Level level, BlockPos head, Direction facing, int skip) {
        int y = head.getY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int part = 0; part <= 1; part++) {
            BlockPos centre = part == 0 ? head : head.relative(facing.getOpposite());
            for (int x = centre.getX() - 1; x <= centre.getX() + 1; x++) {
                for (int z = centre.getZ() - 1; z <= centre.getZ() + 1; z++) {
                    if (!level.isLoaded(pos.set(x, y, z))) {
                        continue;
                    }
                    BlockState below = level.getBlockState(pos.set(x, y - 1, z));
                    if (!below.isRedstoneConductor(level, pos) || !level.getBlockState(pos.set(x, y, z)).isAir()
                            || !level.getBlockState(pos.set(x, y + 1, z)).isAir()) {
                        continue;
                    }
                    if (skip > 0) {
                        skip--;
                    } else {
                        return new BlockPos(x, y, z);
                    }
                }
            }
        }
        return null;
    }
}
