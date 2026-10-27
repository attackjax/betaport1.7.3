package dev.b173.core;

import java.util.BitSet;

public final class BetaFire {
    private static final int[] ENCOURAGE = new int[256];
    private static final int[] ABILITY = new int[256];
    private static final BitSet MATERIAL_BURNS = new BitSet(256);

    static {
        flammable(BetaBlocks.PLANKS, 5, 20);
        flammable(85, 5, 20);
        flammable(53, 5, 20);
        flammable(BetaBlocks.LOG, 5, 5);
        flammable(BetaBlocks.LEAVES, 30, 60);
        flammable(47, 30, 20);
        flammable(46, 15, 100);
        flammable(BetaBlocks.TALL_GRASS, 60, 100);
        flammable(35, 30, 60);
        for (int id : new int[] {5, 17, 18, 25, 26, 35, 46, 47, 53, 54, 58, 63, 64, 68, 72, 84, 85, 95, 96}) {
            MATERIAL_BURNS.set(id);
        }
    }

    private BetaFire() {
    }

    private static void flammable(int id, int encourage, int ability) {
        ENCOURAGE[id] = encourage;
        ABILITY[id] = ability;
    }

    public static int encourage(int id) {
        return ENCOURAGE[id & 255];
    }

    public static int ability(int id) {
        return ABILITY[id & 255];
    }

    public static boolean materialBurns(int id) {
        return MATERIAL_BURNS.get(id & 255);
    }
}
