package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
abstract class GuiGraphicsExtractorMixin {
    @Inject(method = "itemBar", at = @At("HEAD"), cancellable = true)
    private void b173$betaDurability(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (BetaScreens.active() && stack.isDamageableItem() && stack.isDamaged()) {
            ci.cancel();
            BetaScreens.durabilityBar((GuiGraphicsExtractor) (Object) this, stack, x, y);
        }
    }
}
