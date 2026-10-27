package dev.b173.core;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class RecipeParityTest {
    private static final long ORACLE_HASH = 0x1d94aa1bfeb31496L;
    private static final int ORACLE_LINES = 180;

    private RecipeParityTest() {
    }

    public static void run(TestRunner t) {
        t.section("Recipes against Beta 1.7.3's CraftingManager and furnace (oracle hash)");
        long hash = 0xCBF29CE484222325L;
        for (String line : BetaRecipes.LINES) {
            for (byte b : (line + "\n").getBytes(StandardCharsets.UTF_8)) {
                hash = (hash ^ (b & 255)) * 0x100000001B3L;
            }
        }
        t.check("180 lines as Beta listed them (151 crafting, 10 smelting, 19 fuel)",
                hash == ORACLE_HASH && BetaRecipes.LINES.length == ORACLE_LINES,
                String.format("hash %016x, %d lines", hash, BetaRecipes.LINES.length));

        int unknown = 0;
        StringBuilder missing = new StringBuilder();
        for (String line : BetaRecipes.LINES) {
            String[] parts = line.split(" ");
            if (parts[0].equals("fuel")) {
                continue;
            }
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i];
                if (!part.contains(":")) {
                    continue;
                }
                int id = Integer.parseInt(part.substring(0, part.indexOf(':')));
                int star = part.indexOf('*');
                int damage = Integer.parseInt(part.substring(part.indexOf(':') + 1, star < 0 ? part.length() : star));
                boolean known = star >= 0 ? BetaItems.result(id, damage) != null : BetaItems.ingredient(id, damage) != null;
                if (!known) {
                    unknown++;
                    missing.append(part).append(' ');
                }
            }
        }
        t.check("every Beta item in them has a modern stand-in", unknown == 0, missing.toString());

        List<BetaRecipes.Modern> modern = BetaRecipes.modern();
        Map<String, Integer> types = new TreeMap<>();
        for (BetaRecipes.Modern recipe : modern) {
            types.merge(recipe.type(), 1, Integer::sum);
        }
        t.check("147 crafting recipes (four of chain armour need fire) and 10 smelting",
                types.getOrDefault("crafting_shaped", 0) == 116 && types.getOrDefault("crafting_shapeless", 0) == 31
                        && types.getOrDefault("smelting", 0) == 10, types.toString());
        boolean chain = modern.stream().anyMatch(r -> r.path().startsWith("chainmail"));
        t.check("no chain armour recipe", !chain);
        long distinct = modern.stream().map(BetaRecipes.Modern::path).distinct().count();
        t.check("every recipe has its own name", distinct == modern.size(), distinct + " of " + modern.size());

        Map<String, Integer> fuel = BetaRecipes.fuel();
        boolean fuelOk = fuel.get("minecraft:coal") == 1600 && fuel.get("minecraft:charcoal") == 1600
                && fuel.get("minecraft:lava_bucket") == 20000 && fuel.get("minecraft:stick") == 100
                && fuel.get("minecraft:birch_sapling") == 100 && fuel.get("minecraft:spruce_log") == 300
                && fuel.get("minecraft:oak_trapdoor") == 300 && !fuel.containsKey("minecraft:oak_slab")
                && !fuel.containsKey("minecraft:wooden_pickaxe") && !fuel.containsKey("minecraft:ladder")
                && fuel.size() == 22;
        t.check("Beta's fuels: wooden blocks 300, sticks and saplings 100, coal 1600, lava 20000, nothing else",
                fuelOk, fuel.toString());
    }

    public static void main(String[] args) {
        for (BetaRecipes.Modern recipe : BetaRecipes.modern()) {
            System.out.println(recipe.path() + " " + recipe.json());
        }
        System.out.println(BetaRecipes.fuel());
    }
}
