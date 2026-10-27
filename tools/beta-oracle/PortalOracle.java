import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class PortalOracle {
    static fd world;
    static final Map<Long, lm> chunks = new HashMap<>();
    static int terrain;

    static int top(int x, int z) {
        switch (terrain) {
            case 1: return 100;
            case 2: return 96 + ((x * 7 + z * 13) & 7);
            default: return 127;
        }
    }

    static void makeWorld() {
        final ei info = new ei(0L, "oracle");
        wt saves = new wt() {
            public ei c() {
                return info;
            }

            public void b() {
            }

            public bf a(xa provider) {
                return new bf() {
                    public lm a(fd w, int x, int z) {
                        return null;
                    }

                    public void a(fd w, lm chunk) {
                    }

                    public void b(fd w, lm chunk) {
                    }

                    public void a() {
                    }

                    public void b() {
                    }
                };
            }

            public void a(ei worldInfo, List players) {
            }

            public void a(ei worldInfo) {
            }

            public File a(String name) {
                return null;
            }
        };
        world = new fd(saves, "oracle", 0L);
        world.r = new Random(1L);
        world.v = new cl() {
            public boolean a(int x, int z) {
                return true;
            }

            public lm b(int x, int z) {
                return chunk(x, z);
            }

            public lm c(int x, int z) {
                return chunk(x, z);
            }

            public void a(cl self, int x, int z) {
            }

            public boolean a(boolean all, yb progress) {
                return true;
            }

            public boolean a() {
                return false;
            }

            public boolean b() {
                return false;
            }

            public String c() {
                return "VehicleOracle";
            }
        };
    }

    static lm chunk(int cx, int cz) {
        long key = (long) cx << 32 ^ (cz & 0xFFFFFFFFL);
        lm c = chunks.get(key);
        if (c == null) {
            byte[] blocks = new byte[32768];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int h = top(cx * 16 + x, cz * 16 + z);
                    for (int y = 0; y <= h; y++) {
                        blocks[x << 11 | z << 7 | y] = 1;
                    }
                }
            }
            c = new lm(world, blocks, cx, cz);
            chunks.put(key, c);
        }
        return c;
    }

    static ls traveller(double x, double y, double z) {
        ls e = new ls(world) {
        };
        e.c(x, y, z, 0.0F, 0.0F);
        return e;
    }

    static ur teleporter(long seed) throws Exception {
        ur t = new ur();
        java.lang.reflect.Field f = ur.class.getDeclaredField("a");
        f.setAccessible(true);
        f.set(t, new Random(seed));
        return t;
    }

    static void dump(String name, int cx, int cz, int r) {
        StringBuilder out = new StringBuilder();
        int count = 0;
        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                for (int y = 0; y < 128; y++) {
                    int id = world.a(x, y, z);
                    if (id == 49 || id == 90) {
                        out.append(String.format("OR %s block %d %d %d %d%n", name, x, y, z, id));
                        count++;
                    }
                }
            }
        }
        System.out.print(out);
        System.out.printf("OR %s blocks %d%n", name, count);
    }

    static void build(String name, int t, long seed, double x, double y, double z) throws Exception {
        terrain = t;
        chunks.clear();
        makeWorld();
        ls e = traveller(x, y, z);
        teleporter(seed).a(world, e);
        dump(name, (int) Math.floor(x), (int) Math.floor(z), 21);
        System.out.printf("OR %s placed %.4f %.4f %.4f%n", name, e.aM, e.aN, e.aO);
    }

    public static void main(String[] args) throws Exception {
        Class.forName("uu");
        for (long seed = 1; seed <= 4; seed++) {
            build("flat" + seed, 1, seed, 0.5, 101.0, 0.5);
        }
        build("bumpy1", 2, 1, 3.2, 110.0, -5.7);
        build("bumpy2", 2, 2, -4.6, 99.0, 2.3);
        build("under", 1, 3, 0.5, 30.0, 0.5);
        build("solid1", 3, 1, 0.5, 40.0, 0.5);
        build("solid2", 3, 3, 7.5, 125.0, 2.5);
        System.out.println("OR two portals");
        terrain = 1;
        chunks.clear();
        makeWorld();
        teleporter(5).a(world, traveller(0.5, 101.0, 0.5));
        teleporter(6).a(world, traveller(200.5, 101.0, 200.5));
        dump("second", 200, 200, 21);
        double[][] at = {{90.0, 101.0, 90.0}, {101.0, 101.0, 100.0}, {110.0, 101.0, 110.0}, {-100.3, 64.0, 90.1},
                {150.0, 101.0, 170.0}, {100.5, 30.0, 100.5}};
        for (double[] a : at) {
            ls e = traveller(a[0], a[1], a[2]);
            boolean found = teleporter(7).b(world, e);
            System.out.printf("OR find %.1f %.1f %.1f -> %s %.4f %.4f %.4f%n", a[0], a[1], a[2], found, e.aM, e.aN, e.aO);
        }
    }
}
