import java.io.File;
import java.util.List;
import java.util.Random;

public final class ColourOracle {
    private ColourOracle() {
    }

    public static void main(String[] args) {
        long seed = Long.parseLong(args[0]);
        int count = Integer.parseInt(args[1]);
        long positionSeed = Long.parseLong(args[2]);

        final ei info = new ei(seed, "colours");
        wt saves = new wt() {
            public ei c() {
                return info;
            }

            public void b() {
            }

            public bf a(xa provider) {
                return null;
            }

            public void a(ei worldInfo, List players) {
            }

            public void a(ei worldInfo) {
            }

            public File a(String name) {
                return null;
            }
        };
        fd world = new fd(saves, "colours", seed);
        final xv climate = world.a();

        int[] identity = new int[65536];
        for (int i = 0; i < identity.length; i++) {
            identity[i] = i;
        }
        ia.a(identity);
        jh.a(identity);

        final int[] metadata = {0};
        xp access = new xp() {
            public int a(int x, int y, int z) {
                return 0;
            }

            public ow b(int x, int y, int z) {
                return null;
            }

            public float a(int x, int y, int z, int light) {
                return 0.0F;
            }

            public float c(int x, int y, int z) {
                return 0.0F;
            }

            public int e(int x, int y, int z) {
                return metadata[0];
            }

            public ln f(int x, int y, int z) {
                return null;
            }

            public boolean g(int x, int y, int z) {
                return false;
            }

            public boolean h(int x, int y, int z) {
                return false;
            }

            public xv a() {
                return climate;
            }
        };

        Random positions = new Random(positionSeed);
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < count; i++) {
            int x = positions.nextInt(4000000) - 2000000;
            int y = positions.nextInt(128);
            int z = positions.nextInt(4000000) - 2000000;
            metadata[0] = 0;
            int grass = uu.m[2].b(access, x, y, z);
            int leaves = uu.m[18].b(access, x, y, z);
            metadata[0] = 1;
            int tallGrass = uu.m[31].b(access, x, y, z);
            hash = mix(mix(mix(hash, grass), leaves), tallGrass);
            if (i < 3) {
                System.out.printf("  at %d %d %d: grass pixel %d, oak leaves pixel %d, tall grass pixel %d%n",
                        x, y, z, grass, leaves, tallGrass);
            }
        }
        System.out.printf("seed %d, %d positions from %d: checksum 0x%016xL%n", seed, count, positionSeed, hash);
    }

    private static long mix(long hash, int value) {
        for (int shift = 0; shift < 32; shift += 8) {
            hash = (hash ^ (value >>> shift & 255)) * 0x100000001b3L;
        }
        return hash;
    }
}
