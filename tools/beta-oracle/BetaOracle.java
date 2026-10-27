import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BetaOracle {
    private BetaOracle() {
    }

    public static void main(String[] args) throws Exception {
        long seed = Long.parseLong(args[0]);
        int minX = Integer.parseInt(args[1]);
        int minZ = Integer.parseInt(args[2]);
        int maxX = Integer.parseInt(args[3]);
        int maxZ = Integer.parseInt(args[4]);
        File out = new File(args[5]);
        List<int[]> order = args.length > 6 ? readOrder(new File(args[6])) : null;

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
        fd world = new fd(saves, "oracle", seed);
        world.r = new Random(seed ^ 0x5DEECE66DL);

        final yf generator = new yf(world, seed);
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
                return "BetaOracle";
            }
        };
        world.v = provider;

        int fromX = minX - 2;
        int fromZ = minZ - 2;
        int toX = maxX + 2;
        int toZ = maxZ + 2;
        if (order != null) {
            for (int[] chunk : order) {
                fromX = Math.min(fromX, chunk[0] - 2);
                fromZ = Math.min(fromZ, chunk[1] - 2);
                toX = Math.max(toX, chunk[0] + 2);
                toZ = Math.max(toZ, chunk[1] + 2);
            }
        }
        long start = System.nanoTime();
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                provider.b(x, z);
            }
        }
        byte[][] base = dumpBlocks(world, minX, minZ, maxX, maxZ);
        if (order == null) {
            order = new ArrayList<>();
            for (int x = minX - 2; x <= maxX + 1; x++) {
                for (int z = minZ - 2; z <= maxZ + 1; z++) {
                    order.add(new int[]{x, z});
                }
            }
        }
        for (int[] chunk : order) {
            provider.a(provider, chunk[0], chunk[1]);
        }
        byte[][] populated = dumpBlocks(world, minX, minZ, maxX, maxZ);
        byte[][] light = dumpLight(chunks, minX, minZ, maxX, maxZ);
        System.out.printf("Beta 1.7.3 generated %d chunks and decorated %d in %.1f s%n",
                chunks.size(), order.size(), (System.nanoTime() - start) / 1e9);

        try (DataOutputStream data = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out)))) {
            data.writeBytes("B173ORCL");
            data.writeInt(2);
            data.writeLong(seed);
            data.writeInt(minX);
            data.writeInt(minZ);
            data.writeInt(maxX);
            data.writeInt(maxZ);
            data.write(base[0]);
            data.write(base[1]);
            data.write(populated[0]);
            data.write(populated[1]);
            data.write(light[0]);
            data.write(light[1]);
            List<String> tiles = tileEntities(chunks, minX, minZ, maxX, maxZ);
            data.writeInt(tiles.size());
            for (String tile : tiles) {
                data.writeUTF(tile);
            }
            System.out.println("wrote " + out + " with " + tiles.size() + " tile entities");
        }
    }

    private static long key(int x, int z) {
        return (long) x << 32 ^ (z & 0xFFFFFFFFL);
    }

    private static byte[][] dumpBlocks(fd world, int minX, int minZ, int maxX, int maxZ) {
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

    private static byte[][] dumpLight(Map<Long, lm> chunks, int minX, int minZ, int maxX, int maxZ) {
        int width = (maxX - minX + 1) * 16;
        int depth = (maxZ - minZ + 1) * 16;
        byte[] sky = new byte[width * depth * 128];
        byte[] block = new byte[width * depth * 128];
        int i = 0;
        for (int x = minX * 16; x < (maxX + 1) * 16; x++) {
            for (int z = minZ * 16; z < (maxZ + 1) * 16; z++) {
                lm chunk = chunks.get(key(x >> 4, z >> 4));
                for (int y = 0; y < 128; y++) {
                    sky[i] = (byte) chunk.f.a(x & 15, y, z & 15);
                    block[i] = (byte) chunk.g.a(x & 15, y, z & 15);
                    i++;
                }
            }
        }
        return new byte[][]{sky, block};
    }

    private static List<String> tileEntities(Map<Long, lm> chunks, int minX, int minZ, int maxX, int maxZ)
            throws ReflectiveOperationException {
        List<String> lines = new ArrayList<>();
        Field damage = iz.class.getDeclaredField("d");
        damage.setAccessible(true);
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                lm chunk = chunks.get(key(cx, cz));
                for (Object o : chunk.l.values()) {
                    ow tile = (ow) o;
                    String where = tile.e + " " + tile.f + " " + tile.g;
                    if (tile instanceof js chest) {
                        StringBuilder line = new StringBuilder("chest " + where);
                        for (int slot = 0; slot < chest.a(); slot++) {
                            iz stack = chest.f_(slot);
                            if (stack != null) {
                                line.append(' ').append(slot).append(':').append(stack.c).append(':')
                                        .append(stack.a).append(':').append(damage.getInt(stack));
                            }
                        }
                        lines.add(line.toString());
                    } else if (tile instanceof cy spawner) {
                        lines.add("spawner " + where + " " + spawner.a());
                    } else {
                        lines.add("other " + where + " " + tile.getClass().getName());
                    }
                }
            }
        }
        return lines;
    }

    private static List<int[]> readOrder(File log) throws IOException {
        List<int[]> order = new ArrayList<>();
        Pattern line = Pattern.compile("b173 populate (-?\\d+) (-?\\d+)");
        for (String text : Files.readAllLines(log.toPath(), StandardCharsets.UTF_8)) {
            Matcher m = line.matcher(text);
            if (m.find()) {
                order.add(new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))});
            }
        }
        return order;
    }
}
