package dev.b173.content;

import dev.b173.core.BetaContent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public final class BetaLater {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("b173", "later");

    public static FeatureFlag flag;
    private static FeatureFlagSet set;

    private BetaLater() {
    }

    public static FeatureFlagSet set() {
        FeatureFlagSet result = set;
        if (result == null) {
            result = FeatureFlagSet.of(flag);
            set = result;
        }
        return result;
    }

    public static Boolean beta(Block block) {
        Optional<ResourceKey<Block>> key = BuiltInRegistries.BLOCK.getResourceKey(block);
        if (key.isEmpty()) {
            return null;
        }
        Identifier id = key.get().identifier();
        return id.getNamespace().equals("minecraft") && BetaContent.blocks().contains(id.getPath());
    }

    public static Boolean beta(Item item) {
        Optional<ResourceKey<Item>> key = BuiltInRegistries.ITEM.getResourceKey(item);
        if (key.isEmpty()) {
            return null;
        }
        Identifier id = key.get().identifier();
        if (!id.getNamespace().equals("minecraft")) {
            return false;
        }
        if (id.getPath().equals("air") || BetaContent.items().contains(id.getPath())) {
            return true;
        }
        if (item instanceof BlockItem blockItem) {
            Boolean block = beta(blockItem.getBlock());
            return block != null && block;
        }
        return false;
    }

    public static Boolean beta(EntityType<?> type) {
        Optional<ResourceKey<EntityType<?>>> key = BuiltInRegistries.ENTITY_TYPE.getResourceKey(type);
        if (key.isEmpty()) {
            return null;
        }
        Identifier id = key.get().identifier();
        return id.getNamespace().equals("minecraft") && BetaContent.entities().contains(id.getPath());
    }
}
