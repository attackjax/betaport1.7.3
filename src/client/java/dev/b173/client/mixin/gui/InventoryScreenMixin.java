package dev.b173.client.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryScreen.class)
abstract class InventoryScreenMixin {
    @WrapOperation(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"))
    private void b173$betaPreview(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int scale, float yOffset,
                                  float mouseX, float mouseY, LivingEntity player, Operation<Void> original) {
        if (BetaScreens.active()) {
            AbstractContainerScreenAccessor screen = (AbstractContainerScreenAccessor) this;
            BetaScreens.inventoryPreview(graphics, screen.b173$leftPos(), screen.b173$topPos(), mouseX, mouseY, player);
        } else {
            original.call(graphics, x0, y0, x1, y1, scale, yOffset, mouseX, mouseY, player);
        }
    }
}
