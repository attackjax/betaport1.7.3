package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaLoading;
import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
abstract class LevelLoadingScreenMixin extends Screen {
    @Shadow
    private LevelLoadTracker loadTracker;

    @Shadow
    private LevelLoadingScreen.Reason reason;

    private LevelLoadingScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void b173$noBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
                                   CallbackInfo ci) {
        if (BetaScreens.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void b173$betaLoading(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
                                  CallbackInfo ci) {
        if (!BetaScreens.active()) {
            return;
        }
        ci.cancel();
        String heading;
        if (this.reason == LevelLoadingScreen.Reason.NETHER_PORTAL) {
            boolean intoNether = this.minecraft.level != null && this.minecraft.level.dimension() == Level.NETHER;
            heading = intoNether ? "Entering the Nether" : "Leaving the Nether";
        } else if (BetaLoading.opening() != null) {
            heading = BetaLoading.opening();
        } else {
            heading = "Respawning";
        }
        boolean building = !this.loadTracker.hasProgress() || this.loadTracker.serverProgress() < 1.0F;
        if (building) {
            int percent = this.loadTracker.hasProgress()
                    ? Math.min(99, (int) (this.loadTracker.serverProgress() * 100.0F)) : 0;
            BetaLoading.draw(graphics, this.width, this.height, heading, "Building terrain", percent);
        } else {
            BetaLoading.draw(graphics, this.width, this.height, heading, "Simulating world for a bit", -1);
        }
    }
}
