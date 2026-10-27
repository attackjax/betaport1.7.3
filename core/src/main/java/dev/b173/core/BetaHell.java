package dev.b173.core;

import dev.b173.core.noise.PerlinOctaves;

public final class BetaHell implements BetaChunkSource {
    public static final int LAVA_LEVEL = 32;
    private static final int SURFACE_LEVEL = 64;
    private static final int CELLS = 4;
    private static final int LATTICE_XZ = CELLS + 1;
    private static final int LATTICE_Y = 17;
    private static final double SCALE_XZ = 684.412D;
    private static final double SCALE_Y = 2053.236D;

    private static final double[] FALLOFF = new double[LATTICE_Y];

    static {
        for (int y = 0; y < LATTICE_Y; y++) {
            FALLOFF[y] = Math.cos((double) y * 3.141592653589793D * 6.0D / (double) LATTICE_Y) * 2.0D;
            double edge = y;
            if (y > LATTICE_Y / 2) {
                edge = LATTICE_Y - 1 - y;
            }
            if (edge < 4.0D) {
                edge = 4.0D - edge;
                FALLOFF[y] -= edge * edge * edge * 10.0D;
            }
        }
    }

    private final long seed;
    private final PerlinOctaves lowNoise;
    private final PerlinOctaves highNoise;
    private final PerlinOctaves selectorNoise;
    private final PerlinOctaves sandNoise;
    private final PerlinOctaves depthNoise;
    private final BetaHellCaves caves;

    public BetaHell(long seed) {
        this.seed = seed;
        BetaRandom random = new BetaRandom(seed);
        lowNoise = new PerlinOctaves(random, 16);
        highNoise = new PerlinOctaves(random, 16);
        selectorNoise = new PerlinOctaves(random, 8);
        sandNoise = new PerlinOctaves(random, 4);
        depthNoise = new PerlinOctaves(random, 4);
        caves = new BetaHellCaves(seed);
    }

    @Override
    public long seed() {
        return seed;
    }

    @Override
    public BetaChunk generate(int chunkX, int chunkZ) {
        BetaChunk chunk = new BetaChunk(chunkX, chunkZ);
        generate(chunk);
        return chunk;
    }

    public void generate(BetaChunk chunk) {
        int chunkX = chunk.chunkX;
        int chunkZ = chunk.chunkZ;
        BetaRandom random = new BetaRandom(chunkSeed(chunkX, chunkZ));
        fillTerrain(chunk.blocks, density(chunkX * CELLS, chunkZ * CELLS));
        replaceSurface(chunkX, chunkZ, chunk.blocks, random);
        caves.carve(chunkX, chunkZ, chunk.blocks);
    }

    private static long chunkSeed(int chunkX, int chunkZ) {
        return (long) chunkX * 341873128712L + (long) chunkZ * 132897987541L;
    }

    public static BetaRandom randomAfter(int chunkX, int chunkZ) {
        BetaRandom random = new BetaRandom(chunkSeed(chunkX, chunkZ));
        for (int column = 0; column < 256; column++) {
            random.nextDouble();
            random.nextDouble();
            random.nextDouble();
            for (int y = 127; y >= 0; y--) {
                if (y >= 127 - random.nextInt(5)) {
                    continue;
                }
                random.nextInt(5);
            }
        }
        return random;
    }

    double[] density(int noiseX, int noiseZ) {
        double[] selector = selectorNoise.generate(null, noiseX, 0.0D, noiseZ, LATTICE_XZ, LATTICE_Y, LATTICE_XZ,
                SCALE_XZ / 80.0D, SCALE_Y / 60.0D, SCALE_XZ / 80.0D);
        double[] low = lowNoise.generate(null, noiseX, 0.0D, noiseZ, LATTICE_XZ, LATTICE_Y, LATTICE_XZ,
                SCALE_XZ, SCALE_Y, SCALE_XZ);
        double[] high = highNoise.generate(null, noiseX, 0.0D, noiseZ, LATTICE_XZ, LATTICE_Y, LATTICE_XZ,
                SCALE_XZ, SCALE_Y, SCALE_XZ);
        double[] out = new double[LATTICE_XZ * LATTICE_Y * LATTICE_XZ];
        for (int index = 0; index < out.length; index++) {
            int ly = index % LATTICE_Y;
            double a = low[index] / 512.0D;
            double b = high[index] / 512.0D;
            double s = (selector[index] / 10.0D + 1.0D) / 2.0D;
            double value;
            if (s < 0.0D) {
                value = a;
            } else if (s > 1.0D) {
                value = b;
            } else {
                value = a + (b - a) * s;
            }
            value -= FALLOFF[ly];
            if (ly > LATTICE_Y - 4) {
                double fade = (double) ((float) (ly - (LATTICE_Y - 4)) / 3.0F);
                value = value * (1.0D - fade) + -10.0D * fade;
            }
            out[index] = value;
        }
        return out;
    }

