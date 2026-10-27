package dev.b173.rules;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.b173.core.BetaBlockDrops.Rule;
import dev.b173.core.BetaItems;

public final class BetaBlockDrops {
    private static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));

    private static final Map<Block, int[]> BETA = new IdentityHashMap<>();
    private static final Map<Block, Rule> RULES = new IdentityHashMap<>();

    static {
        beta(Blocks.GOLD_ORE, 14, 0);
        beta(Blocks.DEEPSLATE_GOLD_ORE, 14, 0);
        beta(Blocks.IRON_ORE, 15, 0);
        beta(Blocks.DEEPSLATE_IRON_ORE, 15, 0);
        beta(Blocks.COAL_ORE, 16, 0);
        beta(Blocks.DEEPSLATE_COAL_ORE, 16, 0);
        beta(Blocks.OAK_LEAVES, 18, 0);
        beta(Blocks.SPRUCE_LEAVES, 18, 1);
        beta(Blocks.BIRCH_LEAVES, 18, 2);
        beta(Blocks.LAPIS_ORE, 21, 0);
        beta(Blocks.DEEPSLATE_LAPIS_ORE, 21, 0);
        beta(Blocks.COBWEB, 30, 0);
        beta(Blocks.SHORT_GRASS, 31, 1);
        beta(Blocks.FERN, 31, 2);
        beta(Blocks.DEAD_BUSH, 32, 0);
        beta(Blocks.TNT, 46, 0);
        beta(Blocks.BOOKSHELF, 47, 0);
        beta(Blocks.OAK_STAIRS, 53, 0);
        beta(Blocks.DIAMOND_ORE, 56, 0);
        beta(Blocks.DEEPSLATE_DIAMOND_ORE, 56, 0);
        beta(Blocks.WHEAT, 59, 0);
        beta(Blocks.COBBLESTONE_STAIRS, 67, 0);
        beta(Blocks.REDSTONE_ORE, 73, 0);
        beta(Blocks.DEEPSLATE_REDSTONE_ORE, 73, 0);
    }

    private static final Set<Block> PICKAXE_ONLY = Set.of(Blocks.SMOOTH_STONE_SLAB, Blocks.SANDSTONE_SLAB,
            Blocks.OAK_SLAB, Blocks.COBBLESTONE_SLAB, Blocks.STONE_PRESSURE_PLATE, Blocks.IRON_DOOR, Blocks.GLOWSTONE);

    private BetaBlockDrops() {
    }

    private static void beta(Block block, int id, int meta) {
        BETA.put(block, new int[] {id, meta});
        RULES.put(block, dev.b173.core.BetaBlockDrops.rule(id));
    }

    public static List<ItemStack> drops(BlockState state, LootParams.Builder params, List<ItemStack> drops) {
        ServerLevel level = params.getLevel();
        if (!BetaRules.betaWorld(level)) {
            return drops;
        }
        Float radius = params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
        float chance = radius == null ? 1.0F : 1.0F / radius;
        RandomSource random = level.getRandom();
        Block block = state.getBlock();
        Rule rule = RULES.get(block);
        if (rule != null) {
            return beta(state, BETA.get(block), rule, params, chance, random);
        }
        if (isOre(state) && block.asItem() != Items.AIR) {
            return kept(block.asItem(), 1, chance, random);
        }
        return drops;
    }

    private static boolean isOre(BlockState state) {
        return state.is(ORES) || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.IRON_ORES) || state.is(Blocks.NETHER_QUARTZ_ORE)
                || state.is(Blocks.ANCIENT_DEBRIS);
    }

    private static List<ItemStack> beta(BlockState state, int[] beta, Rule rule, LootParams.Builder params,
                                        float chance, RandomSource random) {
        if (rule.none()) {
            return List.of();
        }
        if (rule.crops()) {
            int[] got = dev.b173.core.BetaBlockDrops.crops(state.getValue(CropBlock.AGE), random::nextInt);
            List<ItemStack> drops = new ArrayList<>(kept(Items.WHEAT, got[0], chance, random));
            if (got[1] > 0) {
                drops.add(new ItemStack(Items.WHEAT_SEEDS, got[1]));
            }
            return drops;
        }
        if (beta[0] == 18) {
            ItemInstance tool = params.getOptionalParameter(LootContextParams.TOOL);
            if (tool != null && tool.is(Items.SHEARS)) {
                return List.of(new ItemStack(state.getBlock()));
            }
        }
        Item item = rule.self() ? state.getBlock().asItem() : item(rule, beta[1]);
        return kept(item, rule.roll(random::nextInt), chance, random);
    }

    private static List<ItemStack> kept(Item item, int count, float chance, RandomSource random) {
        int kept = 0;
        for (int i = 0; i < count; i++) {
            if (chance >= 1.0F || random.nextFloat() <= chance) {
                kept++;
            }
        }
        return kept == 0 ? List.of() : List.of(new ItemStack(item, kept));
    }

    private static Item item(Rule rule, int meta) {
        String name = BetaItems.result(Integer.parseInt(rule.item()), rule.damage() < 0 ? meta : rule.damage());
        return BuiltInRegistries.ITEM.getValue(Identifier.parse(name));
    }

    public static boolean harvests(Player player, BlockState state, boolean harvests) {
        if (!PICKAXE_ONLY.contains(state.getBlock()) || !BetaRules.betaWorld(player.level())) {
            return harvests;
        }
        return player.getMainHandItem().is(ItemTags.PICKAXES);
    }
}
