package dev.b173.client.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screen.class)
abstract class ScreenMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @WrapOperation(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;extractPanorama(Lnet/minecraft/client/gui/GuiGraphicsExtractor;F)V"))
    private void b173$noPanorama(Screen screen, GuiGraphicsExtractor graphics, float partialTick, Operation<Void> original) {
        if (!BetaScreens.active()) {
            original.call(screen, graphics, partialTick);
        }
    }

    @WrapOperation(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;extractBlurredBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void b173$noBlur(Screen screen, GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!BetaScreens.active()) {
            original.call(screen, graphics);
        }
    }

    @WrapOperation(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;extractMenuBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void b173$betaBackground(Screen screen, GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (BetaScreens.active() && this.b173$inWorld()) {
            screen.extractTransparentBackground(graphics);
        } else {
            original.call(screen, graphics);
        }
    }

    @Unique
    private boolean b173$inWorld() {
        return this.minecraft != null && this.minecraft.level != null;
    }
}
