package dev.b173.core;

public final class BetaBlocks {
    public static final int AIR = 0;
    public static final int STONE = 1;
    public static final int GRASS = 2;
    public static final int DIRT = 3;
    public static final int COBBLESTONE = 4;
    public static final int PLANKS = 5;
    public static final int SAPLING = 6;
    public static final int BEDROCK = 7;
    public static final int FLOWING_WATER = 8;
    public static final int WATER = 9;
    public static final int FLOWING_LAVA = 10;
    public static final int LAVA = 11;
    public static final int SAND = 12;
    public static final int GRAVEL = 13;
    public static final int GOLD_ORE = 14;
    public static final int IRON_ORE = 15;
    public static final int COAL_ORE = 16;
    public static final int LOG = 17;
    public static final int LEAVES = 18;
    public static final int GLASS = 20;
    public static final int LAPIS_ORE = 21;
    public static final int SANDSTONE = 24;
    public static final int TALL_GRASS = 31;
    public static final int DEAD_BUSH = 32;
    public static final int DANDELION = 37;
    public static final int ROSE = 38;
    public static final int BROWN_MUSHROOM = 39;
    public static final int RED_MUSHROOM = 40;
    public static final int MOSSY_COBBLESTONE = 48;
    public static final int OBSIDIAN = 49;
    public static final int TORCH = 50;
    public static final int FIRE = 51;
    public static final int SPAWNER = 52;
    public static final int CHEST = 54;
    public static final int DIAMOND_ORE = 56;
    public static final int FARMLAND = 60;
    public static final int WOODEN_DOOR = 64;
    public static final int SIGN = 63;
    public static final int LADDER = 65;
    public static final int IRON_DOOR = 71;
    public static final int REDSTONE_ORE = 73;
    public static final int SNOW_LAYER = 78;
    public static final int ICE = 79;
    public static final int SNOW_BLOCK = 80;
    public static final int CACTUS = 81;
    public static final int CLAY = 82;
    public static final int REEDS = 83;
    public static final int PUMPKIN = 86;
    public static final int NETHERRACK = 87;
    public static final int SOUL_SAND = 88;
    public static final int GLOWSTONE = 89;
    public static final int PORTAL = 90;

    public static final int WOOD_OAK = 0;
    public static final int WOOD_SPRUCE = 1;
    public static final int WOOD_BIRCH = 2;

    public static final int TALL_GRASS_SHRUB = 0;
    public static final int TALL_GRASS_GRASS = 1;
    public static final int TALL_GRASS_FERN = 2;

    private static final int M_AIR = 0;
    private static final int M_SOLID = 1;
    private static final int M_LIQUID = 2;
    private static final int M_NON_SOLID = 3;

    private static final int[] LIGHT_OPACITY = new int[256];
    private static final boolean[] OPAQUE_CUBE = new boolean[256];
    private static final byte[] MATERIAL_KIND = new byte[256];
    private static final boolean[] KNOWN = new boolean[256];

    static {
        int[] cubes = {STONE, GRASS, DIRT, COBBLESTONE, PLANKS, BEDROCK, SAND, GRAVEL, GOLD_ORE, IRON_ORE,
                COAL_ORE, LOG, 19, LAPIS_ORE, 22, 23, SANDSTONE, 25, 35, 41, 42, 45, 46, 47, MOSSY_COBBLESTONE,
                OBSIDIAN, CHEST, DIAMOND_ORE, 57, 58, 61, 62, REDSTONE_ORE, 74, SNOW_BLOCK, CLAY, 84,
                PUMPKIN, NETHERRACK, SOUL_SAND, GLOWSTONE, 91, 95};
        for (int id : cubes) {
            define(id, 255, true, M_SOLID);
        }
        define(AIR, 0, false, M_AIR);
        define(FLOWING_WATER, 3, false, M_LIQUID);
        define(WATER, 3, false, M_LIQUID);
        define(FLOWING_LAVA, 255, false, M_LIQUID);
        define(LAVA, 255, false, M_LIQUID);
        define(LEAVES, 1, true, M_SOLID);
        define(ICE, 3, false, M_SOLID);
        define(GLASS, 0, false, M_SOLID);
        define(CACTUS, 0, false, M_SOLID);
        define(SPAWNER, 0, false, M_SOLID);
        define(FARMLAND, 255, false, M_SOLID);
        define(WOODEN_DOOR, 0, false, M_SOLID);
        define(IRON_DOOR, 0, false, M_SOLID);
        define(SIGN, 0, false, M_SOLID);
        define(LADDER, 0, false, M_NON_SOLID);
        define(43, 255, false, M_SOLID);
        define(44, 255, false, M_SOLID);
        define(53, 255, false, M_SOLID);
        define(67, 255, false, M_SOLID);
        define(85, 0, false, M_SOLID);
        define(92, 0, false, M_SOLID);
        define(96, 0, false, M_SOLID);
        define(30, 1, false, M_SOLID);
        for (int id : new int[]{26, 29, 33, 34, 36, 68, 70, 72}) {
            define(id, 0, false, M_SOLID);
        }
        int[] soft = {SAPLING, TALL_GRASS, DEAD_BUSH, DANDELION, ROSE, BROWN_MUSHROOM, RED_MUSHROOM, TORCH,
                FIRE, 55, 59, 66, 27, 28, 69, 75, 76, 77, SNOW_LAYER, REEDS, PORTAL, 93, 94};
        for (int id : soft) {
            define(id, 0, false, M_NON_SOLID);
        }
    }

