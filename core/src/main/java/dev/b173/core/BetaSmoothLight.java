package dev.b173.core;

public final class BetaSmoothLight {
    public interface Access {
        int id(int x, int y, int z);

        float brightness(int x, int y, int z, int minLight);
    }

    public static final boolean[] GRASS = new boolean[256];

    public static final int[] LIGHT_VALUE = new int[256];

    static {
        for (int id : new int[] {0, 6, 27, 28, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 65, 66, 69, 75, 76, 77, 78, 83,
                90, 93, 94}) {
            GRASS[id] = true;
        }
        int[][] light = {{10, 15}, {11, 15}, {39, 1}, {50, 14}, {51, 15}, {62, 13}, {74, 9}, {76, 7}, {89, 15},
                {90, 11}, {91, 15}, {94, 9}, {95, 15}};
        for (int[] pair : light) {
            LIGHT_VALUE[pair[0]] = pair[1];
        }
    }

    public static final boolean[] SMOOTH = new boolean[256];

    static {
        for (int id : new int[] {1, 2, 3, 4, 5, 7, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 35, 41, 42,
                43, 44, 45, 46, 47, 48, 49, 52, 54, 56, 57, 58, 60, 61, 62, 70, 72, 73, 74, 77, 78, 79, 80, 82, 84, 86,
                87, 88, 89, 90, 91, 92, 95, 96, 53, 67, 85, 69, 93, 94, 29, 33, 34}) {
            SMOOTH[id] = true;
        }
    }

    public static boolean takesNeighbourLight(int id) {
        return id == 44 || id == 60 || id == 53 || id == 67;
    }

    public static int corner(int face, float x, float y, float z, float minA, float maxA, float minB, float maxB) {
        float a = face <= 3 ? x : y;
        float b = face <= 1 ? z : face <= 3 ? y : z;
        boolean plusA = maxA > minA && Math.abs(a - maxA) < Math.abs(a - minA);
        boolean plusB = maxB > minB && Math.abs(b - maxB) < Math.abs(b - minB);
        return (plusA ? 2 : 0) + (plusB ? 1 : 0);
    }

    public static float[] table(float base) {
        float[] table = new float[16];
        for (int i = 0; i <= 15; i++) {
            float f = 1.0F - (float) i / 15.0F;
            table[i] = (1.0F - f) / (f * 3.0F + 1.0F) * (1.0F - base) + base;
        }
        return table;
    }

    public static final float[] SHADE = {0.5F, 1.0F, 0.8F, 0.8F, 0.6F, 0.6F};

    private static final int[][] NORMAL = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};
    private static final int[][] AXIS_A = {{1, 0, 0}, {1, 0, 0}, {1, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, 1, 0}};
    private static final int[][] AXIS_B = {{0, 0, 1}, {0, 0, 1}, {0, 1, 0}, {0, 1, 0}, {0, 0, 1}, {0, 0, 1}};

    private BetaSmoothLight() {
    }

    public static float[] face(Access world, boolean[] grass, int face, int x, int y, int z, int lightValue) {
        int[] n = NORMAL[face];
        int[] a = AXIS_A[face];
        int[] b = AXIS_B[face];
        int fx = x + n[0];
        int fy = y + n[1];
        int fz = z + n[2];
        float f = world.brightness(fx, fy, fz, lightValue);
        float aMinus = world.brightness(fx - a[0], fy - a[1], fz - a[2], lightValue);
        float aPlus = world.brightness(fx + a[0], fy + a[1], fz + a[2], lightValue);
        float bMinus = world.brightness(fx - b[0], fy - b[1], fz - b[2], lightValue);
        float bPlus = world.brightness(fx + b[0], fy + b[1], fz + b[2], lightValue);
        boolean gaMinus = grass[world.id(fx - a[0], fy - a[1], fz - a[2])];
        boolean gaPlus = grass[world.id(fx + a[0], fy + a[1], fz + a[2])];
        boolean gbMinus = grass[world.id(fx - b[0], fy - b[1], fz - b[2])];
        boolean gbPlus = grass[world.id(fx + b[0], fy + b[1], fz + b[2])];
        boolean alongB = face >= 4;
        float cmm = gaMinus || gbMinus ? corner(world, fx, fy, fz, a, b, -1, -1, lightValue) : alongB ? bMinus : aMinus;
        float cmp = gaMinus || gbPlus ? corner(world, fx, fy, fz, a, b, -1, 1, lightValue) : alongB ? bPlus : aMinus;
        float cpm = gaPlus || gbMinus ? corner(world, fx, fy, fz, a, b, 1, -1, lightValue) : alongB ? bMinus : aPlus;
        float cpp = gaPlus || gbPlus ? corner(world, fx, fy, fz, a, b, 1, 1, lightValue) : alongB ? bPlus : aPlus;
        float[] out = new float[4];
        if (face <= 1) {
            out[1] = (cmp + aMinus + bPlus + f) / 4.0F;
            out[3] = (bPlus + f + cpp + aPlus) / 4.0F;
            out[2] = (f + bMinus + aPlus + cpm) / 4.0F;
            out[0] = (aMinus + cmm + f + bMinus) / 4.0F;
        } else {
            out[1] = (aMinus + cmp + f + bPlus) / 4.0F;
            out[3] = (f + bPlus + aPlus + cpp) / 4.0F;
            out[2] = (bMinus + f + cpm + aPlus) / 4.0F;
            out[0] = (cmm + aMinus + bMinus + f) / 4.0F;
        }
        return out;
    }

    private static float corner(Access world, int fx, int fy, int fz, int[] a, int[] b, int sa, int sb, int lightValue) {
        return world.brightness(fx + sa * a[0] + sb * b[0], fy + sa * a[1] + sb * b[1], fz + sa * a[2] + sb * b[2],
                lightValue);
    }

    public static int colour(float brightness, int face, boolean tinted, float r, float g, float b) {
        float shade = SHADE[face];
        return pack((tinted ? r : 1.0F) * shade * brightness, (tinted ? g : 1.0F) * shade * brightness,
                (tinted ? b : 1.0F) * shade * brightness);
    }

    public static int overlayColour(float brightness, int face, float r, float g, float b) {
        float shade = SHADE[face];
        return pack(shade * brightness * r, shade * brightness * g, shade * brightness * b);
    }

    private static int pack(float r, float g, float b) {
        int red = Math.clamp((int) (r * 255.0F), 0, 255);
        int green = Math.clamp((int) (g * 255.0F), 0, 255);
        int blue = Math.clamp((int) (b * 255.0F), 0, 255);
        return 0xFF000000 | blue << 16 | green << 8 | red;
    }
}
