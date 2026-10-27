package dev.b173.rules;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.b173.Beta173Mod;
import dev.b173.core.BetaRecipes;
import dev.b173.mixin.crafting.RecipeManagerInvoker;
import dev.b173.world.BetaChunkGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;

public final class BetaCrafting {
    private static volatile Map<Item, Integer> fuel;
    private static volatile SequencedSet<Item> fuelItems;

    private BetaCrafting() {
    }

    public static boolean betaWorld(HolderLookup.Provider registries) {
        return registries.lookup(Registries.LEVEL_STEM)
                .flatMap(stems -> stems.get(LevelStem.OVERWORLD))
                .map(stem -> stem.value().generator() instanceof BetaChunkGenerator)
                .orElse(false);
    }

    public static RecipeMap recipes(HolderLookup.Provider registries) {
        List<RecipeHolder<?>> holders = new ArrayList<>();
        for (BetaRecipes.Modern recipe : BetaRecipes.modern()) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Beta173Mod.id(recipe.path()));
            try {
                JsonObject json = JsonParser.parseString(recipe.json()).getAsJsonObject();
                holders.add(RecipeManagerInvoker.b173$fromJson(key, json, registries));
            } catch (RuntimeException e) {
                Beta173Mod.LOGGER.error("Beta recipe {} could not be read: {}", key.identifier(), e.getMessage());
            }
        }
        Beta173Mod.LOGGER.info("Beta 1.7.3 recipes in place of this game's: {}", holders.size());
        return RecipeMap.create(holders);
    }

    public static int burnDuration(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return fuel().getOrDefault(stack.getItem(), 0);
    }

    public static SequencedSet<Item> fuelItems() {
        fuel();
        return fuelItems;
    }

    private static Map<Item, Integer> fuel() {
        Map<Item, Integer> current = fuel;
        if (current == null) {
            Map<Item, Integer> map = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : BetaRecipes.fuel().entrySet()) {
                map.put(BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.getKey())), entry.getValue());
            }
            fuelItems = new LinkedHashSet<>(map.keySet());
            current = Map.copyOf(map);
            fuel = current;
        }
        return current;
    }
}
