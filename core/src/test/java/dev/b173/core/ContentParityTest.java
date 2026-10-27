package dev.b173.core;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public final class ContentParityTest {
    private static final long ORACLE_HASH = 0xb670a7f63afcd2d8L;

    private ContentParityTest() {
    }

    public static void run(TestRunner t) {
        t.section("Beta's blocks, items and entities (oracle hash) and their stand-ins");
        long hash = 0xCBF29CE484222325L;
        int blocks = 0;
        int items = 0;
        int entities = 0;
        for (String line : BetaContent.LINES) {
            for (byte b : (line + "\n").getBytes(StandardCharsets.UTF_8)) {
                hash = (hash ^ (b & 255)) * 0x100000001B3L;
            }
            blocks += line.startsWith("block ") ? 1 : 0;
            items += line.startsWith("item ") ? 1 : 0;
            entities += line.startsWith("entity ") ? 1 : 0;
        }
        String opaque = "opaque";
        String normal = "normal";
        for (int id = 1; id < 256; id++) {
            opaque += BetaBlocks.isOpaqueCube(id) ? " " + id : "";
            normal += BetaBlocks.isNormalCube(id) ? " " + id : "";
        }
        String oracleOpaque = "";
        String oracleNormal = "";
        for (String line : BetaContent.LINES) {
            oracleOpaque = line.startsWith("opaque") ? line : oracleOpaque;
            oracleNormal = line.startsWith("normal") ? line : oracleNormal;
        }
        t.check("the core's opaque-cube and normal-cube tables are Beta's",
                opaque.equals(oracleOpaque) && normal.equals(oracleNormal), opaque + " / " + normal);
        t.check("228 lines as Beta listed them (96 blocks, 106 items, 24 entities, opaque and normal cubes)",
                hash == ORACLE_HASH && blocks == 96 && items == 106 && entities == 24,
                String.format("hash %016x, %d blocks, %d items, %d entities", hash, blocks, items, entities));

        Set<String> blockIds = ids(BetaContent.BLOCKS);
        Set<String> itemIds = ids(BetaContent.ITEMS);
        Set<String> entityNames = ids(BetaContent.ENTITIES);
        StringBuilder missing = new StringBuilder();
        for (String line : BetaContent.LINES) {
            String[] parts = line.split(" ");
            if (parts[0].equals("opaque") || parts[0].equals("normal")) {
                continue;
            }
            boolean has = switch (parts[0]) {
                case "block" -> blockIds.contains(parts[1]) || Integer.parseInt(parts[1]) == BetaContent.LOCKED_CHEST;
                case "item" -> itemIds.contains(parts[1]);
                default -> entityNames.contains(parts[2]) || parts[2].equals("Mob") || parts[2].equals("Monster");
            };
            if (!has) {
                missing.append(parts[0]).append(' ').append(parts[1]).append(' ').append(parts[2]).append("; ");
            }
        }
        t.check("every Beta block, item and entity has a stand-in (the locked chest has none)",
                missing.isEmpty(), missing.toString());
        t.check("no stand-in for a block, item or entity Beta never listed",
                blockIds.size() == 96 && itemIds.size() == 106 && entityNames.size() == 23,
                blockIds.size() + " blocks, " + itemIds.size() + " items, " + entityNames.size() + " entities");

        boolean conversions = BetaContent.convertBlock("birch_planks", false, false).equals("oak_planks")
                && BetaContent.convertBlock("stripped_spruce_log", false, false).equals("spruce_log")
                && BetaContent.convertBlock("jungle_log", false, false).equals("oak_log")
                && BetaContent.convertBlock("blue_bed", false, false).equals("red_bed")
                && BetaContent.convertBlock("deepslate_iron_ore", false, false).equals("iron_ore")
                && BetaContent.convertBlock("tall_grass", false, false).equals("short_grass")
                && BetaContent.convertBlock("tall_grass", false, true).equals("air")
                && BetaContent.convertBlock("copper_ore", false, false).equals("air")
                && BetaContent.convertBlock("oak_fence_gate", true, false).equals("water")
                && BetaContent.convertBlock("stone", false, false).equals("stone")
                && BetaContent.convertBlock("smooth_stone", false, false).equals("smooth_stone_slab[type=double]")
                && BetaContent.convertItem("raw_iron").equals("iron_ore")
                && BetaContent.convertItem("black_dye").equals("ink_sac")
                && BetaContent.convertItem("emerald") == null
                && BetaContent.convertItem("diamond").equals("diamond");
        t.check("later blocks and items become Beta's counterpart or go", conversions);
        for (String block : BetaContent.blocks()) {
            if (!BetaContent.convertBlock(block, false, false).equals(block)) {
                conversions = false;
            }
        }
        t.check("Beta's own blocks stay themselves", conversions);
    }

    private static Set<String> ids(String[] table) {
        Set<String> ids = new HashSet<>();
        for (String line : table) {
            ids.add(line.split(" ")[0]);
        }
        return ids;
    }
}
