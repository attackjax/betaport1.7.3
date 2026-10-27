package dev.b173.core.populate;

import dev.b173.core.BetaBiome;
import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaRandom;
import dev.b173.core.BetaTerrain;
import dev.b173.core.world.BetaWorld;

public final class BetaPopulator {
    private final long seed;
    private final BetaTerrain terrain;
    private final long xMultiplier;
    private final long zMultiplier;

    public BetaPopulator(BetaTerrain terrain) {
        this.terrain = terrain;
        this.seed = terrain.seed();
        BetaRandom random = new BetaRandom(seed);
        xMultiplier = random.nextLong() / 2L * 2L + 1L;
        zMultiplier = random.nextLong() / 2L * 2L + 1L;
    }

    public void populate(BetaWorld world, int chunkX, int chunkZ) {
        world.fallInstantly = true;
        try {
            decorate(world, chunkX, chunkZ);
        } finally {
            world.fallInstantly = false;
        }
    }

    private void decorate(BetaWorld world, int chunkX, int chunkZ) {
        int x0 = chunkX * 16;
        int z0 = chunkZ * 16;
        BetaBiome biome = terrain.climate().biomeAt(x0 + 16, z0 + 16);
        BetaRandom random = new BetaRandom((long) chunkX * xMultiplier + (long) chunkZ * zMultiplier ^ seed);

        if (random.nextInt(4) == 0) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.lake(world, random, x, y, z, BetaBlocks.WATER);
        }
        if (random.nextInt(8) == 0) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(random.nextInt(120) + 8);
            int z = z0 + random.nextInt(16) + 8;
            if (y < 64 || random.nextInt(10) == 0) {
                BetaFeatures.lake(world, random, x, y, z, BetaBlocks.LAVA);
            }
        }
        for (int i = 0; i < 8; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.dungeon(world, random, x, y, z);
        }
        for (int i = 0; i < 10; i++) {
            int x = x0 + random.nextInt(16);
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16);
            BetaFeatures.clay(world, random, x, y, z, 32);
        }
        veins(world, random, x0, z0, 20, 128, BetaBlocks.DIRT, 32);
        veins(world, random, x0, z0, 10, 128, BetaBlocks.GRAVEL, 32);
        veins(world, random, x0, z0, 20, 128, BetaBlocks.COAL_ORE, 16);
        veins(world, random, x0, z0, 20, 64, BetaBlocks.IRON_ORE, 8);
        veins(world, random, x0, z0, 2, 32, BetaBlocks.GOLD_ORE, 8);
        veins(world, random, x0, z0, 8, 16, BetaBlocks.REDSTONE_ORE, 7);
        veins(world, random, x0, z0, 1, 16, BetaBlocks.DIAMOND_ORE, 7);
        {
            int x = x0 + random.nextInt(16);
            int y = random.nextInt(16) + random.nextInt(16);
            int z = z0 + random.nextInt(16);
            BetaFeatures.minable(world, random, x, y, z, BetaBlocks.LAPIS_ORE, 6);
        }

        int density = (int) ((terrain.treeNoise(x0, z0) / 8.0D + random.nextDouble() * 4.0D + 4.0D) / 3.0D);
        int trees = 0;
        if (random.nextInt(10) == 0) {
            trees++;
        }
        if (biome == BetaBiome.FOREST) {
            trees += density + 5;
        }
        if (biome == BetaBiome.RAINFOREST) {
            trees += density + 5;
        }
        if (biome == BetaBiome.SEASONAL_FOREST) {
            trees += density + 2;
        }
        if (biome == BetaBiome.TAIGA) {
            trees += density + 5;
        }
        if (biome == BetaBiome.DESERT) {
            trees -= 20;
        }
        if (biome == BetaBiome.TUNDRA) {
            trees -= 20;
        }
        if (biome == BetaBiome.PLAINS) {
            trees -= 20;
        }
        for (int i = 0; i < trees; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int z = z0 + random.nextInt(16) + 8;
            Tree tree = pickTree(biome, random);
            int y = world.getHeightValue(x, z);
            switch (tree) {
                case OAK -> BetaFeatures.broadleaf(world, random, x, y, z, false);
                case BIRCH -> BetaFeatures.broadleaf(world, random, x, y, z, true);
                case BIG_OAK -> BetaBigTree.generate(world, random, x, y, z);
                case SPRUCE_TALL -> BetaFeatures.spruceTall(world, random, x, y, z);
                case SPRUCE -> BetaFeatures.spruce(world, random, x, y, z);
            }
        }

        int dandelions = 0;
        if (biome == BetaBiome.FOREST) {
            dandelions = 2;
        }
        if (biome == BetaBiome.SEASONAL_FOREST) {
            dandelions = 4;
        }
        if (biome == BetaBiome.TAIGA) {
            dandelions = 2;
        }
        if (biome == BetaBiome.PLAINS) {
            dandelions = 3;
        }
        for (int i = 0; i < dandelions; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.flowers(world, random, x, y, z, BetaBlocks.DANDELION);
        }

        int grass = 0;
        if (biome == BetaBiome.FOREST) {
            grass = 2;
        }
        if (biome == BetaBiome.RAINFOREST) {
            grass = 10;
        }
        if (biome == BetaBiome.SEASONAL_FOREST) {
            grass = 2;
        }
        if (biome == BetaBiome.TAIGA) {
            grass = 1;
        }
        if (biome == BetaBiome.PLAINS) {
            grass = 10;
        }
        for (int i = 0; i < grass; i++) {
            int meta = BetaBlocks.TALL_GRASS_GRASS;
            if (biome == BetaBiome.RAINFOREST && random.nextInt(3) != 0) {
                meta = BetaBlocks.TALL_GRASS_FERN;
            }
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.tallGrass(world, random, x, y, z, meta);
        }

        int deadBushes = biome == BetaBiome.DESERT ? 2 : 0;
        for (int i = 0; i < deadBushes; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.deadBush(world, random, x, y, z);
        }

        if (random.nextInt(2) == 0) {
            scatter(world, random, x0, z0, BetaBlocks.ROSE);
        }
        if (random.nextInt(4) == 0) {
            scatter(world, random, x0, z0, BetaBlocks.BROWN_MUSHROOM);
        }
        if (random.nextInt(8) == 0) {
            scatter(world, random, x0, z0, BetaBlocks.RED_MUSHROOM);
        }
        for (int i = 0; i < 10; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.reeds(world, random, x, y, z);
        }
        if (random.nextInt(32) == 0) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.pumpkins(world, random, x, y, z);
        }
        int cacti = biome == BetaBiome.DESERT ? 10 : 0;
        for (int i = 0; i < cacti; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.cactus(world, random, x, y, z);
        }
        for (int i = 0; i < 50; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(random.nextInt(120) + 8);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.spring(world, random, x, y, z, BetaBlocks.FLOWING_WATER);
        }
        for (int i = 0; i < 20; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(random.nextInt(random.nextInt(112) + 8) + 8);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.spring(world, random, x, y, z, BetaBlocks.FLOWING_LAVA);
        }

        double[] temperatures = terrain.climate().temperatures(null, x0 + 8, z0 + 8, 16, 16);
        for (int x = x0 + 8; x < x0 + 24; x++) {
            for (int z = z0 + 8; z < z0 + 24; z++) {
                int y = world.findTopSolidBlock(x, z);
                double temperature = temperatures[(x - (x0 + 8)) * 16 + (z - (z0 + 8))]
                        - (double) (y - 64) / 64.0D * 0.3D;
                if (temperature < 0.5D && y > 0 && y < 128 && world.isAirBlock(x, y, z)) {
                    int below = world.getBlockId(x, y - 1, z);
                    if (BetaBlocks.isSolid(below) && below != BetaBlocks.ICE) {
                        world.setBlockWithNotify(x, y, z, BetaBlocks.SNOW_LAYER);
                    }
                }
            }
        }
    }

    private static void veins(BetaWorld world, BetaRandom random, int x0, int z0, int count, int maxY,
                              int block, int size) {
        for (int i = 0; i < count; i++) {
            int x = x0 + random.nextInt(16);
            int y = random.nextInt(maxY);
            int z = z0 + random.nextInt(16);
            BetaFeatures.minable(world, random, x, y, z, block, size);
        }
    }

    private static void scatter(BetaWorld world, BetaRandom random, int x0, int z0, int plant) {
        int x = x0 + random.nextInt(16) + 8;
        int y = random.nextInt(128);
        int z = z0 + random.nextInt(16) + 8;
        BetaFeatures.flowers(world, random, x, y, z, plant);
    }

    enum Tree { OAK, BIRCH, BIG_OAK, SPRUCE_TALL, SPRUCE }

    private static Tree pickTree(BetaBiome biome, BetaRandom random) {
        switch (biome) {
            case FOREST -> {
                if (random.nextInt(5) == 0) {
                    return Tree.BIRCH;
                }
                return random.nextInt(3) == 0 ? Tree.BIG_OAK : Tree.OAK;
            }
            case RAINFOREST -> {
                return random.nextInt(3) == 0 ? Tree.BIG_OAK : Tree.OAK;
            }
            case TAIGA -> {
                return random.nextInt(3) == 0 ? Tree.SPRUCE_TALL : Tree.SPRUCE;
            }
            default -> {
                return random.nextInt(10) == 0 ? Tree.BIG_OAK : Tree.OAK;
            }
        }
    }
}
