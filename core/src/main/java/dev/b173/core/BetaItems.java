package dev.b173.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BetaItems {
    public static final int FIRE = BetaBlocks.FIRE;

    private static final String[] WOOL = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
            "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private static final String[] DYES = {"ink_sac", "red_dye", "green_dye", "cocoa_beans", "lapis_lazuli",
            "purple_dye", "cyan_dye", "light_gray_dye", "gray_dye", "pink_dye", "lime_dye", "yellow_dye",
            "light_blue_dye", "magenta_dye", "orange_dye", "bone_meal"};

    private static final Map<String, List<String>> ITEMS = new HashMap<>();

    static {
        String[] table = {
                "1 stone", "4 cobblestone", "5 oak_planks", "12 sand", "14 gold_ore", "15 iron_ore",
                "20 glass", "22 lapis_block", "23 dispenser", "24 sandstone", "25 note_block",
                "27 powered_rail", "28 detector_rail", "29 sticky_piston", "33 piston", "37 dandelion",
                "38 poppy", "39 brown_mushroom", "40 red_mushroom", "41 gold_block", "42 iron_block",
                "45 bricks", "46 tnt", "47 bookshelf", "50 torch", "53 oak_stairs", "54 chest",
                "56 diamond_ore", "57 diamond_block", "58 crafting_table", "61 furnace", "65 ladder",
                "66 rail", "67 cobblestone_stairs", "69 lever", "70 stone_pressure_plate",
                "72 oak_pressure_plate", "76 redstone_torch", "77 stone_button", "80 snow_block",
                "81 cactus", "82 clay", "84 jukebox", "85 oak_fence", "86 carved_pumpkin", "89 glowstone",
                "91 jack_o_lantern", "96 oak_trapdoor",
                "256 iron_shovel", "257 iron_pickaxe", "258 iron_axe", "259 flint_and_steel", "260 apple",
                "261 bow", "262 arrow", "264 diamond", "265 iron_ingot", "266 gold_ingot", "267 iron_sword",
                "268 wooden_sword", "269 wooden_shovel", "270 wooden_pickaxe", "271 wooden_axe",
                "272 stone_sword", "273 stone_shovel", "274 stone_pickaxe", "275 stone_axe",
                "276 diamond_sword", "277 diamond_shovel", "278 diamond_pickaxe", "279 diamond_axe",
                "280 stick", "281 bowl", "282 mushroom_stew", "283 golden_sword", "284 golden_shovel",
                "285 golden_pickaxe", "286 golden_axe", "287 string", "288 feather", "289 gunpowder",
                "290 wooden_hoe", "291 stone_hoe", "292 iron_hoe", "293 diamond_hoe", "294 golden_hoe", "295 wheat_seeds",
                "296 wheat", "297 bread", "298 leather_helmet", "299 leather_chestplate",
                "300 leather_leggings", "301 leather_boots", "302 chainmail_helmet",
                "303 chainmail_chestplate", "304 chainmail_leggings", "305 chainmail_boots",
                "306 iron_helmet", "307 iron_chestplate", "308 iron_leggings", "309 iron_boots",
                "310 diamond_helmet", "311 diamond_chestplate", "312 diamond_leggings", "313 diamond_boots",
                "314 golden_helmet", "315 golden_chestplate", "316 golden_leggings", "317 golden_boots",
                "318 flint", "319 porkchop", "320 cooked_porkchop", "321 painting", "322 golden_apple",
                "323 oak_sign", "324 oak_door", "325 bucket", "327 lava_bucket", "328 minecart",
                "330 iron_door", "331 redstone", "332 snowball", "333 oak_boat", "334 leather",
                "335 milk_bucket", "336 brick", "337 clay_ball", "338 sugar_cane", "339 paper", "340 book",
                "341 slime_ball", "342 chest_minecart", "343 furnace_minecart", "344 egg", "345 compass",
                "346 fishing_rod", "347 clock", "348 glowstone_dust", "349 cod", "350 cooked_cod",
                "352 bone", "353 sugar", "354 cake", "355 red_bed", "356 repeater", "357 cookie", "358 map",
                "359 shears",
        };
        for (String entry : table) {
            String[] parts = entry.split(" ");
            put(Integer.parseInt(parts[0]), 0, parts[1]);
        }
        put(5, -1, "oak_planks", "spruce_planks", "birch_planks");
        put(6, 0, "oak_sapling");
        put(6, 1, "spruce_sapling");
        put(6, 2, "birch_sapling");
        put(6, -1, "oak_sapling", "spruce_sapling", "birch_sapling");
        put(17, 0, "oak_log");
        put(17, 1, "spruce_log");
        put(17, 2, "birch_log");
        put(17, -1, "oak_log", "spruce_log", "birch_log");
        put(44, 0, "smooth_stone_slab");
        put(44, 1, "sandstone_slab");
        put(44, 2, "oak_slab");
        put(44, 3, "cobblestone_slab");
        put(263, 0, "coal");
        put(263, 1, "charcoal");
        put(263, -1, "coal", "charcoal");
        String[] anyWool = new String[WOOL.length];
        for (int i = 0; i < WOOL.length; i++) {
            put(35, i, WOOL[i] + "_wool");
            anyWool[i] = WOOL[i] + "_wool";
            put(351, i, DYES[i]);
        }
        put(35, -1, anyWool);
        put(351, -1, DYES);
    }

    private BetaItems() {
    }

    private static void put(int id, int damage, String... modern) {
        String[] ids = new String[modern.length];
        for (int i = 0; i < modern.length; i++) {
            ids[i] = "minecraft:" + modern[i];
        }
        ITEMS.put(id + ":" + damage, List.of(ids));
    }

    public static List<String> ingredient(int id, int damage) {
        if (id == FIRE) {
            return List.of();
        }
        List<String> exact = ITEMS.get(id + ":" + damage);
        if (exact != null) {
            return exact;
        }
        return damage == -1 ? ITEMS.get(id + ":0") : null;
    }

    public static String result(int id, int damage) {
        List<String> exact = ITEMS.get(id + ":" + damage);
        return exact == null || exact.size() != 1 ? null : exact.getFirst();
    }
}
