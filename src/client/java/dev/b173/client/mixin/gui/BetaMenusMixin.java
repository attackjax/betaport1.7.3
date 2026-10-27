package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaOptionsScreen;
import dev.b173.client.gui.BetaScreens;
import dev.b173.client.gui.BetaSelectWorldScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
abstract class BetaMenusMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen b173$betaMenus(Screen screen) {
        if (!BetaScreens.active()) {
            return screen;
        }
        Minecraft minecraft = (Minecraft) (Object) this;
        if (screen instanceof SelectWorldScreen select) {
            return new BetaSelectWorldScreen(((SelectWorldScreenAccessor) select).b173$lastScreen());
        }
        if (screen instanceof OptionsScreen options) {
            return new BetaOptionsScreen(options.getLastScreen(), minecraft.options);
        }
        return screen;
    }
}
