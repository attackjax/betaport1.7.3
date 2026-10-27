import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class NetherOracle {
    private NetherOracle() {
    }

    public static void main(String[] args) throws Exception {
        long seed = Long.parseLong(args[0]);
        int minX = Integer.parseInt(args[1]);
        int minZ = Integer.parseInt(args[2]);
        int maxX = Integer.parseInt(args[3]);
        int maxZ = Integer.parseInt(args[4]);
        File out = new File(args[5]);
        boolean arrival = false;
        boolean light = false;
        for (int i = 6; i < args.length; i++) {
            arrival |= args[i].equals("--arrival");
            light |= args[i].equals("--light");
        }

        final ei info = new ei(seed, "oracle");
        wt saves = new wt() {
            public ei c() {
                return info;
            }

            public void b() {
            }

            public bf a(xa provider) {
                return new bf() {
                    public lm a(fd world, int x, int z) {
                        return null;
                    }

                    public void a(fd world, lm chunk) {
                    }

                    public void b(fd world, lm chunk) {
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
        fd world = new fd(saves, "oracle", seed, xa.a(-1));
        world.r = new Random(seed ^ 0x5DEECE66DL);

        final qn generator = new qn(world, seed);
        final Map<Long, lm> chunks = new HashMap<>();
        cl provider = new cl() {
            public boolean a(int x, int z) {
                return chunks.containsKey(key(x, z));
            }

            public lm b(int x, int z) {
                lm chunk = chunks.get(key(x, z));
                if (chunk == null) {
                    chunk = generator.b(x, z);
                    chunks.put(key(x, z), chunk);
                }
                return chunk;
            }

            public lm c(int x, int z) {
                return b(x, z);
            }

            public void a(cl self, int x, int z) {
                generator.a(self, x, z);
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
                return "NetherOracle";
            }
        };
        world.v = provider;

        qn baseGenerator = new qn(world, seed);
        byte[][] base = dumpChunks(baseGenerator, minX, minZ, maxX, maxZ);

        long start = System.nanoTime();
        int decorated = 0;
        if (!arrival) {
            for (int x = minX - 2; x <= maxX + 2; x++) {
                for (int z = minZ - 2; z <= maxZ + 2; z++) {
                    provider.b(x, z);
                }
            }
            for (int x = minX - 2; x <= maxX + 1; x++) {
                for (int z = minZ - 2; z <= maxZ + 1; z++) {
                    generator.b(x + 1, z + 1);
                    provider.a(provider, x, z);
                    decorated++;
                }
            }
        } else {
            for (int x = minX - 2; x <= maxX + 2; x++) {
                for (int z = minZ - 2; z <= maxZ + 2; z++) {
                    provider.b(x, z);
                    if (x > minX - 2 && z > minZ - 2) {
                        provider.a(provider, x - 1, z - 1);
                        decorated++;
                    }
                    if (light) {
                        while (world.j()) {
                        }
                    }
                }
            }
        }
        byte[][] populated = dumpWorld(world, minX, minZ, maxX, maxZ);
        System.out.printf("Beta 1.7.3 Nether: generated %d chunks and decorated %d in %.1f s (%s%s)%n",
                chunks.size(), decorated, (System.nanoTime() - start) / 1e9,
                arrival ? "arrival order" : "diagonal convention", light ? ", light queue" : "");

        try (DataOutputStream data = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out)))) {
            data.writeBytes("B173NETH");
            data.writeInt(1);
            data.writeLong(seed);
            data.writeInt(minX);
            data.writeInt(minZ);
            data.writeInt(maxX);
            data.writeInt(maxZ);
            data.writeBoolean(arrival);
            data.writeBoolean(light);
            data.write(base[0]);
            data.write(base[1]);
            data.write(populated[0]);
            data.write(populated[1]);
            System.out.println("wrote " + out);
        }
    }

    private static long key(int x, int z) {
        return (long) x << 32 ^ (z & 0xFFFFFFFFL);
    }

    private static byte[][] dumpChunks(qn generator, int minX, int minZ, int maxX, int maxZ) {
        Map<Long, lm> fresh = new HashMap<>();
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                fresh.put(key(cx, cz), generator.b(cx, cz));
            }
        }
        int width = (maxX - minX + 1) * 16;
        int depth = (maxZ - minZ + 1) * 16;
        byte[] ids = new byte[width * depth * 128];
        byte[] meta = new byte[width * depth * 128];
        int i = 0;
        for (int x = minX * 16; x < (maxX + 1) * 16; x++) {
            for (int z = minZ * 16; z < (maxZ + 1) * 16; z++) {
                lm chunk = fresh.get(key(x >> 4, z >> 4));
                for (int y = 0; y < 128; y++) {
                    ids[i] = (byte) chunk.a(x & 15, y, z & 15);
                    meta[i] = (byte) chunk.b(x & 15, y, z & 15);
                    i++;
                }
            }
        }
        return new byte[][]{ids, meta};
    }

    private static byte[][] dumpWorld(fd world, int minX, int minZ, int maxX, int maxZ) {
        int width = (maxX - minX + 1) * 16;
        int depth = (maxZ - minZ + 1) * 16;
        byte[] ids = new byte[width * depth * 128];
        byte[] meta = new byte[width * depth * 128];
        int i = 0;
        for (int x = minX * 16; x < (maxX + 1) * 16; x++) {
            for (int z = minZ * 16; z < (maxZ + 1) * 16; z++) {
                for (int y = 0; y < 128; y++) {
                    ids[i] = (byte) world.a(x, y, z);
                    meta[i] = (byte) world.e(x, y, z);
                    i++;
                }
            }
        }
        return new byte[][]{ids, meta};
    }
}
