package dev.b173.core;

import java.util.Set;

public final class BetaMining {
    private static final float[] HARDNESS = new float[97];

    static {
        String table = "1:1.5 2:0.6 3:0.5 4:2.0 5:2.0 6:0.0 7:-1.0 8:100.0 9:100.0 10:0.0 11:100.0 12:0.5 13:0.6 "
                + "14:3.0 15:3.0 16:3.0 17:2.0 18:0.2 19:0.6 20:0.3 21:3.0 22:3.0 23:3.5 24:0.8 25:0.8 26:0.2 27:0.7 "
                + "28:0.7 29:0.5 30:4.0 31:0.0 32:0.0 33:0.5 34:0.5 35:0.8 36:-1.0 37:0.0 38:0.0 39:0.0 40:0.0 41:3.0 "
                + "42:5.0 43:2.0 44:2.0 45:2.0 46:0.0 47:1.5 48:2.0 49:10.0 50:0.0 51:0.0 52:5.0 53:2.0 54:2.5 55:0.0 "
                + "56:3.0 57:5.0 58:2.5 59:0.0 60:0.6 61:3.5 62:3.5 63:1.0 64:3.0 65:0.4 66:0.7 67:2.0 68:1.0 69:0.5 "
                + "70:0.5 71:5.0 72:0.5 73:3.0 74:3.0 75:0.0 76:0.0 77:0.5 78:0.1 79:0.5 80:0.2 81:0.4 82:0.6 83:0.0 "
                + "84:2.0 85:2.0 86:1.0 87:0.4 88:0.5 89:0.3 90:-1.0 91:1.0 92:0.5 93:0.0 94:0.0 95:0.0 96:3.0";
        for (String pair : table.split(" ")) {
            String[] kv = pair.split(":");
            HARDNESS[Integer.parseInt(kv[0])] = Float.parseFloat(kv[1]);
        }
    }

    private static final Set<Integer> SHOVEL = Set.of(2, 3, 12, 13, 60, 78, 80, 82);
    private static final Set<Integer> PICKAXE = Set.of(1, 4, 14, 15, 16, 21, 22, 24, 41, 42, 43, 44, 48, 56, 57, 79, 87);
    private static final Set<Integer> AXE = Set.of(5, 17, 47, 54);

    private static final int COBWEB = 30;

    private BetaMining() {
    }

    public static float hardness(int block) {
        return block > 0 && block < HARDNESS.length ? HARDNESS[block] : Float.NaN;
    }

    public static float strength(int item, int block) {
        float material = switch (item) {
            case 269, 270, 271 -> 2.0F;
            case 273, 274, 275 -> 4.0F;
            case 256, 257, 258 -> 6.0F;
            case 277, 278, 279 -> 8.0F;
            case 284, 285, 286 -> 12.0F;
            default -> 0.0F;
        };
        switch (item) {
            case 256, 269, 273, 277, 284:
                return SHOVEL.contains(block) ? material : 1.0F;
            case 257, 270, 274, 278, 285:
                return PICKAXE.contains(block) ? material : 1.0F;
            case 258, 271, 275, 279, 286:
                return AXE.contains(block) ? material : 1.0F;
            case 267, 268, 272, 276, 283:
                return block == COBWEB ? 15.0F : 1.5F;
            case 359:
                return block == 18 || block == COBWEB ? 15.0F : block == 35 ? 5.0F : 1.0F;
            default:
                return 1.0F;
        }
    }
}
