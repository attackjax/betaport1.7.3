package dev.b173.core.pack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BetaPackBuilder {
    public static final int RESOURCE_FORMAT = 84;
    public static final int REVISION = 9;

    public interface Source {
        byte[] read(String path) throws IOException;
    }

    public interface Sink {
        void write(String path, byte[] data) throws IOException;
    }

    public record Result(int files, List<String> missing, BetaSounds.Result sounds) {
    }

    private static final String TEXTURES = "assets/minecraft/textures/";
    private static final int LOOP_FRAMES = 96;
    private static final int LOOP_BLEND = 16;
    private static final int WARM_UP = 200;

    private static final String[] TERRAIN = {
            "grass_block_top 0", "stone 1", "dirt 2", "grass_block_side 3", "oak_planks 4",
            "smooth_stone_slab_side 5", "smooth_stone 6", "bricks 7", "tnt_side 8", "tnt_top 9", "tnt_bottom 10",
            "cobweb 11", "poppy 12", "dandelion 13", "oak_sapling 15",
            "cobblestone 16", "bedrock 17", "sand 18", "gravel 19", "oak_log 20", "oak_log_top 21",
            "spruce_log_top 21", "birch_log_top 21", "iron_block 22", "gold_block 23", "diamond_block 24",
            "red_mushroom 28", "brown_mushroom 29",
            "gold_ore 32", "iron_ore 33", "coal_ore 34", "bookshelf 35", "mossy_cobblestone 36", "obsidian 37",
            "grass_block_side_overlay 38", "short_grass 39", "crafting_table_top 43", "furnace_front 44",
            "furnace_side 45", "dispenser_front 46",
            "sponge 48", "glass 49", "diamond_ore 50", "redstone_ore 51", "dead_bush 55", "fern 56",
            "crafting_table_side 59", "crafting_table_front 60", "furnace_front_on 61", "furnace_top 62",
            "spruce_sapling 63",
            "white_wool 64", "spawner 65", "snow 66", "ice 67", "grass_block_snow 68", "cactus_top 69",
            "cactus_side 70", "cactus_bottom 71", "clay 72", "sugar_cane 73", "note_block 74", "jukebox_side 74",
            "jukebox_top 75", "birch_sapling 79",
            "torch 80", "oak_door_top 81", "iron_door_top 82", "ladder 83", "oak_trapdoor 84", "farmland_moist 86",
            "farmland 87", "wheat_stage0 88", "wheat_stage1 89", "wheat_stage2 90", "wheat_stage3 91",
            "wheat_stage4 92", "wheat_stage5 93", "wheat_stage6 94", "wheat_stage7 95",
            "lever 96", "oak_door_bottom 97", "iron_door_bottom 98", "redstone_torch 99", "pumpkin_top 102",
            "netherrack 103", "soul_sand 104", "glowstone 105", "piston_top_sticky 106", "piston_top 107",
            "piston_side 108", "piston_bottom 109", "piston_inner 110",
            "rail_corner 112", "black_wool 113", "gray_wool 114", "redstone_torch_off 115", "spruce_log 116",
            "birch_log 117", "pumpkin_side 118", "carved_pumpkin 119", "jack_o_lantern 120", "cake_top 121",
            "cake_side 122", "cake_inner 123", "cake_bottom 124",
            "rail 128", "red_wool 129", "pink_wool 130", "repeater 131",
            "lapis_block 144", "green_wool 145", "lime_wool 146", "repeater_on 147",
            "lapis_ore 160", "brown_wool 161", "yellow_wool 162", "powered_rail 163",
            "sandstone_top 176", "blue_wool 177", "light_blue_wool 178", "powered_rail_on 179",
            "sandstone 192", "purple_wool 193", "magenta_wool 194", "detector_rail 195", "detector_rail_on 195",
            "sandstone_bottom 208", "cyan_wool 209", "orange_wool 210",
            "light_gray_wool 225",
            "destroy_stage_0 240", "destroy_stage_1 241", "destroy_stage_2 242", "destroy_stage_3 243",
            "destroy_stage_4 244", "destroy_stage_5 245", "destroy_stage_6 246", "destroy_stage_7 247",
            "destroy_stage_8 248", "destroy_stage_9 249"
    };

    private static final String[] ITEMS = {
            "leather_helmet 0", "chainmail_helmet 1", "iron_helmet 2", "diamond_helmet 3", "golden_helmet 4",
            "flint_and_steel 5", "flint 6", "coal 7", "charcoal 7", "string 8", "wheat_seeds 9", "apple 10",
            "golden_apple 11", "egg 12", "sugar 13", "snowball 14",
            "leather_chestplate 16", "chainmail_chestplate 17", "iron_chestplate 18", "diamond_chestplate 19",
            "golden_chestplate 20", "bow 21", "bow_pulling_0 21", "bow_pulling_1 21", "bow_pulling_2 21",
            "brick 22", "iron_ingot 23", "feather 24", "wheat 25", "painting 26", "sugar_cane 27", "bone 28",
            "cake 29", "slime_ball 30",
            "leather_leggings 32", "chainmail_leggings 33", "iron_leggings 34", "diamond_leggings 35",
            "golden_leggings 36", "arrow 37", "gold_ingot 39", "gunpowder 40", "bread 41", "oak_sign 42",
            "oak_door 43", "iron_door 44",
            "leather_boots 48", "chainmail_boots 49", "iron_boots 50", "diamond_boots 51", "golden_boots 52",
            "stick 53", "diamond 55", "redstone 56", "clay_ball 57", "paper 58", "book 59", "map 60",
            "filled_map 60",
            "wooden_sword 64", "stone_sword 65", "iron_sword 66", "diamond_sword 67", "golden_sword 68",
            "fishing_rod 69", "bowl 71", "mushroom_stew 72", "glowstone_dust 73", "bucket 74", "water_bucket 75",
            "lava_bucket 76", "milk_bucket 77", "ink_sac 78", "gray_dye 79",
            "wooden_shovel 80", "stone_shovel 81", "iron_shovel 82", "diamond_shovel 83", "golden_shovel 84",
            "fishing_rod_cast 85", "repeater 86", "porkchop 87", "cooked_porkchop 88", "cod 89", "cooked_cod 90",
            "cookie 92", "shears 93", "red_dye 94", "pink_dye 95",
            "wooden_pickaxe 96", "stone_pickaxe 97", "iron_pickaxe 98", "diamond_pickaxe 99", "golden_pickaxe 100",
            "leather 103", "saddle 104", "green_dye 110", "lime_dye 111",
            "wooden_axe 112", "stone_axe 113", "iron_axe 114", "diamond_axe 115", "golden_axe 116",
            "cocoa_beans 126", "yellow_dye 127",
            "wooden_hoe 128", "stone_hoe 129", "iron_hoe 130", "diamond_hoe 131", "golden_hoe 132",
            "minecart 135", "oak_boat 136", "lapis_lazuli 142", "light_blue_dye 143",
            "chest_minecart 151", "purple_dye 158", "magenta_dye 159",
            "furnace_minecart 167", "cyan_dye 174", "orange_dye 175",
            "light_gray_dye 190", "bone_meal 191",
            "music_disc_13 240", "music_disc_cat 241"
    };

    private static final String[] NAMES = {
            "block.minecraft.stone tile.stone.name", "block.minecraft.grass_block tile.grass.name",
            "block.minecraft.dirt tile.dirt.name", "block.minecraft.cobblestone tile.stonebrick.name",
            "block.minecraft.oak_planks tile.wood.name", "block.minecraft.oak_sapling tile.sapling.name",
            "block.minecraft.spruce_sapling tile.sapling.name", "block.minecraft.birch_sapling tile.sapling.name",
            "block.minecraft.bedrock tile.bedrock.name", "block.minecraft.water tile.water.name",
            "block.minecraft.lava tile.lava.name", "block.minecraft.sand tile.sand.name",
            "block.minecraft.sandstone tile.sandStone.name", "block.minecraft.gravel tile.gravel.name",
            "block.minecraft.gold_ore tile.oreGold.name", "block.minecraft.iron_ore tile.oreIron.name",
            "block.minecraft.coal_ore tile.oreCoal.name", "block.minecraft.oak_log tile.log.name",
            "block.minecraft.spruce_log tile.log.name", "block.minecraft.birch_log tile.log.name",
            "block.minecraft.oak_leaves tile.leaves.name", "block.minecraft.spruce_leaves tile.leaves.name",
            "block.minecraft.birch_leaves tile.leaves.name", "block.minecraft.sponge tile.sponge.name",
            "block.minecraft.glass tile.glass.name", "block.minecraft.white_wool tile.cloth.white.name",
            "block.minecraft.orange_wool tile.cloth.orange.name", "block.minecraft.magenta_wool tile.cloth.magenta.name",
            "block.minecraft.light_blue_wool tile.cloth.lightBlue.name", "block.minecraft.yellow_wool tile.cloth.yellow.name",
            "block.minecraft.lime_wool tile.cloth.lime.name", "block.minecraft.pink_wool tile.cloth.pink.name",
            "block.minecraft.gray_wool tile.cloth.gray.name", "block.minecraft.light_gray_wool tile.cloth.silver.name",
            "block.minecraft.cyan_wool tile.cloth.cyan.name", "block.minecraft.purple_wool tile.cloth.purple.name",
            "block.minecraft.blue_wool tile.cloth.blue.name", "block.minecraft.brown_wool tile.cloth.brown.name",
            "block.minecraft.green_wool tile.cloth.green.name", "block.minecraft.red_wool tile.cloth.red.name",
            "block.minecraft.black_wool tile.cloth.black.name", "block.minecraft.dandelion tile.flower.name",
            "block.minecraft.poppy tile.rose.name", "block.minecraft.brown_mushroom tile.mushroom.name",
            "block.minecraft.red_mushroom tile.mushroom.name", "block.minecraft.gold_block tile.blockGold.name",
            "block.minecraft.iron_block tile.blockIron.name", "block.minecraft.smooth_stone_slab tile.stoneSlab.stone.name",
            "block.minecraft.sandstone_slab tile.stoneSlab.sand.name", "block.minecraft.oak_slab tile.stoneSlab.wood.name",
            "block.minecraft.petrified_oak_slab tile.stoneSlab.wood.name", "block.minecraft.cobblestone_slab tile.stoneSlab.cobble.name",
            "block.minecraft.bricks tile.brick.name", "block.minecraft.tnt tile.tnt.name",
            "block.minecraft.bookshelf tile.bookshelf.name", "block.minecraft.mossy_cobblestone tile.stoneMoss.name",
            "block.minecraft.obsidian tile.obsidian.name", "block.minecraft.torch tile.torch.name",
            "block.minecraft.wall_torch tile.torch.name", "block.minecraft.fire tile.fire.name",
            "block.minecraft.spawner tile.mobSpawner.name", "block.minecraft.oak_stairs tile.stairsWood.name",
            "block.minecraft.chest tile.chest.name", "block.minecraft.redstone_wire tile.redstoneDust.name",
            "block.minecraft.diamond_ore tile.oreDiamond.name", "block.minecraft.diamond_block tile.blockDiamond.name",
            "block.minecraft.crafting_table tile.workbench.name", "block.minecraft.wheat tile.crops.name",
            "block.minecraft.farmland tile.farmland.name", "block.minecraft.furnace tile.furnace.name",
            "block.minecraft.oak_sign tile.sign.name", "block.minecraft.oak_wall_sign tile.sign.name",
            "block.minecraft.oak_door item.doorWood.name", "block.minecraft.ladder tile.ladder.name",
            "block.minecraft.rail tile.rail.name", "block.minecraft.powered_rail tile.goldenRail.name",
            "block.minecraft.detector_rail tile.detectorRail.name", "block.minecraft.cobblestone_stairs tile.stairsStone.name",
            "block.minecraft.lever tile.lever.name", "block.minecraft.stone_pressure_plate tile.pressurePlate.name",
            "block.minecraft.oak_pressure_plate tile.pressurePlate.name", "block.minecraft.iron_door item.doorIron.name",
            "block.minecraft.redstone_ore tile.oreRedstone.name", "block.minecraft.redstone_torch tile.notGate.name",
            "block.minecraft.redstone_wall_torch tile.notGate.name", "block.minecraft.stone_button tile.button.name",
            "block.minecraft.snow tile.snow.name", "block.minecraft.snow_block tile.snow.name",
            "block.minecraft.ice tile.ice.name", "block.minecraft.cactus tile.cactus.name",
            "block.minecraft.clay tile.clay.name", "block.minecraft.sugar_cane item.reeds.name",
            "block.minecraft.jukebox tile.jukebox.name", "block.minecraft.oak_fence tile.fence.name",
            "block.minecraft.pumpkin tile.pumpkin.name", "block.minecraft.carved_pumpkin tile.pumpkin.name",
            "block.minecraft.jack_o_lantern tile.litpumpkin.name", "block.minecraft.netherrack tile.hellrock.name",
            "block.minecraft.soul_sand tile.hellsand.name", "block.minecraft.glowstone tile.lightgem.name",
            "block.minecraft.nether_portal tile.portal.name", "block.minecraft.lapis_ore tile.oreLapis.name",
            "block.minecraft.lapis_block tile.blockLapis.name", "block.minecraft.dispenser tile.dispenser.name",
            "block.minecraft.note_block tile.musicBlock.name", "block.minecraft.cake item.cake.name",
            "block.minecraft.red_bed item.bed.name", "block.minecraft.cobweb tile.web.name",
            "block.minecraft.oak_trapdoor tile.trapdoor.name", "block.minecraft.stone_bricks tile.stonebricksmooth.name",
            "block.minecraft.piston tile.pistonBase.name", "block.minecraft.sticky_piston tile.pistonStickyBase.name",
            "item.minecraft.iron_shovel item.shovelIron.name", "item.minecraft.iron_pickaxe item.pickaxeIron.name",
            "item.minecraft.iron_axe item.hatchetIron.name", "item.minecraft.flint_and_steel item.flintAndSteel.name",
            "item.minecraft.apple item.apple.name", "item.minecraft.cookie item.cookie.name",
            "item.minecraft.bow item.bow.name", "item.minecraft.arrow item.arrow.name",
            "item.minecraft.coal item.coal.name", "item.minecraft.charcoal item.charcoal.name",
            "item.minecraft.diamond item.emerald.name", "item.minecraft.iron_ingot item.ingotIron.name",
            "item.minecraft.gold_ingot item.ingotGold.name", "item.minecraft.iron_sword item.swordIron.name",
            "item.minecraft.wooden_sword item.swordWood.name", "item.minecraft.wooden_shovel item.shovelWood.name",
            "item.minecraft.wooden_pickaxe item.pickaxeWood.name", "item.minecraft.wooden_axe item.hatchetWood.name",
            "item.minecraft.stone_sword item.swordStone.name", "item.minecraft.stone_shovel item.shovelStone.name",
            "item.minecraft.stone_pickaxe item.pickaxeStone.name", "item.minecraft.stone_axe item.hatchetStone.name",
            "item.minecraft.diamond_sword item.swordDiamond.name", "item.minecraft.diamond_shovel item.shovelDiamond.name",
            "item.minecraft.diamond_pickaxe item.pickaxeDiamond.name", "item.minecraft.diamond_axe item.hatchetDiamond.name",
            "item.minecraft.stick item.stick.name", "item.minecraft.bowl item.bowl.name",
            "item.minecraft.mushroom_stew item.mushroomStew.name", "item.minecraft.golden_sword item.swordGold.name",
            "item.minecraft.golden_shovel item.shovelGold.name", "item.minecraft.golden_pickaxe item.pickaxeGold.name",
            "item.minecraft.golden_axe item.hatchetGold.name", "item.minecraft.string item.string.name",
            "item.minecraft.feather item.feather.name", "item.minecraft.gunpowder item.sulphur.name",
            "item.minecraft.wooden_hoe item.hoeWood.name", "item.minecraft.stone_hoe item.hoeStone.name",
            "item.minecraft.iron_hoe item.hoeIron.name", "item.minecraft.diamond_hoe item.hoeDiamond.name",
            "item.minecraft.golden_hoe item.hoeGold.name", "item.minecraft.wheat_seeds item.seeds.name",
            "item.minecraft.wheat item.wheat.name", "item.minecraft.bread item.bread.name",
            "item.minecraft.leather_helmet item.helmetCloth.name", "item.minecraft.leather_chestplate item.chestplateCloth.name",
            "item.minecraft.leather_leggings item.leggingsCloth.name", "item.minecraft.leather_boots item.bootsCloth.name",
            "item.minecraft.chainmail_helmet item.helmetChain.name", "item.minecraft.chainmail_chestplate item.chestplateChain.name",
            "item.minecraft.chainmail_leggings item.leggingsChain.name", "item.minecraft.chainmail_boots item.bootsChain.name",
            "item.minecraft.iron_helmet item.helmetIron.name", "item.minecraft.iron_chestplate item.chestplateIron.name",
            "item.minecraft.iron_leggings item.leggingsIron.name", "item.minecraft.iron_boots item.bootsIron.name",
            "item.minecraft.diamond_helmet item.helmetDiamond.name", "item.minecraft.diamond_chestplate item.chestplateDiamond.name",
            "item.minecraft.diamond_leggings item.leggingsDiamond.name", "item.minecraft.diamond_boots item.bootsDiamond.name",
            "item.minecraft.golden_helmet item.helmetGold.name", "item.minecraft.golden_chestplate item.chestplateGold.name",
            "item.minecraft.golden_leggings item.leggingsGold.name", "item.minecraft.golden_boots item.bootsGold.name",
            "item.minecraft.flint item.flint.name", "item.minecraft.porkchop item.porkchopRaw.name",
            "item.minecraft.cooked_porkchop item.porkchopCooked.name", "item.minecraft.painting item.painting.name",
            "item.minecraft.golden_apple item.appleGold.name", "item.minecraft.bucket item.bucket.name",
            "item.minecraft.water_bucket item.bucketWater.name", "item.minecraft.lava_bucket item.bucketLava.name",
            "item.minecraft.minecart item.minecart.name", "item.minecraft.saddle item.saddle.name",
            "item.minecraft.redstone item.redstone.name", "item.minecraft.snowball item.snowball.name",
            "item.minecraft.oak_boat item.boat.name", "item.minecraft.leather item.leather.name",
            "item.minecraft.milk_bucket item.milk.name", "item.minecraft.brick item.brick.name",
            "item.minecraft.clay_ball item.clay.name", "item.minecraft.paper item.paper.name",
            "item.minecraft.book item.book.name", "item.minecraft.slime_ball item.slimeball.name",
            "item.minecraft.chest_minecart item.minecartChest.name", "item.minecraft.furnace_minecart item.minecartFurnace.name",
            "item.minecraft.egg item.egg.name", "item.minecraft.compass item.compass.name",
            "item.minecraft.fishing_rod item.fishingRod.name", "item.minecraft.clock item.clock.name",
            "item.minecraft.glowstone_dust item.yellowDust.name", "item.minecraft.cod item.fishRaw.name",
            "item.minecraft.cooked_cod item.fishCooked.name", "item.minecraft.music_disc_13 item.record.name",
            "item.minecraft.music_disc_cat item.record.name", "item.minecraft.bone item.bone.name",
            "item.minecraft.ink_sac item.dyePowder.black.name", "item.minecraft.red_dye item.dyePowder.red.name",
            "item.minecraft.green_dye item.dyePowder.green.name", "item.minecraft.cocoa_beans item.dyePowder.brown.name",
            "item.minecraft.lapis_lazuli item.dyePowder.blue.name", "item.minecraft.purple_dye item.dyePowder.purple.name",
            "item.minecraft.cyan_dye item.dyePowder.cyan.name", "item.minecraft.light_gray_dye item.dyePowder.silver.name",
            "item.minecraft.gray_dye item.dyePowder.gray.name", "item.minecraft.pink_dye item.dyePowder.pink.name",
            "item.minecraft.lime_dye item.dyePowder.lime.name", "item.minecraft.yellow_dye item.dyePowder.yellow.name",
            "item.minecraft.light_blue_dye item.dyePowder.lightBlue.name", "item.minecraft.magenta_dye item.dyePowder.magenta.name",
            "item.minecraft.orange_dye item.dyePowder.orange.name", "item.minecraft.bone_meal item.dyePowder.white.name",
            "item.minecraft.sugar item.sugar.name", "block.minecraft.repeater item.diode.name",
            "item.minecraft.filled_map item.map.name", "item.minecraft.map item.map.name",
            "item.minecraft.shears item.shears.name", "key.forward key.forward",
            "key.left key.left", "key.back key.back",
            "key.right key.right", "key.jump key.jump",
            "key.inventory key.inventory", "key.drop key.drop",
            "key.chat key.chat", "key.sneak key.sneak",
            "key.playerlist key.playerlist", "gui.toMenu gui.toMenu", "key.fog key.fog",
            "multiplayer.downloadingTerrain multiplayer.downloadingTerrain"
    };

    private static final Object[][] PAINTINGS = {
            {"kebab", 16, 16, 0, 0}, {"aztec", 16, 16, 16, 0}, {"alban", 16, 16, 32, 0},
            {"aztec2", 16, 16, 48, 0}, {"bomb", 16, 16, 64, 0}, {"plant", 16, 16, 80, 0},
            {"wasteland", 16, 16, 96, 0}, {"pool", 32, 16, 0, 32}, {"courbet", 32, 16, 32, 32},
            {"sea", 32, 16, 64, 32}, {"sunset", 32, 16, 96, 32}, {"creebet", 32, 16, 128, 32},
            {"wanderer", 16, 32, 0, 64}, {"graham", 16, 32, 16, 64}, {"match", 32, 32, 0, 128},
            {"bust", 32, 32, 32, 128}, {"stage", 32, 32, 64, 128}, {"void", 32, 32, 96, 128},
            {"skull_and_roses", 32, 32, 128, 128}, {"fighters", 64, 32, 0, 96},
            {"pointer", 64, 64, 0, 192}, {"pigscene", 64, 64, 64, 192}, {"burning_skull", 64, 64, 128, 192},
            {"skeleton", 64, 48, 192, 64}, {"donkey_kong", 64, 48, 192, 112}, {"back", 16, 16, 192, 0}
    };

    private static final Object[][] MOBS = {
            {"mob/pig.png", "entity/pig/pig_temperate", 64, 64},
            {"mob/saddle.png", "entity/equipment/pig_saddle/saddle", 64, 64},
            {"mob/cow.png", "entity/cow/cow_temperate", 64, 64},
            {"mob/sheep.png", "entity/sheep/sheep", 64, 32},
            {"mob/sheep_fur.png", "entity/sheep/sheep_wool", 64, 32},
            {"mob/creeper.png", "entity/creeper/creeper", 64, 32},
            {"armor/power.png", "entity/creeper/creeper_armor", 64, 32},
            {"mob/ghast.png", "entity/ghast/ghast", 64, 32},
            {"mob/ghast_fire.png", "entity/ghast/ghast_shooting", 64, 32},
            {"mob/spider.png", "entity/spider/spider", 64, 32},
            {"mob/spider_eyes.png", "entity/spider/spider_eyes", 64, 32},
            {"mob/zombie.png", "entity/zombie/zombie", 64, 64},
            {"mob/pigzombie.png", "entity/piglin/zombified_piglin", 64, 64},
            {"mob/skeleton.png", "entity/skeleton/skeleton", 64, 32},
            {"mob/chicken.png", "entity/chicken/chicken_temperate", 64, 32},
            {"mob/squid.png", "entity/squid/squid", 64, 32},
            {"mob/slime.png", "entity/slime/slime", 64, 32},
            {"mob/wolf.png", "entity/wolf/wolf", 64, 32},
            {"mob/wolf_tame.png", "entity/wolf/wolf_tame", 64, 32},
            {"mob/wolf_angry.png", "entity/wolf/wolf_angry", 64, 32},
            {"item/boat.png", "entity/boat/oak", 128, 64}
    };

    private static final String[] DEFAULT_SKINS = {
            "alex", "ari", "efe", "kai", "makena", "noor", "steve", "sunny", "zuri"
    };

    private static final String[] DYE_COLOURS = {
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan",
            "purple", "blue", "brown", "green", "red", "black"
    };

    private static final String[] MOON_PHASES = {
            "new_moon", "waxing_crescent", "first_quarter", "waxing_gibbous",
            "full_moon", "waning_gibbous", "third_quarter", "waning_crescent"
    };

    private final Source beta;
    private final Sink sink;
    private final Map<String, Raster> images = new HashMap<>();
    private final List<String> missing = new ArrayList<>();
    private final Map<String, String> lang = new LinkedHashMap<>();
    private int files;

    private BetaPackBuilder(Source beta, Sink sink) {
        this.beta = beta;
        this.sink = sink;
    }

    public static Result build(Source beta, BetaSounds.Resources sounds, Sink sink) throws IOException {
        BetaPackBuilder builder = new BetaPackBuilder(beta, sink);
        if (builder.image("terrain.png") == null) {
            throw new IOException("this jar has no terrain.png; it is not a Beta 1.7.3 client jar");
        }
        builder.write();
        BetaSounds.Result soundResult = null;
        if (sounds != null) {
            soundResult = BetaSounds.write(sounds, (path, data) -> builder.raw(path, data));
        }
        return new Result(builder.files, List.copyOf(builder.missing), soundResult);
    }

    private void write() throws IOException {
        text("pack.mcmeta", """
                {
                  "pack": {
                    "description": "Beta 1.7.3, drawn from your own Beta 1.7.3 jar",
                    "min_format": %d,
                    "max_format": %d
                  }
                }
                """.formatted(RESOURCE_FORMAT, RESOURCE_FORMAT));
        Raster icon = image("pack.png");
        if (icon != null) {
            raw("pack.png", Png.write(icon));
        }
        text("assets/b173/beta.json", "{\"revision\": " + REVISION + "}\n");
        blocks();
        blockModels();
        animations();
        items();
        gui();
        environment();
        entities();
        misc();
        names();
    }

    private void blocks() throws IOException {
        Raster terrain = image("terrain.png");
        for (String entry : TERRAIN) {
            String[] parts = entry.split(" ");
            texture("block/" + parts[0], tile(terrain, Integer.parseInt(parts[1])));
        }
        texture("block/oak_leaves", leaves(terrain, 52, 53));
        texture("block/birch_leaves", leaves(terrain, 52, 53));
        texture("block/spruce_leaves", leaves(terrain, 132, 133));

        Raster line = tile(terrain, 165).rotateClockwise();
        texture("block/redstone_dust_line0", line);
        texture("block/redstone_dust_line1", line);
        Raster cross = tile(terrain, 164);
        Raster dot = new Raster(16, 16);
        for (int y = 5; y <= 10; y++) {
            for (int x = 5; x <= 10; x++) {
                dot.set(x, y, cross.get(x, y));
            }
        }
        texture("block/redstone_dust_dot", dot);
        texture("block/redstone_dust_overlay", new Raster(16, 16));

        for (String name : new String[]{"oak_leaves", "birch_leaves", "spruce_leaves"}) {
            meta("block/" + name, "{\"texture\": {\"mipmap_strategy\": \"dark_cutout\"}}");
        }
        for (String name : new String[]{"dandelion", "poppy", "brown_mushroom", "red_mushroom"}) {
            meta("block/" + name, "{\"texture\": {\"mipmap_strategy\": \"strict_cutout\"}}");
        }
        for (String name : new String[]{"glass", "redstone_dust_dot", "redstone_dust_line0", "redstone_dust_line1"}) {
            meta("block/" + name, "{\"texture\": {\"mipmap_strategy\": \"mean\"}}");
        }
        for (String name : new String[]{"cactus_side", "cactus_top"}) {
            meta("block/" + name, "{\"texture\": {\"alpha_cutoff_bias\": 0.1}}");
        }
    }

    private static Raster tile(Raster atlas, int index) {
        return atlas.crop(index % 16 * 16, index / 16 * 16, 16, 16);
    }

    private static Raster leaves(Raster terrain, int fancy, int fast) {
        Raster holes = tile(terrain, fancy);
        Raster solid = tile(terrain, fast);
        Raster out = new Raster(16, 16);
        for (int i = 0; i < 256; i++) {
            int h = holes.argb[i];
            out.argb[i] = h >>> 24 != 0 ? h : solid.argb[i] & 0x00FFFFFF;
        }
        return out;
    }

    private void blockModels() throws IOException {
        Raster terrain = image("terrain.png");
        int[][] chestTiles = {{25, 0}, {26, 0}, {27, 0}, {41, 0}, {42, 0}, {57, 0}, {58, 0}};
        String[] chestNames = {"top", "side", "front", "front_a", "front_b", "back_a", "back_b"};
        for (int i = 0; i < chestNames.length; i++) {
            texture("b173", "block/chest_" + chestNames[i], tile(terrain, chestTiles[i][0]));
        }
        for (String[] variant : BetaBlockModels.chestVariants()) {
            String[] textures = new String[7];
            for (int side = 0; side < 6; side++) {
                textures[side] = "b173:block/chest_" + variant[side + 1];
            }
            textures[6] = "b173:block/chest_top";
            text("assets/b173/models/block/chest_" + variant[0] + ".json", BetaBlockModels.cube(textures, true, null));
        }

        int[] bedTiles = {134, 135, 149, 150, 151, 152};
        String[] bedNames = {"foot_top", "head_top", "foot_end", "foot_side", "head_side", "head_end"};
        String[] bedTextures = new String[7];
        for (int i = 0; i < bedTiles.length; i++) {
            texture("b173", "block/bed_" + bedNames[i], tile(terrain, bedTiles[i]));
            bedTextures[i] = "b173:block/bed_" + bedNames[i];
        }
        bedTextures[6] = "minecraft:block/oak_planks";
        StringBuilder variants = new StringBuilder("{\n  \"variants\": {\n");
        for (int dir = 0; dir < 4; dir++) {
            for (boolean head : new boolean[]{false, true}) {
                String part = head ? "head" : "foot";
                String model = "bed_" + part + "_" + BetaBlockModels.BED_FACING[dir];
                text("assets/b173/models/block/" + model + ".json", BetaBlockModels.bed(dir, head, bedTextures));
                variants.append("    \"facing=").append(BetaBlockModels.BED_FACING[dir]).append(",part=").append(part)
                        .append("\": {\"model\": \"b173:block/").append(model).append("\"}")
                        .append(dir == 3 && head ? "\n" : ",\n");
            }
        }
        String bedStates = variants.append("  }\n}\n").toString();
        for (String colour : DYE_COLOURS) {
            text("assets/minecraft/blockstates/" + colour + "_bed.json", bedStates);
        }

        String gui = "  \"display\": {\"gui\": {\"rotation\": [30, 315, 0], \"translation\": [0, 0, 0], "
                + "\"scale\": [0.625, 0.625, 0.625]}},\n";
        String[][] icons = {
                {"chest", "b173:block/chest_top", "b173:block/chest_top", "b173:block/chest_side",
                        "b173:block/chest_front", "b173:block/chest_side", "b173:block/chest_side"},
                {"furnace", "minecraft:block/furnace_top", "minecraft:block/furnace_top", "minecraft:block/furnace_side",
                        "minecraft:block/furnace_front", "minecraft:block/furnace_side", "minecraft:block/furnace_side"},
                {"dispenser", "minecraft:block/furnace_top", "minecraft:block/furnace_top", "minecraft:block/furnace_side",
                        "minecraft:block/dispenser_front", "minecraft:block/furnace_side", "minecraft:block/furnace_side"},
                {"carved_pumpkin", "minecraft:block/pumpkin_top", "minecraft:block/pumpkin_top", "minecraft:block/pumpkin_side",
                        "minecraft:block/carved_pumpkin", "minecraft:block/pumpkin_side", "minecraft:block/pumpkin_side"},
                {"jack_o_lantern", "minecraft:block/pumpkin_top", "minecraft:block/pumpkin_top", "minecraft:block/pumpkin_side",
                        "minecraft:block/jack_o_lantern", "minecraft:block/pumpkin_side", "minecraft:block/pumpkin_side"},
                {"crafting_table", "minecraft:block/oak_planks", "minecraft:block/crafting_table_top",
                        "minecraft:block/crafting_table_front", "minecraft:block/crafting_table_side",
                        "minecraft:block/crafting_table_front", "minecraft:block/crafting_table_side"}
        };
        for (String[] icon : icons) {
            String[] textures = new String[7];
            System.arraycopy(icon, 1, textures, 0, 6);
            textures[6] = icon[3];
            text("assets/b173/models/item/" + icon[0] + ".json", BetaBlockModels.cube(textures, true, gui));
            text("assets/minecraft/items/" + icon[0] + ".json",
                    "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"b173:item/" + icon[0] + "\"}}\n");
        }
    }

    private void animations() throws IOException {
        Raster[] still = BetaTextureFX.loop(
                new BetaTextureFX.Water(false, BetaTextureFX.seeded(0x5741544552L)), WARM_UP, LOOP_FRAMES, LOOP_BLEND);
        animation("block/water_still", still);
        texture("block/water_overlay", still[0]);
        animation("block/water_flow", BetaTextureFX.loop(
                new BetaTextureFX.Water(true, BetaTextureFX.seeded(0x464C4F57L)), WARM_UP, LOOP_FRAMES, LOOP_BLEND));
        animation("block/lava_still", BetaTextureFX.loop(
                new BetaTextureFX.Lava(false, BetaTextureFX.seeded(0x4C415641L)), WARM_UP, LOOP_FRAMES, LOOP_BLEND));
        animation("block/lava_flow", BetaTextureFX.loop(
                new BetaTextureFX.Lava(true, BetaTextureFX.seeded(0x4C415646L)), WARM_UP, LOOP_FRAMES, LOOP_BLEND));
        animation("block/fire_0", BetaTextureFX.loop(
                new BetaTextureFX.Fire(BetaTextureFX.seeded(0x46495245L)), WARM_UP, LOOP_FRAMES, LOOP_BLEND));
        animation("block/fire_1", BetaTextureFX.loop(
                new BetaTextureFX.Fire(BetaTextureFX.seeded(0x46495246L)), WARM_UP, LOOP_FRAMES, LOOP_BLEND));
        animation("block/nether_portal", BetaTextureFX.loop(new BetaTextureFX.Portal(), 0, 32, 0));
    }

    private void animation(String name, Raster[] frames) throws IOException {
        texture(name, Raster.strip(frames));
        meta(name, "{\"animation\": {\"frametime\": 1}}");
    }

    private void items() throws IOException {
        Raster items = image("gui/items.png");
        if (items == null) {
            return;
        }
        for (String entry : ITEMS) {
            String[] parts = entry.split(" ");
            texture("item/" + parts[0], tile(items, Integer.parseInt(parts[1])));
        }
        Raster clear = new Raster(16, 16);
        for (String piece : new String[]{"helmet", "chestplate", "leggings", "boots"}) {
            texture("item/leather_" + piece + "_overlay", clear);
            text("assets/minecraft/items/leather_" + piece + ".json",
                    "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"minecraft:item/leather_" + piece + "\"}}\n");
        }
        texture("item/filled_map_markings", clear);
        texture("b173", "item/bed", tile(items, 45));
        text("assets/b173/models/item/bed.json",
                "{\"parent\": \"minecraft:item/generated\", \"textures\": {\"layer0\": \"b173:item/bed\"}}\n");
        for (String colour : DYE_COLOURS) {
            text("assets/minecraft/items/" + colour + "_bed.json",
                    "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"b173:item/bed\"}}\n");
        }
        texture("gui/sprites/container/slot/helmet", tile(items, 15));
        texture("gui/sprites/container/slot/chestplate", tile(items, 31));
        texture("gui/sprites/container/slot/leggings", tile(items, 47));
        texture("gui/sprites/container/slot/boots", tile(items, 63));

        compass(tile(items, 54));
        Raster dial = image("misc/dial.png");
        if (dial != null) {
            clock(tile(items, 70), dial);
        }
    }

    private void compass(Raster base) throws IOException {
        for (int frame = 0; frame < 32; frame++) {
            double turn = (double) Math.floorMod(frame - 16, 32) / 32.0D;
            double angle = -Math.PI - turn * Math.PI * 2.0D;
            double sin = Math.sin(angle);
            double cos = Math.cos(angle);
            Raster out = base.copy();
            for (int k = -4; k <= 4; k++) {
                int x = (int) (8.5D + cos * (double) k * 0.3D);
                int y = (int) (7.5D - sin * (double) k * 0.3D * 0.5D);
                out.set(x, y, 0xFF646464);
            }
            for (int k = -8; k <= 16; k++) {
                int x = (int) (8.5D + sin * (double) k * 0.3D);
                int y = (int) (7.5D + cos * (double) k * 0.3D * 0.5D);
                out.set(x, y, k >= 0 ? 0xFFFF1414 : 0xFF646464);
            }
            texture("item/compass_%02d".formatted(frame), out);
        }
    }

    private void clock(Raster base, Raster dial) throws IOException {
        for (int frame = 0; frame < 64; frame++) {
            double angle = -((double) frame / 64.0D) * Math.PI * 2.0D;
            double sin = Math.sin(angle);
            double cos = Math.cos(angle);
            Raster out = base.copy();
            for (int i = 0; i < 256; i++) {
                int pixel = base.argb[i];
                int r = pixel >> 16 & 255;
                int g = pixel >> 8 & 255;
                int b = pixel & 255;
                if (r == b && g == 0 && b > 0) {
                    double u = -((double) (i % 16) / 15.0D - 0.5D);
                    double v = (double) (i / 16) / 15.0D - 0.5D;
                    int sx = (int) ((u * cos + v * sin + 0.5D) * 16.0D);
                    int sy = (int) ((v * cos - u * sin + 0.5D) * 16.0D);
                    int d = dial.get(sx & 15, sy & 15);
                    int shade = r;
                    out.argb[i] = (d >>> 24) << 24 | ((d >> 16 & 255) * shade / 255) << 16
                            | ((d >> 8 & 255) * shade / 255) << 8 | (d & 255) * shade / 255;
                }
            }
            texture("item/clock_%02d".formatted(frame), out);
        }
    }

    private void gui() throws IOException {
        Raster gui = image("gui/gui.png");
        if (gui != null) {
            texture("gui/sprites/hud/hotbar", gui.crop(0, 0, 182, 22));
            texture("gui/sprites/hud/hotbar_selection", new Raster(24, 23).paste(gui.crop(0, 22, 24, 22), 0, 0));
            String button = "{\"gui\": {\"scaling\": {\"type\": \"nine_slice\", \"width\": 200, \"height\": 20, "
                    + "\"border\": {\"left\": 75, \"right\": 75, \"top\": 3, \"bottom\": 3}}}}";
            Raster disabled = gui.crop(0, 46, 200, 20);
            Raster normal = gui.crop(0, 66, 200, 20);
            Raster hovered = gui.crop(0, 86, 200, 20);
            sprite("widget/button", normal, button);
            sprite("widget/button_disabled", disabled, button);
            sprite("widget/button_highlighted", hovered, button);
            sprite("widget/slider", disabled, button);
            sprite("widget/slider_highlighted", disabled, button);
            Raster handle = new Raster(8, 20).paste(normal.crop(0, 0, 4, 20), 0, 0).paste(normal.crop(196, 0, 4, 20), 4, 0);
            String handleMeta = "{\"gui\": {\"scaling\": {\"type\": \"nine_slice\", \"width\": 8, \"height\": 20, "
                    + "\"border\": {\"left\": 2, \"top\": 2, \"right\": 2, \"bottom\": 3}}}}";
            sprite("widget/slider_handle", handle, handleMeta);
            sprite("widget/slider_handle_highlighted", handle, handleMeta);
        }
        Raster field = new Raster(200, 20);
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 200; x++) {
                boolean edge = x == 0 || y == 0 || x == 199 || y == 19;
                field.set(x, y, edge ? 0xFFA0A0A0 : 0xFF000000);
            }
        }
        String fieldMeta = "{\"gui\": {\"scaling\": {\"type\": \"nine_slice\", \"width\": 200, \"height\": 20, \"border\": 1}}}";
        sprite("widget/text_field", field, fieldMeta);
        sprite("widget/text_field_highlighted", field, fieldMeta);

        Raster icons = image("gui/icons.png");
        if (icons != null) {
            texture("gui/sprites/hud/crosshair", icons.crop(0, 0, 15, 15));
            texture("gui/sprites/hud/heart/container", icons.crop(16, 0, 9, 9));
            texture("gui/sprites/hud/heart/container_blinking", icons.crop(25, 0, 9, 9));
            texture("gui/sprites/hud/heart/full", icons.crop(52, 0, 9, 9));
            texture("gui/sprites/hud/heart/half", icons.crop(61, 0, 9, 9));
            texture("gui/sprites/hud/heart/full_blinking", icons.crop(70, 0, 9, 9));
            texture("gui/sprites/hud/heart/half_blinking", icons.crop(79, 0, 9, 9));
            texture("gui/sprites/hud/armor_empty", icons.crop(16, 9, 9, 9));
            texture("gui/sprites/hud/armor_half", icons.crop(25, 9, 9, 9));
            texture("gui/sprites/hud/armor_full", icons.crop(34, 9, 9, 9));
            texture("gui/sprites/hud/air", icons.crop(16, 18, 9, 9));
            texture("gui/sprites/hud/air_bursting", icons.crop(25, 18, 9, 9));
            texture("gui/sprites/hud/air_empty", new Raster(9, 9));
        }

        copy("gui/inventory.png", "gui/container/inventory");
        copy("gui/crafting.png", "gui/container/crafting_table");
        copy("gui/container.png", "gui/container/generic_54");
        copy("gui/trap.png", "gui/container/dispenser");
        String highlightMeta = "{\"gui\": {\"scaling\": {\"type\": \"nine_slice\", \"width\": 24, \"height\": 24, \"border\": 4}}}";
        Raster highlight = new Raster(24, 24);
        for (int y = 4; y < 20; y++) {
            for (int x = 4; x < 20; x++) {
                highlight.set(x, y, 0x80FFFFFF);
            }
        }
        sprite("container/slot_highlight_front", highlight, highlightMeta);
        sprite("container/slot_highlight_back", new Raster(24, 24), highlightMeta);
        Raster tooltip = new Raster(100, 100);
        for (int y = 9; y < 91; y++) {
            for (int x = 9; x < 91; x++) {
                tooltip.set(x, y, 0xC0000000);
            }
        }
        sprite("tooltip/background", tooltip,
                "{\"gui\": {\"scaling\": {\"type\": \"nine_slice\", \"width\": 100, \"height\": 100, \"border\": 9}}}");
        sprite("tooltip/frame", new Raster(100, 100),
                "{\"gui\": {\"scaling\": {\"type\": \"nine_slice\", \"width\": 100, \"height\": 100, \"border\": 10, \"stretch_inner\": true}}}");
        Raster furnace = image("gui/furnace.png");
        if (furnace != null) {
            texture("gui/container/furnace", furnace);
            texture("gui/sprites/container/furnace/lit_progress", furnace.crop(176, 0, 14, 14));
            texture("gui/sprites/container/furnace/burn_progress", furnace.crop(176, 14, 24, 16));
        }

        copy("font/default.png", "font/ascii");

        Raster dirt = image("gui/background.png");
        if (dirt != null) {
            texture("gui/menu_background", dirt.multiply(0x404040));
            texture("gui/menu_list_background", dirt.multiply(0x202020));
            texture("gui/header_separator", new Raster(32, 2));
            texture("gui/footer_separator", new Raster(32, 2));
        }
        texture("gui/inworld_menu_background", new Raster(16, 16));
        texture("gui/inworld_menu_list_background", new Raster(16, 16));
        texture("gui/inworld_header_separator", new Raster(32, 2));
        texture("gui/inworld_footer_separator", new Raster(32, 2));

        Raster logo = image("title/mclogo.png");
        if (logo != null) {
            texture("b173", "gui/title/mclogo", logo);
        }
        lang.put("title.credits", "Copyright Mojang AB. Do not distribute.");
        lang.put("container.chestDouble", "Large chest");
        lang.put("record.nowPlaying", "Now playing: %s");

        byte[] words = beta.read("lang/en_US.lang");
        if (words != null) {
            for (String line : new String(words, StandardCharsets.UTF_8).split("\\R")) {
                if (line.startsWith("selectWorld.enterSeed=")) {
                    lang.put("selectWorld.enterSeed", line.substring(line.indexOf('=') + 1));
                }
            }
        } else {
            missing.add("lang/en_US.lang");
        }
        byte[] fontCharacters = beta.read("font.txt");
        if (fontCharacters != null) {
            raw("assets/b173/font.txt", fontCharacters);
        } else {
            missing.add("font.txt");
        }

        byte[] splashes = beta.read("title/splashes.txt");
        if (splashes != null) {
            raw("assets/minecraft/texts/splashes.txt", splashes);
        } else {
            missing.add("title/splashes.txt");
        }
    }

    private void sprite(String name, Raster image, String mcmeta) throws IOException {
        texture("gui/sprites/" + name, image);
        meta("gui/sprites/" + name, mcmeta);
    }

    private void environment() throws IOException {
        copy("environment/clouds.png", "environment/clouds");
        copy("environment/rain.png", "environment/rain");
        copy("environment/snow.png", "environment/snow");
        copy("terrain/sun.png", "environment/celestial/sun");
        Raster moon = image("terrain/moon.png");
        if (moon != null) {
            for (String phase : MOON_PHASES) {
                texture("environment/celestial/moon/" + phase, moon);
            }
        }
        copy("misc/grasscolor.png", "colormap/grass");
        copy("misc/foliagecolor.png", "colormap/foliage");
    }

    private void entities() throws IOException {
        String[][] armour = {{"cloth", "leather"}, {"chain", "chainmail"}, {"iron", "iron"},
                {"diamond", "diamond"}, {"gold", "gold"}};
        for (String[] material : armour) {
            copy("armor/" + material[0] + "_1.png", "entity/equipment/humanoid/" + material[1]);
            copy("armor/" + material[0] + "_2.png", "entity/equipment/humanoid_leggings/" + material[1]);
        }
        texture("entity/equipment/humanoid/leather_overlay", new Raster(64, 32));
        texture("entity/equipment/humanoid_leggings/leather_overlay", new Raster(64, 32));
        text("assets/minecraft/equipment/leather.json", """
                {
                  "layers": {
                    "humanoid": [{"texture": "minecraft:leather"}],
                    "humanoid_leggings": [{"texture": "minecraft:leather"}],
                    "humanoid_baby": [
                      {"texture": "minecraft:leather", "dyeable": {"color_when_undyed": -6265536}},
                      {"texture": "minecraft:leather_overlay"}
                    ],
                    "horse_body": [
                      {"texture": "minecraft:leather", "dyeable": {"color_when_undyed": -6265536}},
                      {"texture": "minecraft:leather_overlay"}
                    ]
                  }
                }
                """);

        Raster art = image("art/kz.png");
        if (art != null) {
            for (Object[] painting : PAINTINGS) {
                texture("painting/" + painting[0],
                        art.crop((Integer) painting[3], (Integer) painting[4], (Integer) painting[1], (Integer) painting[2]));
            }
        }
        copy("item/sign.png", "entity/signs/oak");
        copy("item/cart.png", "entity/minecart");
        copy("item/arrows.png", "entity/projectiles/arrow");

        for (Object[] mob : MOBS) {
            padded((String) mob[0], (String) mob[1], (Integer) mob[2], (Integer) mob[3]);
        }
        for (String skin : DEFAULT_SKINS) {
            padded("mob/char.png", "entity/player/wide/" + skin, 64, 64);
            padded("mob/char.png", "entity/player/slim/" + skin, 64, 64);
        }
        texture("entity/wolf/wolf_collar", new Raster(64, 32));
        texture("entity/sheep/sheep_wool_undercoat", new Raster(64, 32));
    }

    private void padded(String betaPath, String texture, int width, int height) throws IOException {
        Raster image = image(betaPath);
        if (image == null) {
            return;
        }
        if (image.width == width && image.height == height) {
            texture(texture, image);
            return;
        }
        Raster canvas = new Raster(width, height);
        canvas.paste(image, 0, 0);
        texture(texture, canvas);
    }

    private void misc() throws IOException {
        copy("misc/vignette.png", "misc/vignette");
        copy("misc/pumpkinblur.png", "misc/pumpkinblur");
        copy("misc/shadow.png", "misc/shadow");
        copy("misc/water.png", "misc/underwater");
        copy("misc/mapbg.png", "map/map_background");
        Raster markers = image("misc/mapicons.png");
        if (markers != null) {
            texture("map/decorations/player", markers.crop(0, 0, 8, 8));
        }
    }

    private void names() throws IOException {
        byte[] data = beta.read("lang/en_US.lang");
        if (data == null) {
            missing.add("lang/en_US.lang");
        } else {
            Map<String, String> betaText = new HashMap<>();
            for (String line : new String(data, StandardCharsets.UTF_8).split("\\R")) {
                int equals = line.indexOf('=');
                if (equals > 0 && !line.startsWith("#")) {
                    betaText.put(line.substring(0, equals), line.substring(equals + 1));
                }
            }
            for (String entry : NAMES) {
                String[] keys = entry.split(" ");
                String text = betaText.get(keys[1]);
                if (text != null) {
                    lang.put(keys[0], text);
                }
            }
            for (Map.Entry<String, String> entry : betaText.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("selectWorld.") || key.startsWith("options.") || key.startsWith("gui.")
                        || key.startsWith("controls.") || key.startsWith("performance.")) {
                    lang.put("b173." + key, entry.getValue());
                }
            }
        }
        StringBuilder json = new StringBuilder("{\n");
        int i = 0;
        for (Map.Entry<String, String> entry : lang.entrySet()) {
            json.append("  ").append(jsonString(entry.getKey())).append(": ").append(jsonString(entry.getValue()))
                    .append(++i < lang.size() ? ",\n" : "\n");
        }
        text("assets/minecraft/lang/en_us.json", json.append("}\n").toString());
    }

    private static String jsonString(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }

    private Raster image(String path) throws IOException {
        if (images.containsKey(path)) {
            return images.get(path);
        }
        byte[] data = beta.read(path);
        Raster image = null;
        if (data == null) {
            missing.add(path);
        } else {
            image = Png.read(data);
        }
        images.put(path, image);
        return image;
    }

    private void copy(String betaPath, String texture) throws IOException {
        Raster image = image(betaPath);
        if (image != null) {
            texture(texture, image);
        }
    }

    private void texture(String name, Raster image) throws IOException {
        texture("minecraft", name, image);
    }

    private void texture(String namespace, String name, Raster image) throws IOException {
        raw("assets/" + namespace + "/textures/" + name + ".png", Png.write(image));
    }

    private void meta(String name, String json) throws IOException {
        text(TEXTURES + name + ".png.mcmeta", json + "\n");
    }

    private void text(String path, String content) throws IOException {
        raw(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private void raw(String path, byte[] data) throws IOException {
        sink.write(path, data);
        files++;
    }
}
