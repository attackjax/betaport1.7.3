package dev.b173.core.world;

import dev.b173.core.BetaBlocks;

public final class BetaPlants {
    private BetaPlants() {
    }

    public static boolean canBlockStay(BetaWorld world, int plant, int x, int y, int z) {
        int below = world.getBlockId(x, y - 1, z);
        if (plant == BetaBlocks.BROWN_MUSHROOM || plant == BetaBlocks.RED_MUSHROOM) {
            if (y < 0 || y >= 128) {
                return false;
            }
            return world.getFullBlockLightValue(x, y, z) < 13 && BetaBlocks.isOpaqueCube(below);
        }
        boolean lit = world.getFullBlockLightValue(x, y, z) >= 8 || world.canBlockSeeTheSky(x, y, z);
        if (!lit) {
            return false;
        }
        if (plant == BetaBlocks.DEAD_BUSH) {
            return below == BetaBlocks.SAND;
        }
        return BetaBlocks.isPlantSoil(below);
    }

    public static boolean reedCanStay(BetaWorld world, int x, int y, int z) {
        int below = world.getBlockId(x, y - 1, z);
        if (below == BetaBlocks.REEDS) {
            return true;
        }
        if (below != BetaBlocks.GRASS && below != BetaBlocks.DIRT) {
            return false;
        }
        return world.isWater(x - 1, y - 1, z) || world.isWater(x + 1, y - 1, z)
                || world.isWater(x, y - 1, z - 1) || world.isWater(x, y - 1, z + 1);
    }

    public static boolean cactusCanStay(BetaWorld world, int x, int y, int z) {
        if (world.isSolid(x - 1, y, z) || world.isSolid(x + 1, y, z)
                || world.isSolid(x, y, z - 1) || world.isSolid(x, y, z + 1)) {
            return false;
        }
        int below = world.getBlockId(x, y - 1, z);
        return below == BetaBlocks.CACTUS || below == BetaBlocks.SAND;
    }

    public static boolean snowCanStay(BetaWorld world, int x, int y, int z) {
        int below = world.getBlockId(x, y - 1, z);
        if (below == 0 || !BetaBlocks.isOpaqueCube(below)) {
            return false;
        }
        return BetaBlocks.isSolid(below);
    }
}
