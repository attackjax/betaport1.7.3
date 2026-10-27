package dev.b173.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BetaContent {
    public static final String[] LINES = {
            "block 1 Stone",
            "block 2 Grass",
            "block 3 Dirt",
            "block 4 Cobblestone",
            "block 5 Wooden Planks",
            "block 6 Sapling",
            "block 7 Bedrock",
            "block 8 Water",
            "block 9 Water",
            "block 10 Lava",
            "block 11 Lava",
            "block 12 Sand",
            "block 13 Gravel",
            "block 14 Gold Ore",
            "block 15 Iron Ore",
            "block 16 Coal Ore",
            "block 17 Wood",
            "block 18 Leaves",
            "block 19 Sponge",
            "block 20 Glass",
            "block 21 Lapis Lazuli Ore",
            "block 22 Lapis Lazuli Block",
            "block 23 Dispenser",
            "block 24 Sandstone",
            "block 25 Note Block",
            "block 26 Bed",
            "block 27 Powered Rail",
            "block 28 Detector Rail",
            "block 29 Sticky Piston",
            "block 30 Cobweb",
            "block 31 tile.tallgrass.name",
            "block 32 tile.deadbush.name",
            "block 33 Piston",
            "block 34 null.name",
            "block 35 Wool",
            "block 36 null.name",
            "block 37 Flower",
            "block 38 Rose",
            "block 39 Mushroom",
            "block 40 Mushroom",
            "block 41 Block of Gold",
            "block 42 Block of Iron",
            "block 43 tile.stoneSlab.name",
            "block 44 tile.stoneSlab.name",
            "block 45 Bricks",
            "block 46 TNT",
            "block 47 Bookshelf",
            "block 48 Moss Stone",
            "block 49 Obsidian",
            "block 50 Torch",
            "block 51 Fire",
            "block 52 Monster Spawner",
            "block 53 Wooden Stairs",
            "block 54 Chest",
            "block 55 Redstone Dust",
            "block 56 Diamond Ore",
            "block 57 Block of Diamond",
            "block 58 Crafting Table",
            "block 59 Crops",
            "block 60 Farmland",
            "block 61 Furnace",
            "block 62 Furnace",
            "block 63 Sign",
            "block 64 Wooden Door",
            "block 65 Ladder",
            "block 66 Rail",
            "block 67 Stone Stairs",
            "block 68 Sign",
            "block 69 Lever",
            "block 70 Pressure Plate",
            "block 71 Iron Door",
            "block 72 Pressure Plate",
            "block 73 Redstone Ore",
            "block 74 Redstone Ore",
            "block 75 Redstone Torch",
            "block 76 Redstone Torch",
            "block 77 Button",
            "block 78 Snow",
            "block 79 Ice",
            "block 80 Snow",
            "block 81 Cactus",
            "block 82 Clay",
            "block 83 Sugar cane",
            "block 84 Jukebox",
            "block 85 Fence",
            "block 86 Pumpkin",
            "block 87 Netherrack",
            "block 88 Soul Sand",
            "block 89 Glowstone",
            "block 90 Portal",
            "block 91 Jack 'o' Lantern",
            "block 92 Cake",
            "block 93 tile.diode.name",
            "block 94 tile.diode.name",
            "block 95 Locked chest",
            "block 96 Trapdoor",
            "item 256 item.shovelIron",
            "item 257 item.pickaxeIron",
            "item 258 item.hatchetIron",
            "item 259 item.flintAndSteel",
            "item 260 item.apple",
            "item 261 item.bow",
            "item 262 item.arrow",
            "item 263 item.coal",
            "item 264 item.emerald",
            "item 265 item.ingotIron",
            "item 266 item.ingotGold",
            "item 267 item.swordIron",
            "item 268 item.swordWood",
            "item 269 item.shovelWood",
            "item 270 item.pickaxeWood",
            "item 271 item.hatchetWood",
            "item 272 item.swordStone",
            "item 273 item.shovelStone",
            "item 274 item.pickaxeStone",
            "item 275 item.hatchetStone",
            "item 276 item.swordDiamond",
            "item 277 item.shovelDiamond",
            "item 278 item.pickaxeDiamond",
            "item 279 item.hatchetDiamond",
            "item 280 item.stick",
            "item 281 item.bowl",
            "item 282 item.mushroomStew",
            "item 283 item.swordGold",
            "item 284 item.shovelGold",
            "item 285 item.pickaxeGold",
            "item 286 item.hatchetGold",
            "item 287 item.string",
            "item 288 item.feather",
            "item 289 item.sulphur",
            "item 290 item.hoeWood",
            "item 291 item.hoeStone",
            "item 292 item.hoeIron",
            "item 293 item.hoeDiamond",
            "item 294 item.hoeGold",
            "item 295 item.seeds",
            "item 296 item.wheat",
            "item 297 item.bread",
            "item 298 item.helmetCloth",
            "item 299 item.chestplateCloth",
            "item 300 item.leggingsCloth",
            "item 301 item.bootsCloth",
            "item 302 item.helmetChain",
            "item 303 item.chestplateChain",
            "item 304 item.leggingsChain",
            "item 305 item.bootsChain",
            "item 306 item.helmetIron",
            "item 307 item.chestplateIron",
            "item 308 item.leggingsIron",
            "item 309 item.bootsIron",
            "item 310 item.helmetDiamond",
            "item 311 item.chestplateDiamond",
            "item 312 item.leggingsDiamond",
            "item 313 item.bootsDiamond",
            "item 314 item.helmetGold",
            "item 315 item.chestplateGold",
            "item 316 item.leggingsGold",
            "item 317 item.bootsGold",
            "item 318 item.flint",
            "item 319 item.porkchopRaw",
            "item 320 item.porkchopCooked",
            "item 321 item.painting",
            "item 322 item.appleGold",
            "item 323 item.sign",
            "item 324 item.doorWood",
            "item 325 item.bucket",
            "item 326 item.bucketWater",
            "item 327 item.bucketLava",
            "item 328 item.minecart",
            "item 329 item.saddle",
            "item 330 item.doorIron",
            "item 331 item.redstone",
            "item 332 item.snowball",
            "item 333 item.boat",
            "item 334 item.leather",
            "item 335 item.milk",
            "item 336 item.brick",
            "item 337 item.clay",
            "item 338 item.reeds",
            "item 339 item.paper",
            "item 340 item.book",
            "item 341 item.slimeball",
            "item 342 item.minecartChest",
            "item 343 item.minecartFurnace",
            "item 344 item.egg",
            "item 345 item.compass",
            "item 346 item.fishingRod",
            "item 347 item.clock",
            "item 348 item.yellowDust",
            "item 349 item.fishRaw",
            "item 350 item.fishCooked",
            "item 351 item.dyePowder.black",
            "item 352 item.bone",
            "item 353 item.sugar",
            "item 354 item.cake",
            "item 355 item.bed",
            "item 356 item.diode",
            "item 357 item.cookie",
            "item 358 item.map",
            "item 359 item.shears",
            "item 2256 item.record",
            "item 2257 item.record",
            "entity 1 Item",
            "entity 9 Painting",
            "entity 10 Arrow",
            "entity 11 Snowball",
            "entity 20 PrimedTnt",
            "entity 21 FallingSand",
            "entity 40 Minecart",
            "entity 41 Boat",
            "entity 48 Mob",
            "entity 49 Monster",
            "entity 50 Creeper",
            "entity 51 Skeleton",
            "entity 52 Spider",
            "entity 53 Giant",
            "entity 54 Zombie",
            "entity 55 Slime",
            "entity 56 Ghast",
            "entity 57 PigZombie",
            "entity 90 Pig",
            "entity 91 Sheep",
            "entity 92 Cow",
            "entity 93 Chicken",
            "entity 94 Squid",
            "entity 95 Wolf",
            "opaque 1 2 3 4 5 7 12 13 14 15 16 17 18 19 21 22 23 24 25 35 41 42 45 46 47 48 49 54 56 57 58 61 62 73 74 80 82 84 86 87 88 89 91 95",
            "normal 1 2 3 4 5 7 12 13 14 15 16 17 19 21 22 23 24 25 35 41 42 43 45 47 48 49 52 54 56 57 58 61 62 73 74 80 82 84 86 87 88 89 91 95"
    };

    public static final String[] BLOCKS = {
            "0 air cave_air void_air",
            "1 stone", "2 grass_block", "3 dirt", "4 cobblestone", "5 oak_planks",
            "6 oak_sapling spruce_sapling birch_sapling", "7 bedrock", "8 water", "9 water", "10 lava",
            "11 lava", "12 sand", "13 gravel", "14 gold_ore", "15 iron_ore", "16 coal_ore",
            "17 oak_log spruce_log birch_log", "18 oak_leaves spruce_leaves birch_leaves", "19 sponge",
            "20 glass", "21 lapis_ore", "22 lapis_block", "23 dispenser", "24 sandstone", "25 note_block",
            "26 red_bed", "27 powered_rail", "28 detector_rail", "29 sticky_piston", "30 cobweb",
            "31 short_grass fern dead_bush", "32 dead_bush", "33 piston", "34 piston_head",
            "35 white_wool orange_wool magenta_wool light_blue_wool yellow_wool lime_wool pink_wool gray_wool"
                    + " light_gray_wool cyan_wool purple_wool blue_wool brown_wool green_wool red_wool black_wool",
            "36 moving_piston", "37 dandelion", "38 poppy", "39 brown_mushroom", "40 red_mushroom",
            "41 gold_block", "42 iron_block",
            "43 smooth_stone_slab sandstone_slab oak_slab cobblestone_slab",
            "44 smooth_stone_slab sandstone_slab oak_slab cobblestone_slab",
            "45 bricks", "46 tnt", "47 bookshelf", "48 mossy_cobblestone", "49 obsidian",
            "50 torch wall_torch", "51 fire", "52 spawner", "53 oak_stairs", "54 chest", "55 redstone_wire",
            "56 diamond_ore", "57 diamond_block", "58 crafting_table", "59 wheat", "60 farmland",
            "61 furnace", "62 furnace", "63 oak_sign", "64 oak_door", "65 ladder", "66 rail",
            "67 cobblestone_stairs", "68 oak_wall_sign", "69 lever", "70 stone_pressure_plate",
            "71 iron_door", "72 oak_pressure_plate", "73 redstone_ore", "74 redstone_ore",
            "75 redstone_torch redstone_wall_torch", "76 redstone_torch redstone_wall_torch",
            "77 stone_button", "78 snow", "79 ice", "80 snow_block", "81 cactus", "82 clay", "83 sugar_cane",
            "84 jukebox", "85 oak_fence", "86 carved_pumpkin", "87 netherrack", "88 soul_sand",
            "89 glowstone", "90 nether_portal", "91 jack_o_lantern", "92 cake", "93 repeater",
            "94 repeater", "96 oak_trapdoor",
    };

    public static final int LOCKED_CHEST = 95;

    public static final String[] ITEMS = {
            "256 iron_shovel", "257 iron_pickaxe", "258 iron_axe", "259 flint_and_steel", "260 apple",
            "261 bow", "262 arrow", "263 coal charcoal", "264 diamond", "265 iron_ingot", "266 gold_ingot",
            "267 iron_sword", "268 wooden_sword", "269 wooden_shovel", "270 wooden_pickaxe", "271 wooden_axe",
            "272 stone_sword", "273 stone_shovel", "274 stone_pickaxe", "275 stone_axe", "276 diamond_sword",
            "277 diamond_shovel", "278 diamond_pickaxe", "279 diamond_axe", "280 stick", "281 bowl",
            "282 mushroom_stew", "283 golden_sword", "284 golden_shovel", "285 golden_pickaxe",
            "286 golden_axe", "287 string", "288 feather", "289 gunpowder", "290 wooden_hoe", "291 stone_hoe",
            "292 iron_hoe", "293 diamond_hoe", "294 golden_hoe", "295 wheat_seeds", "296 wheat", "297 bread",
            "298 leather_helmet", "299 leather_chestplate", "300 leather_leggings", "301 leather_boots",
            "302 chainmail_helmet", "303 chainmail_chestplate", "304 chainmail_leggings", "305 chainmail_boots",
            "306 iron_helmet", "307 iron_chestplate", "308 iron_leggings", "309 iron_boots",
            "310 diamond_helmet", "311 diamond_chestplate", "312 diamond_leggings", "313 diamond_boots",
            "314 golden_helmet", "315 golden_chestplate", "316 golden_leggings", "317 golden_boots",
            "318 flint", "319 porkchop", "320 cooked_porkchop", "321 painting", "322 golden_apple",
            "323 oak_sign", "324 oak_door", "325 bucket", "326 water_bucket", "327 lava_bucket",
            "328 minecart", "329 saddle", "330 iron_door", "331 redstone", "332 snowball", "333 oak_boat",
            "334 leather", "335 milk_bucket", "336 brick", "337 clay_ball", "338 sugar_cane", "339 paper",
            "340 book", "341 slime_ball", "342 chest_minecart", "343 furnace_minecart", "344 egg",
            "345 compass", "346 fishing_rod", "347 clock", "348 glowstone_dust", "349 cod", "350 cooked_cod",
            "351 ink_sac red_dye green_dye cocoa_beans lapis_lazuli purple_dye cyan_dye light_gray_dye"
                    + " gray_dye pink_dye lime_dye yellow_dye light_blue_dye magenta_dye orange_dye bone_meal",
            "352 bone", "353 sugar", "354 cake", "355 red_bed", "356 repeater", "357 cookie",
            "358 map filled_map", "359 shears", "2256 music_disc_13", "2257 music_disc_cat",
    };

    public static final String[] ENTITIES = {
            "Arrow arrow", "Snowball snowball", "Item item", "Painting painting", "Creeper creeper",
            "Skeleton skeleton", "Spider spider", "Giant giant", "Zombie zombie", "Slime slime", "Ghast ghast",
            "PigZombie zombified_piglin", "Pig pig", "Sheep sheep", "Cow cow", "Chicken chicken", "Squid squid",
            "Wolf wolf", "PrimedTnt tnt", "FallingSand falling_block",
            "Minecart minecart chest_minecart furnace_minecart", "Boat oak_boat",
            "- player egg fishing_bobber fireball lightning_bolt",
    };

    private static final List<String> WOODS = List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped");
    private static final List<String> COLOURS = List.of("white", "orange", "magenta", "light_blue", "yellow",
            "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black");

    private static final Map<String, String> BLOCK_MAP = new HashMap<>();
    private static final Map<String, String> ITEM_MAP = new HashMap<>();
    private static final Set<String> BLOCK_SET = names(BLOCKS);
    private static final Set<String> ITEM_SET = names(ITEMS);
    private static final Set<String> ENTITY_SET = names(ENTITIES);

    static {
        for (String ore : new String[] {"coal", "iron", "gold", "lapis", "diamond", "redstone"}) {
            BLOCK_MAP.put("deepslate_" + ore + "_ore", ore + "_ore");
        }
        String[][] same = {
                {"stone_slab", "smooth_stone_slab"}, {"smooth_stone", "smooth_stone_slab[type=double]"},
                {"petrified_oak_slab", "oak_slab"}, {"cut_sandstone_slab", "sandstone_slab"},
                {"smooth_sandstone_slab", "sandstone_slab"}, {"chiseled_sandstone", "sandstone"},
                {"cut_sandstone", "sandstone"}, {"smooth_sandstone", "sandstone"},
                {"mossy_cobblestone_slab", "cobblestone_slab"}, {"mossy_cobblestone_stairs", "cobblestone_stairs"},
                {"infested_stone", "stone"}, {"infested_cobblestone", "cobblestone"},
                {"coarse_dirt", "dirt"}, {"rooted_dirt", "dirt"}, {"podzol", "grass_block"},
                {"mycelium", "grass_block"}, {"dirt_path", "grass_block"},
                {"tall_grass", "short_grass"}, {"large_fern", "fern"},
                {"packed_ice", "ice"}, {"blue_ice", "ice"}, {"frosted_ice", "water"},
                {"seagrass", "water"}, {"tall_seagrass", "water"}, {"kelp", "water"}, {"kelp_plant", "water"},
                {"bubble_column", "water"}, {"soul_torch", "torch"}, {"soul_wall_torch", "wall_torch"},
                {"soul_fire", "fire"}, {"pumpkin", "carved_pumpkin"}, {"trapped_chest", "chest"},
                {"tinted_glass", "glass"}, {"bamboo_mosaic", "oak_planks"},
                {"bamboo_mosaic_stairs", "oak_stairs"}, {"bamboo_mosaic_slab", "oak_slab"},
                {"bamboo_block", "oak_log"}, {"stripped_bamboo_block", "oak_log"},
        };
        for (String[] pair : same) {
            BLOCK_MAP.put(pair[0], pair[1]);
        }
        for (String wood : WOODS) {
            String log = wood.equals("spruce") || wood.equals("birch") ? wood + "_log" : "oak_log";
            for (String form : new String[] {"_log", "_wood", "_stem", "_hyphae"}) {
                BLOCK_MAP.putIfAbsent(wood + form, log);
                BLOCK_MAP.putIfAbsent("stripped_" + wood + form, log);
            }
            String[][] shapes = {{"_planks", "oak_planks"}, {"_leaves", "oak_leaves"}, {"_sapling", "oak_sapling"},
                    {"_stairs", "oak_stairs"}, {"_slab", "oak_slab"}, {"_fence", "oak_fence"},
                    {"_door", "oak_door"}, {"_trapdoor", "oak_trapdoor"}, {"_pressure_plate", "oak_pressure_plate"},
                    {"_sign", "oak_sign"}, {"_wall_sign", "oak_wall_sign"}};
            for (String[] shape : shapes) {
                BLOCK_MAP.putIfAbsent(wood + shape[0], shape[1]);
            }
        }
        BLOCK_MAP.put("mangrove_propagule", "oak_sapling");
        BLOCK_MAP.put("azalea_leaves", "oak_leaves");
        BLOCK_MAP.put("flowering_azalea_leaves", "oak_leaves");
        for (String colour : COLOURS) {
            BLOCK_MAP.put(colour + "_bed", "red_bed");
            BLOCK_MAP.put(colour + "_stained_glass", "glass");
        }
        for (String self : BLOCK_SET) {
            BLOCK_MAP.remove(self);
        }

        String[][] items = {
                {"raw_iron", "iron_ore"}, {"raw_gold", "gold_ore"}, {"black_dye", "ink_sac"},
                {"white_dye", "bone_meal"}, {"blue_dye", "lapis_lazuli"}, {"brown_dye", "cocoa_beans"},
                {"enchanted_golden_apple", "golden_apple"}, {"written_book", "book"}, {"writable_book", "book"},
                {"powder_snow_bucket", "bucket"}, {"bamboo_raft", "oak_boat"}, {"bamboo_chest_raft", "oak_boat"},
        };
        for (String[] pair : items) {
            ITEM_MAP.put(pair[0], pair[1]);
        }
        for (String fish : new String[] {"cod", "salmon", "pufferfish", "tropical_fish", "axolotl", "tadpole"}) {
            ITEM_MAP.put(fish + "_bucket", "water_bucket");
        }
        for (String wood : WOODS) {
            ITEM_MAP.put(wood + "_boat", "oak_boat");
            ITEM_MAP.put(wood + "_chest_boat", "oak_boat");
        }
        for (String self : ITEM_SET) {
            ITEM_MAP.remove(self);
        }
    }

    private BetaContent() {
    }

    private static Set<String> names(String[] table) {
        Set<String> names = new HashSet<>();
        for (String line : table) {
            String[] parts = line.split(" ");
            names.addAll(List.of(parts).subList(1, parts.length));
        }
        return Collections.unmodifiableSet(names);
    }

    public static Set<String> blocks() {
        return BLOCK_SET;
    }

    public static Set<String> items() {
        return ITEM_SET;
    }

    public static Set<String> entities() {
        return ENTITY_SET;
    }

    public static String convertBlock(String path, boolean waterlogged, boolean upperHalf) {
        if (BLOCK_SET.contains(path)) {
            return path;
        }
        String counterpart = BLOCK_MAP.get(path);
        if (counterpart != null && upperHalf && (path.equals("tall_grass") || path.equals("large_fern"))) {
            counterpart = null;
        }
        if (counterpart != null) {
            return counterpart;
        }
        return waterlogged ? "water" : "air";
    }

    public static String convertItem(String path) {
        if (ITEM_SET.contains(path)) {
            return path;
        }
        return ITEM_MAP.get(path);
    }

    public static String blockOf(String counterpart) {
        int bracket = counterpart.indexOf('[');
        return bracket < 0 ? counterpart : counterpart.substring(0, bracket);
    }
}
