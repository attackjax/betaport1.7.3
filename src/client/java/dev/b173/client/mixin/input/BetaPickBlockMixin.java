package dev.b173.client.mixin.input;

import dev.b173.core.BetaContent;
import dev.b173.core.BetaPick;
import dev.b173.rules.BetaRules;
import dev.b173.world.BetaBlockStates;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Minecraft.class)
abstract class BetaPickBlockMixin {
    @Unique
    private static Map<String, Integer> b173$items;

    @Unique
    private static Map<String, Integer> b173$blocks;

    @Unique
    private static Map<String, Integer> b173$table(String[] lines) {
        Map<String, Integer> map = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split(" ");
            int id = Integer.parseInt(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                map.putIfAbsent(parts[i], id);
            }
        }
        return map;
    }

    @Unique
    private static int b173$betaId(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (b173$items == null) {
            b173$items = b173$table(BetaContent.ITEMS);
            b173$blocks = b173$table(BetaContent.BLOCKS);
        }
        Integer id = b173$items.get(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
        if (id == null && stack.getItem() instanceof BlockItem block) {
            id = b173$blocks.get(BuiltInRegistries.BLOCK.getKey(block.getBlock()).getPath());
        }
        return id == null ? -1 : id;
    }

    @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
    private void b173$betaPick(CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;
        if (!BetaRules.clientInBetaWorld || minecraft.player == null || minecraft.level == null) {
            return;
        }
        ci.cancel();
        HitResult hit = minecraft.hitResult;
        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            return;
        }
        int blockId = hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK
                ? BetaBlockStates.id(BetaBlockStates.beta(minecraft.level.getBlockState(block.getBlockPos()))) : 7;
        Inventory inventory = minecraft.player.getInventory();
        int[] ids = new int[Inventory.INVENTORY_SIZE];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = b173$betaId(inventory.getItem(i));
        }
        int slot = BetaPick.slot(ids, blockId);
        if (slot >= 0) {
            inventory.setSelectedSlot(slot);
        }
    }
}
