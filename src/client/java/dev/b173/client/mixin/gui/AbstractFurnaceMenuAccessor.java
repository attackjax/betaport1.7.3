package dev.b173.client.mixin.gui;

import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceMenu.class)
public interface AbstractFurnaceMenuAccessor {
    @Accessor("data")
    ContainerData b173$data();
}
