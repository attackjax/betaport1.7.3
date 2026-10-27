package dev.b173.mixin.content;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ItemStack.class)
abstract class ItemStackSizeMixin {
    @Unique
    private static final Map<String, Integer> B173_STACKS = Map.ofEntries(
            Map.entry("apple", 1), Map.entry("bread", 1), Map.entry("porkchop", 1),
            Map.entry("cooked_porkchop", 1), Map.entry("cod", 1), Map.entry("cooked_cod", 1),
            Map.entry("golden_apple", 1), Map.entry("oak_sign", 1), Map.entry("oak_door", 1),
            Map.entry("iron_door", 1), Map.entry("bucket", 1), Map.entry("map", 1),
            Map.entry("filled_map", 1), Map.entry("cookie", 8));
    @Unique
    private static final Map<Item, Integer> B173_BY_ITEM = new ConcurrentHashMap<>();

    @Shadow
    public abstract Item getItem();

    public int getMaxStackSize() {
        int size = ((ItemStack) (Object) this).getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
        int beta = B173_BY_ITEM.computeIfAbsent(getItem(), item -> {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            Integer limit = id.getNamespace().equals("minecraft") ? B173_STACKS.get(id.getPath()) : null;
            return limit == null ? Integer.MAX_VALUE : limit;
        });
        return Math.min(size, beta);
    }
}
