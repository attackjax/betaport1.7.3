package dev.b173.client.mixin.gui;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("titleLabelX")
    void b173$setTitleLabelX(int x);

    @Accessor("titleLabelY")
    void b173$setTitleLabelY(int y);

    @Accessor("leftPos")
    int b173$leftPos();

    @Accessor("topPos")
    int b173$topPos();
}
