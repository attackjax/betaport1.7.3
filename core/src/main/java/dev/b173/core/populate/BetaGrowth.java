package dev.b173.core.populate;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaRandom;
import dev.b173.core.world.BetaWorld;

public final class BetaGrowth {
    private BetaGrowth() {
    }

    public static boolean growTree(BetaWorld world, BetaRandom random, int x, int y, int z, int kind) {
        world.setBlock(x, y, z, 0);
        boolean grown;
        if (kind == BetaBlocks.WOOD_SPRUCE) {
            grown = BetaFeatures.spruce(world, random, x, y, z);
        } else if (kind == BetaBlocks.WOOD_BIRCH) {
            grown = BetaFeatures.broadleaf(world, random, x, y, z, true);
        } else if (random.nextInt(10) == 0) {
            grown = BetaBigTree.fromSapling(world, random, x, y, z);
        } else {
            grown = BetaFeatures.broadleaf(world, random, x, y, z, false);
        }
        if (!grown) {
            world.setBlockAndMetadata(x, y, z, BetaBlocks.SAPLING, kind);
        }
        return grown;
    }

    public static void boneMealGrass(BetaWorld world, BetaRandom random, int x, int y, int z) {
        tries:
        for (int i = 0; i < 128; i++) {
            int px = x;
            int py = y + 1;
            int pz = z;
            for (int step = 0; step < i / 16; step++) {
                px += random.nextInt(3) - 1;
                py += (random.nextInt(3) - 1) * random.nextInt(3) / 2;
                pz += random.nextInt(3) - 1;
                if (world.getBlockId(px, py - 1, pz) != BetaBlocks.GRASS
                        || BetaBlocks.isNormalCube(world.getBlockId(px, py, pz))) {
                    continue tries;
                }
            }
            if (world.getBlockId(px, py, pz) == 0) {
                if (random.nextInt(10) != 0) {
                    world.setBlockAndMetadata(px, py, pz, BetaBlocks.TALL_GRASS, BetaBlocks.TALL_GRASS_GRASS);
                } else if (random.nextInt(3) != 0) {
                    world.setBlock(px, py, pz, BetaBlocks.DANDELION);
                } else {
                    world.setBlock(px, py, pz, BetaBlocks.ROSE);
                }
            }
        }
    }
}