    private BetaBlocks() {
    }

    private static void define(int id, int opacity, boolean opaqueCube, int kind) {
        LIGHT_OPACITY[id] = opacity;
        OPAQUE_CUBE[id] = opaqueCube;
        MATERIAL_KIND[id] = (byte) kind;
        KNOWN[id] = true;
    }

    public static boolean isKnown(int id) {
        return KNOWN[id & 255];
    }

    public static int lightOpacity(int id) {
        return LIGHT_OPACITY[id & 255];
    }

    private static final java.util.BitSet NORMAL_CUBE = java.util.BitSet.valueOf(new long[0]);

    static {
        for (int id : new int[] {1, 2, 3, 4, 5, 7, 12, 13, 14, 15, 16, 17, 19, 21, 22, 23, 24, 25, 35, 41, 42, 43, 45,
                47, 48, 49, 52, 54, 56, 57, 58, 61, 62, 73, 74, 80, 82, 84, 86, 87, 88, 89, 91, 95}) {
            NORMAL_CUBE.set(id);
        }
    }

    public static boolean isNormalCube(int id) {
        return NORMAL_CUBE.get(id & 255);
    }

    public static boolean isOpaqueCube(int id) {
        return OPAQUE_CUBE[id & 255];
    }

    public static boolean isSolid(int id) {
        return MATERIAL_KIND[id & 255] == M_SOLID;
    }

    public static boolean isLiquid(int id) {
        return MATERIAL_KIND[id & 255] == M_LIQUID;
    }

    public static boolean isWater(int id) {
        return id == WATER || id == FLOWING_WATER;
    }

    public static boolean isLava(int id) {
        return id == LAVA || id == FLOWING_LAVA;
    }

    public static boolean sameFluid(int a, int b) {
        return isWater(a) ? isWater(b) : isLava(a) && isLava(b);
    }

    public static boolean isPlantSoil(int id) {
        return id == GRASS || id == DIRT || id == FARMLAND;
    }

    public static boolean isFlowerLike(int id) {
        return id == SAPLING || id == TALL_GRASS || id == DEAD_BUSH || id == DANDELION || id == ROSE
                || id == BROWN_MUSHROOM || id == RED_MUSHROOM;
    }

    public static String name(int id) {
        return switch (id) {
            case AIR -> "air";
            case STONE -> "stone";
            case GRASS -> "grass";
            case DIRT -> "dirt";
            case COBBLESTONE -> "cobblestone";
            case BEDROCK -> "bedrock";
            case FLOWING_WATER -> "flowing_water";
            case WATER -> "water";
            case FLOWING_LAVA -> "flowing_lava";
            case LAVA -> "lava";
            case SAND -> "sand";
            case GRAVEL -> "gravel";
            case GOLD_ORE -> "gold_ore";
            case IRON_ORE -> "iron_ore";
            case COAL_ORE -> "coal_ore";
            case LOG -> "log";
            case LEAVES -> "leaves";
            case LAPIS_ORE -> "lapis_ore";
            case SANDSTONE -> "sandstone";
            case TALL_GRASS -> "tall_grass";
            case DEAD_BUSH -> "dead_bush";
            case DANDELION -> "dandelion";
            case ROSE -> "rose";
            case BROWN_MUSHROOM -> "brown_mushroom";
            case RED_MUSHROOM -> "red_mushroom";
            case MOSSY_COBBLESTONE -> "mossy_cobblestone";
            case OBSIDIAN -> "obsidian";
            case SPAWNER -> "spawner";
            case CHEST -> "chest";
            case DIAMOND_ORE -> "diamond_ore";
            case REDSTONE_ORE -> "redstone_ore";
            case SNOW_LAYER -> "snow_layer";
            case ICE -> "ice";
            case CACTUS -> "cactus";
            case CLAY -> "clay";
            case REEDS -> "reeds";
            case PUMPKIN -> "pumpkin";
            case FIRE -> "fire";
            case NETHERRACK -> "netherrack";
            case SOUL_SAND -> "soul_sand";
            case GLOWSTONE -> "glowstone";
            case PORTAL -> "portal";
            default -> "block_" + id;
        };
    }
}
