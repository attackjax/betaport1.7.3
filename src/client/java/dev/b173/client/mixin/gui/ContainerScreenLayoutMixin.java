package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InventoryScreen.class, CraftingScreen.class, AbstractFurnaceScreen.class, DispenserScreen.class})
abstract class ContainerScreenLayoutMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void b173$betaLayout(CallbackInfo ci) {
        BetaScreens.layOut((AbstractContainerScreen<?>) (Object) this);
    }
}
