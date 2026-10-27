package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.content.BetaDigging;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
abstract class DiggingSpeedMixin {
    @ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
    private float b173$betaStrength(float modern, BlockState state) {
        return BetaDigging.strength((ItemStack) (Object) this, state, modern);
    }
}
