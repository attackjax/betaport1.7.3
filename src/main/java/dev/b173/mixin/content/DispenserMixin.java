package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DispenserBlock.class)
abstract class DispenserMixin {
    @Unique
    private static final DispenseItemBehavior B173_DROP = new DefaultDispenseItemBehavior();

    @ModifyReturnValue(method = "getDispenseMethod", at = @At("RETURN"))
    private DispenseItemBehavior b173$betaDispense(DispenseItemBehavior behavior, Level level, ItemStack stack) {
        if (stack.is(Items.ARROW) || stack.is(Items.EGG) || stack.is(Items.SNOWBALL)) {
            return behavior;
        }
        return B173_DROP;
    }
}
