package dev.b173.core;

import dev.b173.core.noise.PerlinOctaves;

public final class BetaTerrain implements BetaChunkSource {
    public static final int SEA_LEVEL = 64;
    private static final int CELLS = 4;
    private static final int LATTICE_XZ = CELLS + 1;
    private static final int LATTICE_Y = 17;
    private static final double COORDINATE_SCALE = 684.412D;

    private final long seed;
    private final BetaClimate climate;
    private final PerlinOctaves lowNoise;
    private final PerlinOctaves highNoise;
    private final PerlinOctaves selectorNoise;
    private final PerlinOctaves beachNoise;
    private final PerlinOctaves surfaceNoise;
    private final PerlinOctaves continentalNoise;
    private final PerlinOctaves depthNoise;
    private final PerlinOctaves treeNoise;
    private final BetaCaves caves;

    public BetaTerrain(long seed) {
        this.seed = seed;
        this.climate = new BetaClimate(seed);
        BetaRandom random = new BetaRandom(seed);
        lowNoise = new PerlinOctaves(random, 16);
        highNoise = new PerlinOctaves(random, 16);
        selectorNoise = new PerlinOctaves(random, 8);
        beachNoise = new PerlinOctaves(random, 4);
        surfaceNoise = new PerlinOctaves(random, 4);
        continentalNoise = new PerlinOctaves(random, 10);
        depthNoise = new PerlinOctaves(random, 16);
        treeNoise = new PerlinOctaves(random, 8);
        caves = new BetaCaves(seed);
    }

    @Override
    public long seed() {
        return seed;
    }

