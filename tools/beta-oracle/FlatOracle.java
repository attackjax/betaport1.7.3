import java.lang.reflect.Field;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public final class FlatOracle {
    private static final int[] BLOCKS = {1, 6, 30, 32, 37, 38, 39, 40, 83, 50, 75, 76, 51, 8, 9, 10, 11, 55, 59, 64, 71,
            65, 27, 28, 66, 81, 26, 53, 85, 44, 69, 77, 70, 92, 96};
    private static final int[][] OFFSETS = {{0, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};
    private static final String[] PLACES = {"self", "below", "above", "north", "south", "west", "east"};
    private static final float[] SHADES = {1.0F, 0.8F, 0.6F, 0.5F};

    private FlatOracle() {
    }

    public static void main(String[] args) throws Exception {
        int samples = Integer.parseInt(args[0]);
        float[] table = new float[16];
        for (int i = 0; i <= 15; i++) {
            float f = 1.0F - (float) i / 15.0F;
            table[i] = (1.0F - f) / (f * 3.0F + 1.0F) * (1.0F - 0.05F) + 0.05F;
        }
        Field raw = nw.class.getDeclaredField("g");
        Field count = nw.class.getDeclaredField("h");
        Field drawing = nw.class.getDeclaredField("w");
        raw.setAccessible(true);
        count.setAccessible(true);
        drawing.setAccessible(true);
        Random random = new Random(173);
        for (int id : BLOCKS) {
            Map<String, Integer> found = new TreeMap<>();
            int errors = 0;
            for (int s = 0; s < samples; s++) {
                final int[][][] ids = new int[3][3][3];
                final int[][][] levels = new int[3][3][3];
                final int[][][] metas = new int[3][3][3];
                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        for (int z = 0; z < 3; z++) {
                            ids[x][y][z] = random.nextInt(3) == 0 ? 1 : 0;
                            levels[x][y][z] = random.nextInt(16);
                            metas[x][y][z] = random.nextInt(16);
                        }
                    }
                }
                ids[1][1][1] = id;
                if (id == 64 || id == 71) {
                    metas[1][1][1] &= 7;
                    ids[1][2][1] = id;
                    metas[1][2][1] = 8;
                }
                if (id == 66) {
                    metas[1][1][1] %= 10;
                }
                if (id == 27 || id == 28) {
                    metas[1][1][1] %= 6;
                }
                xp world = new xp() {
                    private boolean in(int x, int y, int z) {
                        return x >= -1 && x <= 1 && y >= 63 && y <= 65 && z >= -1 && z <= 1;
                    }

                    public int a(int x, int y, int z) {
                        return in(x, y, z) ? ids[x + 1][y - 63][z + 1] : 0;
                    }

                    public ow b(int x, int y, int z) {
                        return null;
                    }

                    public float a(int x, int y, int z, int min) {
                        return table[Math.max(in(x, y, z) ? levels[x + 1][y - 63][z + 1] : 15, min)];
                    }

                    public float c(int x, int y, int z) {
                        return table[in(x, y, z) ? levels[x + 1][y - 63][z + 1] : 15];
                    }

                    public int e(int x, int y, int z) {
                        return in(x, y, z) ? metas[x + 1][y - 63][z + 1] : 0;
                    }

                    public ln f(int x, int y, int z) {
                        int b = a(x, y, z);
                        return b == 0 ? ln.a : uu.m[b].bA;
                    }

                    public boolean g(int x, int y, int z) {
                        return uu.o[a(x, y, z)];
                    }

                    public boolean h(int x, int y, int z) {
                        int b = a(x, y, z);
                        return b != 0 && uu.m[b].bA.h() && uu.m[b].d();
                    }

                    public xv a() {
                        return null;
                    }
                };
                cv render = new cv(world);
                drawing.setBoolean(nw.a, false);
                nw.a.b();
                int tint;
                try {
                    tint = uu.m[id].b(world, 0, 64, 0);
                    render.b(uu.m[id], 0, 64, 0);
                } catch (RuntimeException e) {
                    errors++;
                    continue;
                }
                float tr = (tint >> 16 & 255) / 255.0F;
                float tg = (tint >> 8 & 255) / 255.0F;
                float tb = (tint & 255) / 255.0F;
                int[] buffer = (int[]) raw.get(nw.a);
                int vertices = count.getInt(nw.a);
                int lightValue = uu.s[id];
                for (int q = 0; q + 6 <= vertices; q += 6) {
                    int[] corners = {q, q + 1, q + 2, q + 5};
                    float[][] p = new float[4][3];
                    int[] colours = new int[4];
                    for (int k = 0; k < 4; k++) {
                        for (int c = 0; c < 3; c++) {
                            p[k][c] = Float.intBitsToFloat(buffer[corners[k] * 8 + c]);
                        }
                        colours[k] = buffer[corners[k] * 8 + 5];
                    }
                    String facing = facing(p);
                    StringBuilder answers = new StringBuilder();
                    for (int o = 0; o < OFFSETS.length; o++) {
                        int[] off = OFFSETS[o];
                        float b = world.a(off[0], 64 + off[1], off[2], lightValue);
                        for (float shade : SHADES) {
                            boolean all = true;
                            for (int colour : colours) {
                                all &= close(colour, shade * b * tr, shade * b * tg, shade * b * tb);
                            }
                            if (all) {
                                answers.append(PLACES[o]).append('*').append(shade).append(' ');
                            }
                        }
                    }
                    String key = "block " + id + " facing " + facing + ": "
                            + (answers.length() == 0 ? "none" : answers.toString().trim());
                    found.merge(key, 1, Integer::sum);
                }
            }
            for (Map.Entry<String, Integer> e : found.entrySet()) {
                System.out.println(e.getKey() + "  x" + e.getValue());
            }
            if (errors > 0) {
                System.out.println("block " + id + " threw in " + errors + " samples");
            }
        }
    }

    private static boolean close(int colour, float r, float g, float b) {
        int red = colour & 255;
        int green = colour >> 8 & 255;
        int blue = colour >> 16 & 255;
        return Math.abs(red - (int) (r * 255.0F)) <= 1 && Math.abs(green - (int) (g * 255.0F)) <= 1
                && Math.abs(blue - (int) (b * 255.0F)) <= 1;
    }

    private static String facing(float[][] p) {
        float ux = p[1][0] - p[0][0], uy = p[1][1] - p[0][1], uz = p[1][2] - p[0][2];
        float vx = p[2][0] - p[0][0], vy = p[2][1] - p[0][1], vz = p[2][2] - p[0][2];
        float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
        float ax = Math.abs(nx), ay = Math.abs(ny), az = Math.abs(nz);
        float max = Math.max(ax, Math.max(ay, az));
        if (max < 1.0E-6F) {
            return "flat";
        }
        int axes = (ax > max * 0.99F ? 1 : 0) + (ay > max * 0.99F ? 1 : 0) + (az > max * 0.99F ? 1 : 0);
        if (axes > 1 || (ax > 0.01F * max ? 1 : 0) + (ay > 0.01F * max ? 1 : 0) + (az > 0.01F * max ? 1 : 0) > 1) {
            return "slanted";
        }
        if (ay == max) {
            return ny > 0 ? "up" : "down";
        }
        if (az == max) {
            return nz > 0 ? "south" : "north";
        }
        return nx > 0 ? "east" : "west";
    }
}
