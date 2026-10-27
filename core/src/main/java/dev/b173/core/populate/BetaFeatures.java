package dev.b173.core.populate;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaMath;
import dev.b173.core.BetaRandom;
import dev.b173.core.world.BetaPlants;
import dev.b173.core.world.BetaWorld;

public final class BetaFeatures {
    private static final float PI = (float) Math.PI;

    private BetaFeatures() {
    }

    public static boolean lake(BetaWorld world, BetaRandom random, int x, int y, int z, int liquid) {
        x -= 8;
        z -= 8;
        while (y > 0 && world.isAirBlock(x, y, z)) {
            y--;
        }
        y -= 4;
        boolean[] shape = new boolean[2048];
        int blobs = random.nextInt(4) + 4;
        for (int i = 0; i < blobs; i++) {
            double sizeX = random.nextDouble() * 6.0D + 3.0D;
            double sizeY = random.nextDouble() * 4.0D + 2.0D;
            double sizeZ = random.nextDouble() * 6.0D + 3.0D;
            double cx = random.nextDouble() * (16.0D - sizeX - 2.0D) + 1.0D + sizeX / 2.0D;
            double cy = random.nextDouble() * (8.0D - sizeY - 4.0D) + 2.0D + sizeY / 2.0D;
            double cz = random.nextDouble() * (16.0D - sizeZ - 2.0D) + 1.0D + sizeZ / 2.0D;
            for (int px = 1; px < 15; px++) {
                for (int pz = 1; pz < 15; pz++) {
                    for (int py = 1; py < 7; py++) {
                        double dx = ((double) px - cx) / (sizeX / 2.0D);
                        double dy = ((double) py - cy) / (sizeY / 2.0D);
                        double dz = ((double) pz - cz) / (sizeZ / 2.0D);
                        if (dx * dx + dy * dy + dz * dz < 1.0D) {
                            shape[(px * 16 + pz) * 8 + py] = true;
                        }
                    }
                }
            }
        }
        for (int px = 0; px < 16; px++) {
            for (int pz = 0; pz < 16; pz++) {
                for (int py = 0; py < 8; py++) {
                    if (!lakeEdge(shape, px, pz, py)) {
                        continue;
                    }
                    int id = world.getBlockId(x + px, y + py, z + pz);
                    if (py >= 4 && BetaBlocks.isLiquid(id)) {
                        return false;
                    }
                    if (py < 4 && !BetaBlocks.isSolid(id) && id != liquid) {
                        return false;
                    }
                }
            }
        }
        for (int px = 0; px < 16; px++) {
            for (int pz = 0; pz < 16; pz++) {
                for (int py = 0; py < 8; py++) {
                    if (shape[(px * 16 + pz) * 8 + py]) {
                        world.setBlock(x + px, y + py, z + pz, py < 4 ? liquid : 0);
                    }
                }
            }
        }
        for (int px = 0; px < 16; px++) {
            for (int pz = 0; pz < 16; pz++) {
                for (int py = 4; py < 8; py++) {
                    if (shape[(px * 16 + pz) * 8 + py]
                            && world.getBlockId(x + px, y + py - 1, z + pz) == BetaBlocks.DIRT
                            && world.skyLight(x + px, y + py, z + pz) > 0) {
                        world.setBlock(x + px, y + py - 1, z + pz, BetaBlocks.GRASS);
                    }
                }
            }
        }
        if (BetaBlocks.isLava(liquid)) {
            for (int px = 0; px < 16; px++) {
                for (int pz = 0; pz < 16; pz++) {
                    for (int py = 0; py < 8; py++) {
                        if (lakeEdge(shape, px, pz, py) && (py < 4 || random.nextInt(2) != 0)
                                && world.isSolid(x + px, y + py, z + pz)) {
                            world.setBlock(x + px, y + py, z + pz, BetaBlocks.STONE);
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean lakeEdge(boolean[] shape, int x, int z, int y) {
        if (shape[(x * 16 + z) * 8 + y]) {
            return false;
        }
        return x < 15 && shape[((x + 1) * 16 + z) * 8 + y]
                || x > 0 && shape[((x - 1) * 16 + z) * 8 + y]
                || z < 15 && shape[(x * 16 + z + 1) * 8 + y]
                || z > 0 && shape[(x * 16 + z - 1) * 8 + y]
                || y < 7 && shape[(x * 16 + z) * 8 + y + 1]
                || y > 0 && shape[(x * 16 + z) * 8 + y - 1];
    }

    public static boolean dungeon(BetaWorld world, BetaRandom random, int x, int y, int z) {
        int height = 3;
        int rx = random.nextInt(2) + 2;
        int rz = random.nextInt(2) + 2;
        int openings = 0;
        for (int px = x - rx - 1; px <= x + rx + 1; px++) {
            for (int py = y - 1; py <= y + height + 1; py++) {
                for (int pz = z - rz - 1; pz <= z + rz + 1; pz++) {
                    boolean solid = world.isSolid(px, py, pz);
                    if (py == y - 1 && !solid) {
                        return false;
                    }
                    if (py == y + height + 1 && !solid) {
                        return false;
                    }
                    if ((px == x - rx - 1 || px == x + rx + 1 || pz == z - rz - 1 || pz == z + rz + 1)
                            && py == y && world.isAirBlock(px, py, pz) && world.isAirBlock(px, py + 1, pz)) {
                        openings++;
                    }
                }
            }
        }
        if (openings < 1 || openings > 5) {
            return false;
        }
        for (int px = x - rx - 1; px <= x + rx + 1; px++) {
            for (int py = y + height; py >= y - 1; py--) {
                for (int pz = z - rz - 1; pz <= z + rz + 1; pz++) {
                    boolean wall = px == x - rx - 1 || py == y - 1 || pz == z - rz - 1
                            || px == x + rx + 1 || py == y + height + 1 || pz == z + rz + 1;
                    if (!wall) {
                        world.setBlockWithNotify(px, py, pz, 0);
                        continue;
                    }
                    if (py >= 0 && !world.isSolid(px, py - 1, pz)) {
                        world.setBlockWithNotify(px, py, pz, 0);
                        continue;
                    }
                    if (!world.isSolid(px, py, pz)) {
                        continue;
                    }
                    if (py == y - 1 && random.nextInt(4) != 0) {
                        world.setBlockWithNotify(px, py, pz, BetaBlocks.MOSSY_COBBLESTONE);
                    } else {
                        world.setBlockWithNotify(px, py, pz, BetaBlocks.COBBLESTONE);
                    }
                }
            }
        }
        for (int chest = 0; chest < 2; chest++) {
            for (int attempt = 0; attempt < 3; attempt++) {
                int px = x + random.nextInt(rx * 2 + 1) - rx;
                int pz = z + random.nextInt(rz * 2 + 1) - rz;
                if (!world.isAirBlock(px, y, pz)) {
                    continue;
                }
                int walls = 0;
                if (world.isSolid(px - 1, y, pz)) {
                    walls++;
                }
                if (world.isSolid(px + 1, y, pz)) {
                    walls++;
                }
                if (world.isSolid(px, y, pz - 1)) {
                    walls++;
                }
                if (world.isSolid(px, y, pz + 1)) {
                    walls++;
                }
                if (walls != 1) {
                    continue;
                }
                world.setBlockWithNotify(px, y, pz, BetaBlocks.CHEST);
                for (int i = 0; i < 8; i++) {
                    BetaWorld.ChestItem item = dungeonLoot(random);
                    if (item != null) {
                        world.addChestItem(px, y, pz, item);
                    }
                }
                break;
            }
        }
        world.setBlockWithNotify(x, y, z, BetaBlocks.SPAWNER);
        world.setSpawner(x, y, z, dungeonMob(random));
        return true;
    }

    private static BetaWorld.ChestItem dungeonLoot(BetaRandom random) {
        int roll = random.nextInt(11);
        String item = null;
        int count = 1;
        int damage = 0;
        switch (roll) {
            case 0 -> item = "minecraft:saddle";
            case 1 -> {
                item = "minecraft:iron_ingot";
                count = random.nextInt(4) + 1;
            }
            case 2 -> item = "minecraft:bread";
            case 3 -> {
                item = "minecraft:wheat";
                count = random.nextInt(4) + 1;
            }
            case 4 -> {
                item = "minecraft:gunpowder";
                count = random.nextInt(4) + 1;
            }
            case 5 -> {
                item = "minecraft:string";
                count = random.nextInt(4) + 1;
            }
            case 6 -> item = "minecraft:bucket";
            case 7 -> {
                if (random.nextInt(100) == 0) {
                    item = "minecraft:golden_apple";
                }
            }
            case 8 -> {
                if (random.nextInt(2) == 0) {
                    item = "minecraft:redstone";
                    count = random.nextInt(4) + 1;
                }
            }
            case 9 -> {
                if (random.nextInt(10) == 0) {
                    item = random.nextInt(2) == 0 ? "minecraft:music_disc_13" : "minecraft:music_disc_cat";
                }
            }
            case 10 -> {
                item = "minecraft:cocoa_beans";
                damage = 3;
            }
            default -> {
            }
        }
        if (item == null) {
            return null;
        }
        return new BetaWorld.ChestItem(random.nextInt(27), item, count, damage);
    }

    private static String dungeonMob(BetaRandom random) {
        return switch (random.nextInt(4)) {
            case 0 -> "minecraft:skeleton";
            case 3 -> "minecraft:spider";
            default -> "minecraft:zombie";
        };
    }

    public static boolean clay(BetaWorld world, BetaRandom random, int x, int y, int z, int size) {
        if (!world.isWater(x, y, z)) {
            return false;
        }
        float angle = random.nextFloat() * PI;
        double x0 = (float) (x + 8) + BetaMath.sin(angle) * (float) size / 8.0F;
        double x1 = (float) (x + 8) - BetaMath.sin(angle) * (float) size / 8.0F;
        double z0 = (float) (z + 8) + BetaMath.cos(angle) * (float) size / 8.0F;
        double z1 = (float) (z + 8) - BetaMath.cos(angle) * (float) size / 8.0F;
        double y0 = y + random.nextInt(3) + 2;
        double y1 = y + random.nextInt(3) + 2;
        for (int i = 0; i <= size; i++) {
            double cx = x0 + (x1 - x0) * (double) i / (double) size;
            double cy = y0 + (y1 - y0) * (double) i / (double) size;
            double cz = z0 + (z1 - z0) * (double) i / (double) size;
            double spread = random.nextDouble() * (double) size / 16.0D;
            double width = (double) (BetaMath.sin((float) i * PI / (float) size) + 1.0F) * spread + 1.0D;
            double tall = (double) (BetaMath.sin((float) i * PI / (float) size) + 1.0F) * spread + 1.0D;
            int minX = BetaMath.floor(cx - width / 2.0D);
            int maxX = BetaMath.floor(cx + width / 2.0D);
            int minY = BetaMath.floor(cy - tall / 2.0D);
            int maxY = BetaMath.floor(cy + tall / 2.0D);
            int minZ = BetaMath.floor(cz - width / 2.0D);
            int maxZ = BetaMath.floor(cz + width / 2.0D);
            for (int px = minX; px <= maxX; px++) {
                for (int py = minY; py <= maxY; py++) {
                    for (int pz = minZ; pz <= maxZ; pz++) {
                        double dx = ((double) px + 0.5D - cx) / (width / 2.0D);
                        double dy = ((double) py + 0.5D - cy) / (tall / 2.0D);
                        double dz = ((double) pz + 0.5D - cz) / (width / 2.0D);
                        if (dx * dx + dy * dy + dz * dz < 1.0D
                                && world.getBlockId(px, py, pz) == BetaBlocks.SAND) {
                            world.setBlock(px, py, pz, BetaBlocks.CLAY);
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean minable(BetaWorld world, BetaRandom random, int x, int y, int z, int block, int size) {
        float angle = random.nextFloat() * PI;
        double x0 = (float) (x + 8) + BetaMath.sin(angle) * (float) size / 8.0F;
        double x1 = (float) (x + 8) - BetaMath.sin(angle) * (float) size / 8.0F;
        double z0 = (float) (z + 8) + BetaMath.cos(angle) * (float) size / 8.0F;
        double z1 = (float) (z + 8) - BetaMath.cos(angle) * (float) size / 8.0F;
        double y0 = y + random.nextInt(3) + VEIN_Y_OFFSET;
        double y1 = y + random.nextInt(3) + VEIN_Y_OFFSET;
        for (int i = 0; i <= size; i++) {
            double cx = x0 + (x1 - x0) * (double) i / (double) size;
            double cy = y0 + (y1 - y0) * (double) i / (double) size;
            double cz = z0 + (z1 - z0) * (double) i / (double) size;
            double spread = random.nextDouble() * (double) size / 16.0D;
            double width = (double) (BetaMath.sin((float) i * PI / (float) size) + 1.0F) * spread + 1.0D;
            double tall = (double) (BetaMath.sin((float) i * PI / (float) size) + 1.0F) * spread + 1.0D;
            int minX = BetaMath.floor(cx - width / 2.0D);
            int minY = BetaMath.floor(cy - tall / 2.0D);
            int minZ = BetaMath.floor(cz - width / 2.0D);
            int maxX = BetaMath.floor(cx + width / 2.0D);
            int maxY = BetaMath.floor(cy + tall / 2.0D);
            int maxZ = BetaMath.floor(cz + width / 2.0D);
            for (int px = minX; px <= maxX; px++) {
                double dx = ((double) px + 0.5D - cx) / (width / 2.0D);
                if (dx * dx >= 1.0D) {
                    continue;
                }
                for (int py = minY; py <= maxY; py++) {
                    double dy = ((double) py + 0.5D - cy) / (tall / 2.0D);
                    if (dx * dx + dy * dy >= 1.0D) {
                        continue;
                    }
                    for (int pz = minZ; pz <= maxZ; pz++) {
                        double dz = ((double) pz + 0.5D - cz) / (width / 2.0D);
                        if (dx * dx + dy * dy + dz * dz < 1.0D
                                && world.getBlockId(px, py, pz) == BetaBlocks.STONE) {
                            world.setBlock(px, py, pz, block);
                        }
                    }
                }
            }
        }
        return true;
    }

    static int VEIN_Y_OFFSET = 2;

    public static boolean broadleaf(BetaWorld world, BetaRandom random, int x, int y, int z, boolean birch) {
        int height = random.nextInt(3) + (birch ? 5 : 4);
        if (y < 1 || y + height + 1 > 128) {
            return false;
        }
        for (int py = y; py <= y + 1 + height; py++) {
            int r = 1;
            if (py == y) {
                r = 0;
            }
            if (py >= y + 1 + height - 2) {
                r = 2;
            }
            for (int px = x - r; px <= x + r; px++) {
                for (int pz = z - r; pz <= z + r; pz++) {
                    if (py >= 0 && py < 128) {
                        int id = world.getBlockId(px, py, pz);
                        if (id != 0 && id != BetaBlocks.LEAVES) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        int ground = world.getBlockId(x, y - 1, z);
        if (ground != BetaBlocks.GRASS && ground != BetaBlocks.DIRT || y >= 128 - height - 1) {
            return false;
        }
        world.setBlock(x, y - 1, z, BetaBlocks.DIRT);
        int wood = birch ? BetaBlocks.WOOD_BIRCH : BetaBlocks.WOOD_OAK;
        for (int py = y - 3 + height; py <= y + height; py++) {
            int layer = py - (y + height);
            int r = 1 - layer / 2;
            for (int px = x - r; px <= x + r; px++) {
                int dx = px - x;
                for (int pz = z - r; pz <= z + r; pz++) {
                    int dz = pz - z;
                    if ((Math.abs(dx) != r || Math.abs(dz) != r || random.nextInt(2) != 0 && layer != 0)
                            && !BetaBlocks.isOpaqueCube(world.getBlockId(px, py, pz))) {
                        if (birch) {
                            world.setBlockAndMetadata(px, py, pz, BetaBlocks.LEAVES, wood);
                        } else {
                            world.setBlock(px, py, pz, BetaBlocks.LEAVES);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < height; i++) {
            int id = world.getBlockId(x, y + i, z);
            if (id == 0 || id == BetaBlocks.LEAVES) {
                if (birch) {
                    world.setBlockAndMetadata(x, y + i, z, BetaBlocks.LOG, wood);
                } else {
                    world.setBlock(x, y + i, z, BetaBlocks.LOG);
                }
            }
        }
        return true;
    }

    public static boolean spruceTall(BetaWorld world, BetaRandom random, int x, int y, int z) {
        int height = random.nextInt(5) + 7;
        int bare = height - random.nextInt(2) - 3;
        int crown = height - bare;
        int maxRadius = 1 + random.nextInt(crown + 1);
        if (!coniferSpace(world, x, y, z, height, bare, maxRadius)) {
            return false;
        }
        int ground = world.getBlockId(x, y - 1, z);
        if (ground != BetaBlocks.GRASS && ground != BetaBlocks.DIRT || y >= 128 - height - 1) {
            return false;
        }
        world.setBlock(x, y - 1, z, BetaBlocks.DIRT);
        int r = 0;
        for (int py = y + height; py >= y + bare; py--) {
            coniferLayer(world, x, py, z, r);
            if (r >= 1 && py == y + bare + 1) {
                r--;
                continue;
            }
            if (r < maxRadius) {
                r++;
            }
        }
        for (int i = 0; i < height - 1; i++) {
            int id = world.getBlockId(x, y + i, z);
            if (id == 0 || id == BetaBlocks.LEAVES) {
                world.setBlockAndMetadata(x, y + i, z, BetaBlocks.LOG, BetaBlocks.WOOD_SPRUCE);
            }
        }
        return true;
    }

    public static boolean spruce(BetaWorld world, BetaRandom random, int x, int y, int z) {
        int height = random.nextInt(4) + 6;
        int bare = 1 + random.nextInt(2);
        int crown = height - bare;
        int maxRadius = 2 + random.nextInt(2);
        if (!coniferSpace(world, x, y, z, height, bare, maxRadius)) {
            return false;
        }
        int ground = world.getBlockId(x, y - 1, z);
        if (ground != BetaBlocks.GRASS && ground != BetaBlocks.DIRT || y >= 128 - height - 1) {
            return false;
        }
        world.setBlock(x, y - 1, z, BetaBlocks.DIRT);
        int r = random.nextInt(2);
        int limit = 1;
        boolean started = false;
        for (int layer = 0; layer <= crown; layer++) {
            coniferLayer(world, x, y + height - layer, z, r);
            if (r >= limit) {
                r = started ? 1 : 0;
                started = true;
                if (++limit > maxRadius) {
                    limit = maxRadius;
                }
            } else {
                r++;
            }
        }
        int shortening = random.nextInt(3);
        for (int i = 0; i < height - shortening; i++) {
            int id = world.getBlockId(x, y + i, z);
            if (id == 0 || id == BetaBlocks.LEAVES) {
                world.setBlockAndMetadata(x, y + i, z, BetaBlocks.LOG, BetaBlocks.WOOD_SPRUCE);
            }
        }
        return true;
    }

    private static boolean coniferSpace(BetaWorld world, int x, int y, int z, int height, int bare, int radius) {
        if (y < 1 || y + height + 1 > 128) {
            return false;
        }
        for (int py = y; py <= y + 1 + height; py++) {
            int r = py - y < bare ? 0 : radius;
            for (int px = x - r; px <= x + r; px++) {
                for (int pz = z - r; pz <= z + r; pz++) {
                    if (py >= 0 && py < 128) {
                        int id = world.getBlockId(px, py, pz);
                        if (id != 0 && id != BetaBlocks.LEAVES) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void coniferLayer(BetaWorld world, int x, int y, int z, int r) {
        for (int px = x - r; px <= x + r; px++) {
            int dx = px - x;
            for (int pz = z - r; pz <= z + r; pz++) {
                int dz = pz - z;
                if ((Math.abs(dx) != r || Math.abs(dz) != r || r <= 0)
                        && !BetaBlocks.isOpaqueCube(world.getBlockId(px, y, pz))) {
                    world.setBlockAndMetadata(px, y, pz, BetaBlocks.LEAVES, BetaBlocks.WOOD_SPRUCE);
                }
            }
        }
    }

    public static void flowers(BetaWorld world, BetaRandom random, int x, int y, int z, int plant) {
        for (int i = 0; i < 64; i++) {
            int px = x + random.nextInt(8) - random.nextInt(8);
            int py = y + random.nextInt(4) - random.nextInt(4);
            int pz = z + random.nextInt(8) - random.nextInt(8);
            if (world.isAirBlock(px, py, pz) && BetaPlants.canBlockStay(world, plant, px, py, pz)) {
                world.setBlock(px, py, pz, plant);
            }
        }
    }

    public static void tallGrass(BetaWorld world, BetaRandom random, int x, int y, int z, int meta) {
        int id;
        while (((id = world.getBlockId(x, y, z)) == 0 || id == BetaBlocks.LEAVES) && y > 0) {
            y--;
        }
        for (int i = 0; i < 128; i++) {
            int px = x + random.nextInt(8) - random.nextInt(8);
            int py = y + random.nextInt(4) - random.nextInt(4);
            int pz = z + random.nextInt(8) - random.nextInt(8);
            if (world.isAirBlock(px, py, pz) && BetaPlants.canBlockStay(world, BetaBlocks.TALL_GRASS, px, py, pz)) {
                world.setBlockAndMetadata(px, py, pz, BetaBlocks.TALL_GRASS, meta);
            }
        }
    }

    public static void deadBush(BetaWorld world, BetaRandom random, int x, int y, int z) {
        int id;
        while (((id = world.getBlockId(x, y, z)) == 0 || id == BetaBlocks.LEAVES) && y > 0) {
            y--;
        }
        for (int i = 0; i < 4; i++) {
            int px = x + random.nextInt(8) - random.nextInt(8);
            int py = y + random.nextInt(4) - random.nextInt(4);
            int pz = z + random.nextInt(8) - random.nextInt(8);
            if (world.isAirBlock(px, py, pz) && BetaPlants.canBlockStay(world, BetaBlocks.DEAD_BUSH, px, py, pz)) {
                world.setBlock(px, py, pz, BetaBlocks.DEAD_BUSH);
            }
        }
    }

    public static void reeds(BetaWorld world, BetaRandom random, int x, int y, int z) {
        for (int i = 0; i < 20; i++) {
            int px = x + random.nextInt(4) - random.nextInt(4);
            int pz = z + random.nextInt(4) - random.nextInt(4);
            if (!world.isAirBlock(px, y, pz)) {
                continue;
            }
            if (!world.isWater(px - 1, y - 1, pz) && !world.isWater(px + 1, y - 1, pz)
                    && !world.isWater(px, y - 1, pz - 1) && !world.isWater(px, y - 1, pz + 1)) {
                continue;
            }
            int height = 2 + random.nextInt(random.nextInt(3) + 1);
            for (int h = 0; h < height; h++) {
                if (BetaPlants.reedCanStay(world, px, y + h, pz)) {
                    world.setBlock(px, y + h, pz, BetaBlocks.REEDS);
                }
            }
        }
    }

    public static void pumpkins(BetaWorld world, BetaRandom random, int x, int y, int z) {
        for (int i = 0; i < 64; i++) {
            int px = x + random.nextInt(8) - random.nextInt(8);
            int py = y + random.nextInt(4) - random.nextInt(4);
            int pz = z + random.nextInt(8) - random.nextInt(8);
            if (world.isAirBlock(px, py, pz) && world.getBlockId(px, py - 1, pz) == BetaBlocks.GRASS) {
                world.setBlockAndMetadata(px, py, pz, BetaBlocks.PUMPKIN, random.nextInt(4));
            }
        }
    }

    public static void cactus(BetaWorld world, BetaRandom random, int x, int y, int z) {
        for (int i = 0; i < 10; i++) {
            int px = x + random.nextInt(8) - random.nextInt(8);
            int py = y + random.nextInt(4) - random.nextInt(4);
            int pz = z + random.nextInt(8) - random.nextInt(8);
            if (!world.isAirBlock(px, py, pz)) {
                continue;
            }
            int height = 1 + random.nextInt(random.nextInt(3) + 1);
            for (int h = 0; h < height; h++) {
                if (BetaPlants.cactusCanStay(world, px, py + h, pz)) {
                    world.setBlock(px, py + h, pz, BetaBlocks.CACTUS);
                }
            }
        }
    }

    public static boolean spring(BetaWorld world, BetaRandom random, int x, int y, int z, int liquid) {
        if (world.getBlockId(x, y + 1, z) != BetaBlocks.STONE) {
            return false;
        }
        if (world.getBlockId(x, y - 1, z) != BetaBlocks.STONE) {
            return false;
        }
        int centre = world.getBlockId(x, y, z);
        if (centre != 0 && centre != BetaBlocks.STONE) {
            return false;
        }
        int stone = 0;
        if (world.getBlockId(x - 1, y, z) == BetaBlocks.STONE) {
            stone++;
        }
        if (world.getBlockId(x + 1, y, z) == BetaBlocks.STONE) {
            stone++;
        }
        if (world.getBlockId(x, y, z - 1) == BetaBlocks.STONE) {
            stone++;
        }
        if (world.getBlockId(x, y, z + 1) == BetaBlocks.STONE) {
            stone++;
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
        if (stone == 3 && air == 1) {
            world.setBlockWithNotify(x, y, z, liquid);
            world.scheduledUpdatesAreImmediate = true;
            world.updateTick(x, y, z, liquid, random);
            world.scheduledUpdatesAreImmediate = false;
        }
        return true;
    }
}
