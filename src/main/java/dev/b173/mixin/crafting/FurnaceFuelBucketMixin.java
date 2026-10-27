package dev.b173.mixin.crafting;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.b173.rules.BetaRules;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceBlockEntity.class)
abstract class FurnaceFuelBucketMixin {
    @ModifyExpressionValue(method = "consumeFuel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;getCraftingRemainder()Lnet/minecraft/world/item/ItemStackTemplate;"))
    private static ItemStackTemplate b173$keepNoBucket(ItemStackTemplate remainder) {
        return BetaRules.betaGame() ? null : remainder;
    }
}
