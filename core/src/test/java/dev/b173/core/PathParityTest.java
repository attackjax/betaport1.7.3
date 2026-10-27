package dev.b173.core;

import java.util.Random;

public final class PathParityTest {
    private static final float[][] SIZES = {{0.6F, 1.8F}, {1.4F, 0.9F}, {0.9F, 0.9F}, {0.6F, 1.6F}, {0.3F, 0.7F},
            {4.0F, 4.0F}};
    private static final long BETA_HASH = 0xdd4ea0a691487427L;

    private PathParityTest() {
    }

    private static int[][][][] world(Random random) {
        int[][][] ids = new int[32][16][32];
        int[][][] metas = new int[32][16][32];
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                int ground = 3 + random.nextInt(2) + (random.nextInt(6) == 0 ? random.nextInt(4) : 0);
                for (int y = 0; y < 16; y++) {
                    if (y <= ground) {
                        ids[x][y][z] = random.nextInt(25) == 0 ? 0 : 1;
                    } else if (y <= ground + 2) {
                        int roll = random.nextInt(100);
                        ids[x][y][z] = roll < 12 ? 1 : roll < 16 ? 9 : roll < 18 ? 11 : roll < 21 ? 64 : roll < 22 ? 71
                                : roll < 24 ? 85 : 0;
                        metas[x][y][z] = random.nextInt(16);
                    }
                }
            }
        }
        return new int[][][][] {ids, metas};
    }

    public static void run(TestRunner t) {
        t.section("Paths against Beta's PathFinder");
        Random random = new Random(26);
        long hash = 0xcbf29ce484222325L;
        int found = 0;
        for (int s = 0; s < 3000; s++) {
            int[][][][] w = world(random);
            int[][][] ids = w[0];
            int[][][] metas = w[1];
            BetaPathFinder.World world = new BetaPathFinder.World() {
                private boolean in(int x, int y, int z) {
                    return x >= -16 && x < 16 && y >= 56 && y < 72 && z >= -16 && z < 16;
                }

                public int id(int x, int y, int z) {
                    return in(x, y, z) ? ids[x + 16][y - 56][z + 16] : 0;
                }

                public int meta(int x, int y, int z) {
                    return in(x, y, z) ? metas[x + 16][y - 56][z + 16] : 0;
                }
            };
            float[] size = SIZES[random.nextInt(SIZES.length)];
            double sx = random.nextInt(24) - 12 + random.nextDouble();
            double sy = 56 + 4 + random.nextInt(5);
            double sz = random.nextInt(24) - 12 + random.nextDouble();
            int tx = (int) Math.floor(sx) + random.nextInt(25) - 12;
            int ty = (int) sy + random.nextInt(7) - 3;
            int tz = (int) Math.floor(sz) + random.nextInt(25) - 12;
            float distance = random.nextBoolean() ? 16.0F : 10.0F;
            boolean toEntity = random.nextBoolean();
            double minX = sx - size[0] / 2.0F;
            double minZ = sz - size[0] / 2.0F;
            int[][] path;
            if (toEntity) {
                double ox = tx + random.nextDouble();
                double oz = tz + random.nextDouble();
                path = new BetaPathFinder(world).find(minX, sy, minZ, size[0], size[1], ox, ty, oz, distance);
            } else {
                path = new BetaPathFinder(world).find(minX, sy, minZ, size[0], size[1], (double) ((float) tx + 0.5F),
                        (double) ((float) ty + 0.5F), (double) ((float) tz + 0.5F), distance);
            }
            if (path == null) {
                hash = mix(hash, -1);
                continue;
            }
            found++;
            hash = mix(hash, path.length);
            for (int[] p : path) {
                hash = mix(hash, p[0]);
                hash = mix(hash, p[1]);
                hash = mix(hash, p[2]);
            }
        }
        t.check("3000 searches (" + found + " paths) give Beta's path hash", hash == BETA_HASH,
                String.format("0x%016x, Beta 0x%016x", hash, BETA_HASH));
    }

    private static long mix(long hash, int value) {
        for (int shift = 0; shift < 32; shift += 8) {
            hash = (hash ^ (value >>> shift & 255)) * 0x100000001b3L;
        }
        return hash;
    }
}
