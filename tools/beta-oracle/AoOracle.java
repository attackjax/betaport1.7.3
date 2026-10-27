import java.lang.reflect.Field;
import java.util.Random;

public final class AoOracle {
    private static final int[] IDS = {0, 0, 0, 0, 1, 2, 3, 4, 5, 12, 13, 17, 18, 20, 24, 35, 44, 45, 48, 49, 53,
            54, 58, 61, 79, 80, 82, 85, 87, 88, 89, 91};
    private static final int[] CENTRES = {1, 2, 3, 4, 5, 12, 13, 17, 18, 20, 24, 35, 45, 48, 49, 58, 79, 80, 82,
            87, 88, 89, 91};

    private AoOracle() {
    }

    public static void main(String[] args) throws Exception {
        int samples = Integer.parseInt(args[0]);
        long seed = Long.parseLong(args[1]);
        StringBuilder grass = new StringBuilder("grass");
        StringBuilder light = new StringBuilder("light");
        for (int i = 0; i < 256; i++) {
            grass.append(uu.r[i] ? " 1" : " 0");
            light.append(' ').append(uu.s[i]);
        }
        System.out.println(grass);
        System.out.println(light);
        StringBuilder types = new StringBuilder("render");
        for (int i = 0; i < 256; i++) {
            types.append(" ").append(uu.m[i] == null ? -1 : uu.m[i].b());
        }
        System.out.println(types);
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
        Random random = new Random(seed);
        StringBuilder out = new StringBuilder();
        for (int s = 0; s < samples; s++) {
            final int[][][] ids = new int[5][5][5];
            final int[][][] levels = new int[5][5][5];
            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    for (int z = 0; z < 5; z++) {
                        ids[x][y][z] = IDS[random.nextInt(IDS.length)];
                        levels[x][y][z] = random.nextInt(16);
                    }
                }
            }
            int centre = CENTRES[random.nextInt(CENTRES.length)];
            ids[2][2][2] = centre;
            float r = random.nextBoolean() ? 1.0F : random.nextFloat();
            float g = random.nextBoolean() ? 1.0F : random.nextFloat();
            float b = random.nextBoolean() ? 1.0F : random.nextFloat();
            xp world = new xp() {
                private boolean in(int x, int y, int z) {
                    return x >= -2 && x <= 2 && y >= 62 && y <= 66 && z >= -2 && z <= 2;
                }

                public int a(int x, int y, int z) {
                    return in(x, y, z) ? ids[x + 2][y - 62][z + 2] : 0;
                }

                public ow b(int x, int y, int z) {
                    return null;
                }

                public float a(int x, int y, int z, int min) {
                    int level = in(x, y, z) ? levels[x + 2][y - 62][z + 2] : 15;
                    return table[Math.max(level, min)];
                }

                public float c(int x, int y, int z) {
                    return table[in(x, y, z) ? levels[x + 2][y - 62][z + 2] : 15];
                }

                public int e(int x, int y, int z) {
                    return 0;
                }

                public ln f(int x, int y, int z) {
                    int id = a(x, y, z);
                    return id == 0 ? ln.a : uu.m[id].bA;
                }

                public boolean g(int x, int y, int z) {
                    return uu.o[a(x, y, z)];
                }

                public boolean h(int x, int y, int z) {
                    int id = a(x, y, z);
                    return id != 0 && uu.m[id].bA.h() && uu.m[id].d();
                }

                public xv a() {
                    return null;
                }
            };
            cv render = new cv(world);
            drawing.setBoolean(nw.a, false);
            nw.a.b();
            render.a(uu.m[centre], 0, 64, 0, r, g, b);
            int[] buffer = (int[]) raw.get(nw.a);
            int vertices = count.getInt(nw.a);
            out.append("sample ").append(s).append(' ').append(centre).append(' ')
                    .append(Float.floatToIntBits(r)).append(' ').append(Float.floatToIntBits(g)).append(' ')
                    .append(Float.floatToIntBits(b)).append('\n');
            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    for (int z = 0; z < 5; z++) {
                        out.append("cell ").append(x - 2).append(' ').append(y - 2).append(' ').append(z - 2)
                                .append(' ').append(ids[x][y][z]).append(' ').append(levels[x][y][z]).append('\n');
                    }
                }
            }
            for (int v = 0; v < vertices; v++) {
                out.append("v ").append(buffer[v * 8]).append(' ').append(buffer[v * 8 + 1]).append(' ')
                        .append(buffer[v * 8 + 2]).append(' ').append(buffer[v * 8 + 5]).append('\n');
            }
        }
        if (args.length > 2 && args[2].equals("check")) {
            check(out.toString());
        } else {
            System.out.print(out);
        }
    }

    private static void check(String text) {
        StringBuilder record = new StringBuilder();
        long hash = 0xcbf29ce484222325L;
        String[] lines = text.split("\n");
        int i = 0;
        while (i < lines.length) {
            i++;
            i += 125;
            java.util.List<int[]> verts = new java.util.ArrayList<>();
            while (i < lines.length && lines[i].startsWith("v ")) {
                String[] c = lines[i++].split(" ");
                verts.add(new int[] {Integer.parseInt(c[1]), Integer.parseInt(c[2]), Integer.parseInt(c[3]),
                        Integer.parseInt(c[4])});
            }
            int drawn = 0;
            int twice = 0;
            for (int q = 0; q + 6 <= verts.size(); q += 6) {
                int[][] quad = {verts.get(q), verts.get(q + 1), verts.get(q + 2), verts.get(q + 5)};
                int face = faceOf(quad);
                if ((drawn & 1 << face) != 0) {
                    twice |= 1 << face;
                }
                drawn |= 1 << face;
                int[] byCorner = new int[4];
                for (int[] v : quad) {
                    byCorner[cornerOf(face, v)] = v[3];
                }
                for (int colour : byCorner) {
                    for (int shift = 0; shift < 32; shift += 8) {
                        hash = (hash ^ (colour >>> shift & 255)) * 0x100000001b3L;
                    }
                }
            }
            record.append(String.format("%02x%x", drawn, twice == 0 ? 0 : 1));
        }
        System.out.println(record);
        System.out.printf("hash 0x%016x%n", hash);
    }

    private static float c(int[] v, int axis) {
        return Float.intBitsToFloat(v[axis]);
    }

    private static int faceOf(int[][] quad) {
        float[][] want = {{1, 64}, {1, 65}, {2, 0}, {2, 1}, {0, 0}, {0, 1}};
        for (int face = 0; face < 6; face++) {
            boolean all = true;
            for (int[] v : quad) {
                all &= c(v, (int) want[face][0]) == want[face][1];
            }
            if (all) {
                return face;
            }
        }
        throw new IllegalStateException("not a face");
    }

    private static int cornerOf(int face, int[] v) {
        float a;
        float b;
        if (face <= 1) {
            a = c(v, 0);
            b = c(v, 2);
        } else if (face <= 3) {
            a = c(v, 0);
            b = c(v, 1) - 64;
        } else {
            a = c(v, 1) - 64;
            b = c(v, 2);
        }
        return (a > 0.5F ? 2 : 0) + (b > 0.5F ? 1 : 0);
    }
}
