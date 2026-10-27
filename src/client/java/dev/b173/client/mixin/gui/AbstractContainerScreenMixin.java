package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin {
    @Inject(method = "getTooltipFromContainerItem", at = @At("HEAD"), cancellable = true)
    private void b173$betaTooltip(ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        if (BetaScreens.active()) {
            cir.setReturnValue(BetaScreens.tooltip(stack));
        }
    }
}
