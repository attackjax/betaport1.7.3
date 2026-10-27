package dev.b173.content;

import dev.b173.core.BetaContent;
import dev.b173.core.BetaMining;
import dev.b173.world.BetaBlockStates;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BetaDigging {
    private static final Map<String, Integer> ITEM_IDS = new HashMap<>();
    private static final Map<Item, Integer> BY_ITEM = new ConcurrentHashMap<>();

    static {
        for (String line : BetaContent.ITEMS) {
            String[] parts = line.split(" ");
            ITEM_IDS.put(parts[1], Integer.parseInt(parts[0]));
        }
    }

    private BetaDigging() {
    }

    private static int blockId(BlockState state) {
        if (!Boolean.TRUE.equals(BetaLater.beta(state.getBlock()))) {
            return -1;
        }
        return BetaBlockStates.id(BetaBlockStates.beta(state));
    }

    public static float hardness(BlockState state, float modern) {
        int id = blockId(state);
        return id > 0 ? BetaMining.hardness(id) : modern;
    }

    public static float strength(ItemStack stack, BlockState state, float modern) {
        int block = blockId(state);
        if (block <= 0) {
            return modern;
        }
        int item = stack.isEmpty() ? 0 : BY_ITEM.computeIfAbsent(stack.getItem(), it -> {
            Identifier id = BuiltInRegistries.ITEM.getKey(it);
            Integer beta = id.getNamespace().equals("minecraft") ? ITEM_IDS.get(id.getPath()) : null;
            return beta == null ? 0 : beta;
        });
        return BetaMining.strength(item, block);
    }
}
