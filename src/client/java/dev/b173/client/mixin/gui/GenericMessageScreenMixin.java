package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaLoading;
import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericMessageScreen.class)
abstract class GenericMessageScreenMixin extends Screen {
    private GenericMessageScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private String[] b173$beta() {
        if (!BetaScreens.active()) {
            return null;
        }
        String key = this.title.getContents() instanceof TranslatableContents t ? t.getKey() : "";
        if (key.equals("menu.savingLevel")) {
            return new String[] {"", "Saving chunks"};
        }
        if (BetaLoading.opening() != null && (key.equals("selectWorld.data_read")
                || key.equals("selectWorld.resource_load") || key.equals("createWorld.preparing"))) {
            return new String[] {BetaLoading.opening(), ""};
        }
        if (key.equals("createWorld.preparing")) {
            return new String[] {"", ""};
        }
        return null;
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void b173$noText(CallbackInfo ci) {
        if (b173$beta() != null) {
            ci.cancel();
        }
    }

    @Inject(method = "repositionElements", at = @At("HEAD"), cancellable = true)
    private void b173$noTextMoved(CallbackInfo ci) {
        if (b173$beta() != null) {
            ci.cancel();
        }
    }

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void b173$betaLoading(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
                                  CallbackInfo ci) {
        String[] lines = b173$beta();
        if (lines != null) {
            ci.cancel();
            BetaLoading.draw(graphics, this.width, this.height, lines[0], lines[1], -1);
        }
    }
}
