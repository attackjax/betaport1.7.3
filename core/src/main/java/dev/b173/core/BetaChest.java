package dev.b173.core;

public final class BetaChest {
    @FunctionalInterface
    public interface Blocks {
        int id(int x, int y, int z);
    }

    private static final int SIDE = 26;

    private BetaChest() {
    }

    public static boolean canPlace(Blocks world, int x, int y, int z) {
        int beside = 0;
        beside += world.id(x - 1, y, z) == BetaBlocks.CHEST ? 1 : 0;
        beside += world.id(x + 1, y, z) == BetaBlocks.CHEST ? 1 : 0;
        beside += world.id(x, y, z - 1) == BetaBlocks.CHEST ? 1 : 0;
        beside += world.id(x, y, z + 1) == BetaBlocks.CHEST ? 1 : 0;
        if (beside > 1) {
            return false;
        }
        return !paired(world, x - 1, y, z) && !paired(world, x + 1, y, z)
                && !paired(world, x, y, z - 1) && !paired(world, x, y, z + 1);
    }

    private static boolean paired(Blocks world, int x, int y, int z) {
        if (world.id(x, y, z) != BetaBlocks.CHEST) {
            return false;
        }
        return world.id(x - 1, y, z) == BetaBlocks.CHEST || world.id(x + 1, y, z) == BetaBlocks.CHEST
                || world.id(x, y, z - 1) == BetaBlocks.CHEST || world.id(x, y, z + 1) == BetaBlocks.CHEST;
    }

    public static int[] look(Blocks world, int x, int y, int z) {
        int north = world.id(x, y, z - 1);
        int south = world.id(x, y, z + 1);
        int west = world.id(x - 1, y, z);
        int east = world.id(x + 1, y, z);
        if (north == BetaBlocks.CHEST || south == BetaBlocks.CHEST) {
            int partnerZ = north == BetaBlocks.CHEST ? z - 1 : z + 1;
            int partnerWest = world.id(x - 1, y, partnerZ);
            int partnerEast = world.id(x + 1, y, partnerZ);
            int front = 5;
            if ((opaque(west) || opaque(partnerWest)) && !opaque(east) && !opaque(partnerEast)) {
                front = 5;
            }
            if ((opaque(east) || opaque(partnerEast)) && !opaque(west) && !opaque(partnerWest)) {
                front = 4;
            }
            return new int[] {front, north == BetaBlocks.CHEST ? 2 : 3};
        }
        if (west == BetaBlocks.CHEST || east == BetaBlocks.CHEST) {
            int partnerX = west == BetaBlocks.CHEST ? x - 1 : x + 1;
            int partnerNorth = world.id(partnerX, y, z - 1);
            int partnerSouth = world.id(partnerX, y, z + 1);
            int front = 3;
            if ((opaque(north) || opaque(partnerNorth)) && !opaque(south) && !opaque(partnerSouth)) {
                front = 3;
            }
            if ((opaque(south) || opaque(partnerSouth)) && !opaque(north) && !opaque(partnerNorth)) {
                front = 2;
            }
            return new int[] {front, west == BetaBlocks.CHEST ? 4 : 5};
        }
        int front = 3;
        if (opaque(north) && !opaque(south)) {
            front = 3;
        }
        if (opaque(south) && !opaque(north)) {
            front = 2;
        }
        if (opaque(west) && !opaque(east)) {
            front = 5;
        }
        if (opaque(east) && !opaque(west)) {
            front = 4;
        }
        return new int[] {front, -1};
    }

    public static int texture(Blocks world, int x, int y, int z, int side) {
        if (side == 0 || side == 1) {
            return SIDE - 1;
        }
        int[] look = look(world, x, y, z);
        int front = look[0];
        int partner = look[1];
        if (partner < 0) {
            return side == front ? SIDE + 1 : SIDE;
        }
        boolean alongZ = partner == 2 || partner == 3;
        if (alongZ ? side == 2 || side == 3 : side == 4 || side == 5) {
            return SIDE;
        }
        int offset = partner == 2 || partner == 4 ? -1 : 0;
        if (side == (alongZ ? 4 : 3)) {
            offset = -1 - offset;
        }
        return (side == front ? SIDE + 16 : SIDE + 32) + offset;
    }

    private static boolean opaque(int id) {
        return BetaBlocks.isOpaqueCube(id);
    }
}
