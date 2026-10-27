package dev.b173.core;

import dev.b173.core.noise.PerlinOctaves;

import java.util.Random;

public final class TerrainParityTest {
    private TerrainParityTest() {
    }

    public static void run(TestRunner t) {
        t.section("java.util.Random replica");
        for (long seed : new long[]{0L, 1L, -1L, 12345L, Long.MIN_VALUE, Long.MAX_VALUE, 25214903917L}) {
            Random reference = new Random(seed);
            BetaRandom ours = new BetaRandom(seed);
            boolean same = true;
            for (int i = 0; i < 2000 && same; i++) {
                switch (i % 7) {
                    case 0 -> same = reference.nextInt() == ours.nextInt();
                    case 1 -> same = reference.nextInt(1 + i) == ours.nextInt(1 + i);
                    case 2 -> same = reference.nextLong() == ours.nextLong();
                    case 3 -> same = reference.nextDouble() == ours.nextDouble();
                    case 4 -> same = reference.nextFloat() == ours.nextFloat();
                    case 5 -> same = reference.nextBoolean() == ours.nextBoolean();
                    default -> same = reference.nextInt(256) == ours.nextInt(256);
                }
            }
            t.check("seed " + seed + " stream matches java.util.Random", same);
        }

        t.section("Climate");
        climate(t, 0L, 0, 0, 0.91868674455220250D, 0.51627697992666610D);
        climate(t, 12345L, 100, -200, 0.98340677456747380D, 0.58284841235641620D);
        climate(t, -987654321L, 2048, 4096, 0.75811506814830190D, 0.54772363960284860D);
        climate(t, Long.MAX_VALUE, -31, -47, 0.86418849625249380D, 0.10264858839453617D);

        t.section("Perlin octaves");
        perlin(t, 12345L, -6480435348680049220L, 8886595889914116199L, 20.319491795248194D);
        perlin(t, -987654321L, 4367878008506452464L, 4222895526146720440L, -29.397958198440445D);

        t.section("Terrain, surface and caves");
        terrain(t, 0L, 0, 0, -5269162498297286196L);
        terrain(t, 0L, -1, -1, -1731346225582079475L);
        terrain(t, 0L, 1000, 1000, 6139623363433083740L);
        terrain(t, 1L, 0, 0, 1897419822800886124L);
        terrain(t, 1L, -32, 47, 9151602550538641201L);
        terrain(t, -1L, 0, 0, -4608660329486835876L);
        terrain(t, -1L, 511, -512, 2226014390480075141L);
        terrain(t, 12345L, 6, -13, -7372633547912830469L);
        terrain(t, 12345L, 0, 0, 8953584338840136443L);
        terrain(t, 12345L, -100, 200, -3701996463697429919L);
        terrain(t, -987654321L, -2, -3, 4919090602495585608L);
        terrain(t, -987654321L, 2048, -4096, 8240369298900689522L);
        terrain(t, Long.MAX_VALUE, 31, -47, 3162912773903310332L);
        terrain(t, Long.MIN_VALUE, -31, 47, 3970861668773491363L);
        terrain(t, 25214903917L, 300000, -300000, -8919915522389949498L);
        terrain(t, 8675309L, -1875, 2047, -5126588818450141840L);

        BetaChunk chunk = new BetaTerrain(12345L).generate(0, 0);
        boolean topIsAir = true;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                topIsAir &= chunk.id(x, 127, z) == BetaBlocks.AIR;
            }
        }
        t.check("the top density layer fades to air", topIsAir);
    }

    private static void climate(TestRunner t, long seed, int x, int z, double temperature, double humidity) {
        BetaClimate.Point p = new BetaClimate(seed).at(x, z);
        t.check("seed " + seed + " climate at " + x + "," + z,
                p.temperature() == temperature && p.humidity() == humidity,
                "got " + p.temperature() + ", " + p.humidity());
    }

    private static void perlin(TestRunner t, long seed, long fullHash, long flatHash, double point) {
        double[] full = new PerlinOctaves(new BetaRandom(seed), 16)
                .generate(null, 4, 0, -8, 5, 17, 5, 684.412D, 684.412D, 684.412D);
        t.check("seed " + seed + " 3D lattice", hash(full) == fullHash, "got " + hash(full));
        double[] flat = new PerlinOctaves(new BetaRandom(seed), 10)
                .generate(null, 4, 0, -8, 5, 1, 5, 1.121D, 1.0D, 1.121D);
        t.check("seed " + seed + " flattened 2D lattice", hash(flat) == flatHash, "got " + hash(flat));
        double sample = new PerlinOctaves(new BetaRandom(seed), 8).samplePoint(32.0D, -48.0D);
        t.check("seed " + seed + " point sample", sample == point, "got " + sample);
    }

    private static void terrain(TestRunner t, long seed, int chunkX, int chunkZ, long expected) {
        long hash = terrainHash(new BetaTerrain(seed).generate(chunkX, chunkZ));
        t.check("seed " + seed + " chunk " + chunkX + "," + chunkZ, hash == expected, "got " + hash);
    }

    static long terrainHash(BetaChunk chunk) {
        long hash = 0xcbf29ce484222325L;
        for (byte b : chunk.blocks) {
            int ordinal = switch (b & 255) {
                case BetaBlocks.AIR -> 0;
                case BetaBlocks.STONE -> 1;
                case BetaBlocks.WATER, BetaBlocks.FLOWING_WATER -> 2;
                case BetaBlocks.LAVA, BetaBlocks.FLOWING_LAVA -> 3;
                case BetaBlocks.ICE -> 4;
                case BetaBlocks.BEDROCK -> 5;
                case BetaBlocks.GRASS -> 6;
                case BetaBlocks.DIRT -> 7;
                case BetaBlocks.SAND -> 8;
                case BetaBlocks.SANDSTONE -> 9;
                case BetaBlocks.GRAVEL -> 10;
                default -> throw new IllegalStateException("unexpected terrain block " + (b & 255));
            };
            hash ^= ordinal;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    static long hash(double[] values) {
        long hash = 0xcbf29ce484222325L;
        for (double value : values) {
            hash ^= Double.doubleToRawLongBits(value);
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
