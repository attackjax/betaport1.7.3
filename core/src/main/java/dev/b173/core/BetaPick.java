package dev.b173.core;

public final class BetaPick {
    private BetaPick() {
    }

    public static int picked(int blockId) {
        return switch (blockId) {
            case BetaBlocks.GRASS -> BetaBlocks.DIRT;
            case 43 -> 44;
            case 7 -> 1;
            default -> blockId;
        };
    }

    public static int slot(int[] inventoryIds, int blockId) {
        int wanted = picked(blockId);
        for (int i = 0; i < inventoryIds.length; i++) {
            if (inventoryIds[i] == wanted && wanted != 0) {
                return i < 9 ? i : -1;
            }
        }
        return -1;
    }
}
