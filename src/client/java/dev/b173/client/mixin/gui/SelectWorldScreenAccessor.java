package dev.b173.client.mixin.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SelectWorldScreen.class)
public interface SelectWorldScreenAccessor {
    @Accessor("lastScreen")
    Screen b173$lastScreen();
}