    public BetaClimate climate() {
        return climate;
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
        BetaRandom random = new BetaRandom((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
        double[] temperature = new double[256];
        double[] humidity = new double[256];
        climate.sample(chunkX * 16, chunkZ * 16, 16, 16, temperature, humidity);
        double[] density = density(chunkX * CELLS, chunkZ * CELLS, temperature, humidity);
        fillTerrain(chunk.blocks, density, temperature);
        BetaBiome[] biomes = BetaClimate.biomes(null, temperature, humidity, 256);
        replaceSurface(chunkX, chunkZ, chunk.blocks, biomes, random);
        caves.carve(chunkX, chunkZ, chunk.blocks);
    }

    double[] density(int noiseX, int noiseZ, double[] temperature, double[] humidity) {
        double[] continental = continentalNoise.generate2d(null, noiseX, noiseZ, LATTICE_XZ, LATTICE_XZ, 1.121D, 1.121D);
        double[] depth = depthNoise.generate2d(null, noiseX, noiseZ, LATTICE_XZ, LATTICE_XZ, 200.0D, 200.0D);
        double[] selector = selectorNoise.generate(null, noiseX, 0.0D, noiseZ, LATTICE_XZ, LATTICE_Y, LATTICE_XZ,
                COORDINATE_SCALE / 80.0D, COORDINATE_SCALE / 160.0D, COORDINATE_SCALE / 80.0D);
        double[] low = lowNoise.generate(null, noiseX, 0.0D, noiseZ, LATTICE_XZ, LATTICE_Y, LATTICE_XZ,
                COORDINATE_SCALE, COORDINATE_SCALE, COORDINATE_SCALE);
        double[] high = highNoise.generate(null, noiseX, 0.0D, noiseZ, LATTICE_XZ, LATTICE_Y, LATTICE_XZ,
                COORDINATE_SCALE, COORDINATE_SCALE, COORDINATE_SCALE);
        double[] out = new double[LATTICE_XZ * LATTICE_Y * LATTICE_XZ];
        int index = 0;
        int column = 0;
        int spacing = 16 / LATTICE_XZ;
        for (int lx = 0; lx < LATTICE_XZ; lx++) {
            int sampleX = lx * spacing + spacing / 2;
            for (int lz = 0; lz < LATTICE_XZ; lz++) {
                int sampleZ = lz * spacing + spacing / 2;
                double t = temperature[sampleX * 16 + sampleZ];
                double h = humidity[sampleX * 16 + sampleZ] * t;
                double dryness = 1.0D - h;
                dryness *= dryness;
                dryness *= dryness;
                dryness = 1.0D - dryness;
                double scale = (continental[column] + 256.0D) / 512.0D;
                scale *= dryness;
                if (scale > 1.0D) {
                    scale = 1.0D;
                }
                double depthValue = depth[column] / 8000.0D;
                if (depthValue < 0.0D) {
                    depthValue = -depthValue * 0.3D;
                }
                depthValue = depthValue * 3.0D - 2.0D;
                if (depthValue < 0.0D) {
                    depthValue /= 2.0D;
                    if (depthValue < -1.0D) {
                        depthValue = -1.0D;
                    }
                    depthValue /= 1.4D;
                    depthValue /= 2.0D;
                    scale = 0.0D;
                } else {
                    if (depthValue > 1.0D) {
                        depthValue = 1.0D;
                    }
                    depthValue /= 8.0D;
                }
                if (scale < 0.0D) {
                    scale = 0.0D;
                }
                scale += 0.5D;
                depthValue = depthValue * (double) LATTICE_Y / 16.0D;
                double centre = (double) LATTICE_Y / 2.0D + depthValue * 4.0D;
                column++;
                for (int ly = 0; ly < LATTICE_Y; ly++) {
                    double falloff = ((double) ly - centre) * 12.0D / scale;
                    if (falloff < 0.0D) {
                        falloff *= 4.0D;
                    }
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
                    value -= falloff;
                    if (ly > LATTICE_Y - 4) {
                        double fade = (double) ((float) (ly - (LATTICE_Y - 4)) / 3.0F);
                        value = value * (1.0D - fade) + -10.0D * fade;
                    }
                    out[index++] = value;
                }
            }
        }
        return out;
    }

    private static void fillTerrain(byte[] blocks, double[] density, double[] temperature) {
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
                            int x = cx * 4 + sx;
                            int index = x << 11 | (cz * 4) << 7 | y;
                            double value = edgeA;
                            double stepZ = (edgeB - edgeA) * 0.25D;
                            for (int sz = 0; sz < 4; sz++) {
                                int id = BetaBlocks.AIR;
                                if (y < SEA_LEVEL) {
                                    double t = temperature[x * 16 + cz * 4 + sz];
                                    id = t < 0.5D && y >= SEA_LEVEL - 1 ? BetaBlocks.ICE : BetaBlocks.WATER;
                                }
                                if (value > 0.0D) {
                                    id = BetaBlocks.STONE;
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

    private void replaceSurface(int chunkX, int chunkZ, byte[] blocks, BetaBiome[] biomes, BetaRandom random) {
        double scale = 0.03125D;
        double[] sand = beachNoise.generate(null, chunkX * 16, chunkZ * 16, 0.0D, 16, 16, 1, scale, scale, 1.0D);
        double[] gravel = beachNoise.generate(null, chunkX * 16, 109.0134D, chunkZ * 16, 16, 1, 16, scale, 1.0D, scale);
        double[] stone = surfaceNoise.generate(null, chunkX * 16, chunkZ * 16, 0.0D, 16, 16, 1,
                scale * 2.0D, scale * 2.0D, scale * 2.0D);
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int column = z + x * 16;
                BetaBiome biome = biomes[column];
                boolean sandy = sand[column] + random.nextDouble() * 0.2D > 0.0D;
                boolean gravelly = gravel[column] + random.nextDouble() * 0.2D > 3.0D;
                int thickness = (int) (stone[column] / 3.0D + 3.0D + random.nextDouble() * 0.25D);
                int run = -1;
                int top = biome.topBlock();
                int filler = biome.fillerBlock();
                int base = x << 11 | z << 7;
                for (int y = 127; y >= 0; y--) {
                    int index = base | y;
                    if (y <= random.nextInt(5)) {
                        blocks[index] = (byte) BetaBlocks.BEDROCK;
                        continue;
                    }
                    int current = blocks[index];
                    if (current == BetaBlocks.AIR) {
                        run = -1;
                        continue;
                    }
                    if (current != BetaBlocks.STONE) {
                        continue;
                    }
                    if (run == -1) {
                        if (thickness <= 0) {
                            top = BetaBlocks.AIR;
                            filler = BetaBlocks.STONE;
                        } else if (y >= SEA_LEVEL - 4 && y <= SEA_LEVEL + 1) {
                            top = biome.topBlock();
                            filler = biome.fillerBlock();
                            if (gravelly) {
                                top = BetaBlocks.AIR;
                                filler = BetaBlocks.GRAVEL;
                            }
                            if (sandy) {
                                top = BetaBlocks.SAND;
                                filler = BetaBlocks.SAND;
                            }
                        }
                        if (y < SEA_LEVEL && top == BetaBlocks.AIR) {
                            top = BetaBlocks.WATER;
                        }
                        run = thickness;
                        blocks[index] = (byte) (y >= SEA_LEVEL - 1 ? top : filler);
                        continue;
                    }
                    if (run > 0) {
                        run--;
                        blocks[index] = (byte) filler;
                        if (run == 0 && filler == BetaBlocks.SAND) {
                            run = random.nextInt(4);
                            filler = BetaBlocks.SANDSTONE;
                        }
                    }
                }
            }
        }
    }

    public double treeNoise(int blockX, int blockZ) {
        return treeNoise.samplePoint((double) blockX * 0.5D, (double) blockZ * 0.5D);
    }
}
