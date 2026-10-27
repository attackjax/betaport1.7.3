package dev.b173.mixin.content;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseContainerBlockEntity.class)
public interface ContainerItemsAccess {
    @Invoker("getItems")
    NonNullList<ItemStack> b173$items();
}
