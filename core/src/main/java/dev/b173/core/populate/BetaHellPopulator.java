package dev.b173.core.populate;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaHell;
import dev.b173.core.BetaRandom;
import dev.b173.core.world.BetaWorld;

public final class BetaHellPopulator {
    private BetaHellPopulator() {
    }

    public static void populate(BetaWorld world, int chunkX, int chunkZ) {
        populate(world, chunkX, chunkZ, BetaHell.randomAfter(chunkX + 1, chunkZ + 1));
    }

    public static void populate(BetaWorld world, int chunkX, int chunkZ, BetaRandom random) {
        world.fallInstantly = true;
        try {
            decorate(world, chunkX, chunkZ, random);
        } finally {
            world.fallInstantly = false;
        }
    }

    private static void decorate(BetaWorld world, int chunkX, int chunkZ, BetaRandom random) {
        int x0 = chunkX * 16;
        int z0 = chunkZ * 16;
        for (int i = 0; i < 8; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(120) + 4;
            int z = z0 + random.nextInt(16) + 8;
            lavaSpring(world, random, x, y, z);
        }
        int fires = random.nextInt(random.nextInt(10) + 1) + 1;
        for (int i = 0; i < fires; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(120) + 4;
            int z = z0 + random.nextInt(16) + 8;
            fire(world, random, x, y, z);
        }
        int clusters = random.nextInt(random.nextInt(10) + 1);
        for (int i = 0; i < clusters; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(120) + 4;
            int z = z0 + random.nextInt(16) + 8;
            glowstone(world, random, x, y, z);
        }
        for (int i = 0; i < 10; i++) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            glowstone(world, random, x, y, z);
        }
        if (random.nextInt(1) == 0) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.flowers(world, random, x, y, z, BetaBlocks.BROWN_MUSHROOM);
        }
        if (random.nextInt(1) == 0) {
            int x = x0 + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = z0 + random.nextInt(16) + 8;
            BetaFeatures.flowers(world, random, x, y, z, BetaBlocks.RED_MUSHROOM);
        }
    }

    static void lavaSpring(BetaWorld world, BetaRandom random, int x, int y, int z) {
        if (world.getBlockId(x, y + 1, z) != BetaBlocks.NETHERRACK) {
            return;
        }
        int centre = world.getBlockId(x, y, z);
        if (centre != 0 && centre != BetaBlocks.NETHERRACK) {
            return;
        }
        int rock = 0;
        if (world.getBlockId(x - 1, y, z) == BetaBlocks.NETHERRACK) {
            rock++;
        }
        if (world.getBlockId(x + 1, y, z) == BetaBlocks.NETHERRACK) {
            rock++;
        }
        if (world.getBlockId(x, y, z - 1) == BetaBlocks.NETHERRACK) {
            rock++;
        }
        if (world.getBlockId(x, y, z + 1) == BetaBlocks.NETHERRACK) {
            rock++;
        }
        if (world.getBlockId(x, y - 1, z) == BetaBlocks.NETHERRACK) {
            rock++;
        }
        int air = 0;
        if (world.isAirBlock(x - 1, y, z)) {
            air++;
        }
        if (world.isAirBlock(x + 1, y, z)) {
            air++;
        }
        if (world.isAirBlock(x, y, z - 1)) {
            air++;
        }
        if (world.isAirBlock(x, y, z + 1)) {
            air++;
        }
        if (world.isAirBlock(x, y - 1, z)) {
            air++;
        }
        if (rock == 4 && air == 1) {
            world.setBlockWithNotify(x, y, z, BetaBlocks.FLOWING_LAVA);
            world.scheduledUpdatesAreImmediate = true;
            world.updateTick(x, y, z, BetaBlocks.FLOWING_LAVA, random);
            world.scheduledUpdatesAreImmediate = false;
        }
    }

    static void fire(BetaWorld world, BetaRandom random, int x, int y, int z) {
        for (int i = 0; i < 64; i++) {
            int px = x + random.nextInt(8) - random.nextInt(8);
            int py = y + random.nextInt(4) - random.nextInt(4);
            int pz = z + random.nextInt(8) - random.nextInt(8);
            if (world.isAirBlock(px, py, pz) && world.getBlockId(px, py - 1, pz) == BetaBlocks.NETHERRACK) {
                world.setBlockWithNotify(px, py, pz, BetaBlocks.FIRE);
            }
        }
    }

    static void glowstone(BetaWorld world, BetaRandom random, int x, int y, int z) {
        if (!world.isAirBlock(x, y, z)) {
            return;
        }
        if (world.getBlockId(x, y + 1, z) != BetaBlocks.NETHERRACK) {
            return;
        }
        world.setBlockWithNotify(x, y, z, BetaBlocks.GLOWSTONE);
        for (int i = 0; i < 1500; i++) {
            int px = x + random.nextInt(8) - random.nextInt(8);
            int py = y - random.nextInt(12);
            int pz = z + random.nextInt(8) - random.nextInt(8);
            if (world.getBlockId(px, py, pz) != 0) {
                continue;
            }
            int touching = 0;
            if (world.getBlockId(px - 1, py, pz) == BetaBlocks.GLOWSTONE) {
                touching++;
            }
            if (world.getBlockId(px + 1, py, pz) == BetaBlocks.GLOWSTONE) {
                touching++;
            }
            if (world.getBlockId(px, py - 1, pz) == BetaBlocks.GLOWSTONE) {
                touching++;
            }
            if (world.getBlockId(px, py + 1, pz) == BetaBlocks.GLOWSTONE) {
                touching++;
            }
            if (world.getBlockId(px, py, pz - 1) == BetaBlocks.GLOWSTONE) {
                touching++;
            }
            if (world.getBlockId(px, py, pz + 1) == BetaBlocks.GLOWSTONE) {
                touching++;
            }
            if (touching == 1) {
                world.setBlockWithNotify(px, py, pz, BetaBlocks.GLOWSTONE);
            }
        }
    }
}
