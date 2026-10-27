package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaBlockDrops;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BlockBehaviour.class)
abstract class BlockDropsMixin {
    @ModifyReturnValue(method = "getDrops", at = @At("RETURN"))
    private List<ItemStack> b173$betaDrops(List<ItemStack> drops, BlockState state, LootParams.Builder params) {
        return BetaBlockDrops.drops(state, params, drops);
    }
}