    private static void fillTerrain(byte[] blocks, double[] density) {
        for (int cx = 0; cx < CELLS; cx++) {
            for (int cz = 0; cz < CELLS; cz++) {
                for (int cy = 0; cy < 16; cy++) {
                    int i00 = (cx * LATTICE_XZ + cz) * LATTICE_Y + cy;
                    int i01 = (cx * LATTICE_XZ + cz + 1) * LATTICE_Y + cy;
                    int i10 = ((cx + 1) * LATTICE_XZ + cz) * LATTICE_Y + cy;
                    int i11 = ((cx + 1) * LATTICE_XZ + cz + 1) * LATTICE_Y + cy;
                    double d00 = density[i00];
                    double d01 = density[i01];
                    double d10 = density[i10];
                    double d11 = density[i11];
                    double step00 = (density[i00 + 1] - d00) * 0.125D;
                    double step01 = (density[i01 + 1] - d01) * 0.125D;
                    double step10 = (density[i10 + 1] - d10) * 0.125D;
                    double step11 = (density[i11 + 1] - d11) * 0.125D;
                    for (int sy = 0; sy < 8; sy++) {
                        int y = cy * 8 + sy;
                        double edgeA = d00;
                        double edgeB = d01;
                        double stepA = (d10 - d00) * 0.25D;
                        double stepB = (d11 - d01) * 0.25D;
                        for (int sx = 0; sx < 4; sx++) {
                            int index = (sx + cx * 4) << 11 | (cz * 4) << 7 | y;
                            double value = edgeA;
                            double stepZ = (edgeB - edgeA) * 0.25D;
                            for (int sz = 0; sz < 4; sz++) {
                                int id = BetaBlocks.AIR;
                                if (y < LAVA_LEVEL) {
                                    id = BetaBlocks.LAVA;
                                }
                                if (value > 0.0D) {
                                    id = BetaBlocks.NETHERRACK;
                                }
                                blocks[index] = (byte) id;
                                index += 128;
                                value += stepZ;
                            }
                            edgeA += stepA;
                            edgeB += stepB;
                        }
                        d00 += step00;
                        d01 += step01;
                        d10 += step10;
                        d11 += step11;
                    }
                }
            }
        }
    }

    private void replaceSurface(int chunkX, int chunkZ, byte[] blocks, BetaRandom random) {
        double scale = 0.03125D;
        double[] soul = sandNoise.generate(null, chunkX * 16, chunkZ * 16, 0.0D, 16, 16, 1, scale, scale, 1.0D);
        double[] gravel = sandNoise.generate(null, chunkX * 16, 109.0134D, chunkZ * 16, 16, 1, 16,
                scale, 1.0D, scale);
        double[] depth = depthNoise.generate(null, chunkX * 16, chunkZ * 16, 0.0D, 16, 16, 1,
                scale * 2.0D, scale * 2.0D, scale * 2.0D);
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int column = z + x * 16;
                boolean soulSand = soul[column] + random.nextDouble() * 0.2D > 0.0D;
                boolean gravelly = gravel[column] + random.nextDouble() * 0.2D > 0.0D;
                int thickness = (int) (depth[column] / 3.0D + 3.0D + random.nextDouble() * 0.25D);
                int run = -1;
                int top = BetaBlocks.NETHERRACK;
                int filler = BetaBlocks.NETHERRACK;
                int base = x << 11 | z << 7;
                for (int y = 127; y >= 0; y--) {
                    int index = base | y;
                    if (y >= 127 - random.nextInt(5)) {
                        blocks[index] = (byte) BetaBlocks.BEDROCK;
                        continue;
                    }
                    if (y <= random.nextInt(5)) {
                        blocks[index] = (byte) BetaBlocks.BEDROCK;
                        continue;
                    }
                    int current = blocks[index];
                    if (current == BetaBlocks.AIR) {
                        run = -1;
                        continue;
                    }
                    if (current != BetaBlocks.NETHERRACK) {
                        continue;
                    }
                    if (run == -1) {
                        if (thickness <= 0) {
                            top = BetaBlocks.AIR;
                            filler = BetaBlocks.NETHERRACK;
                        } else if (y >= SURFACE_LEVEL - 4 && y <= SURFACE_LEVEL + 1) {
                            top = BetaBlocks.NETHERRACK;
                            filler = BetaBlocks.NETHERRACK;
                            if (gravelly) {
                                top = BetaBlocks.GRAVEL;
                                filler = BetaBlocks.NETHERRACK;
                            }
                            if (soulSand) {
                                top = BetaBlocks.SOUL_SAND;
                                filler = BetaBlocks.SOUL_SAND;
                            }
                        }
                        if (y < SURFACE_LEVEL && top == BetaBlocks.AIR) {
                            top = BetaBlocks.LAVA;
                        }
                        run = thickness;
                        blocks[index] = (byte) (y >= SURFACE_LEVEL - 1 ? top : filler);
                        continue;
                    }
                    if (run > 0) {
                        run--;
                        blocks[index] = (byte) filler;
                    }
                }
            }
        }
    }
}
