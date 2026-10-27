package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PauseScreen.class)
abstract class PauseScreenMixin extends Screen {
    @Shadow
    @Final
    private boolean showPauseMenu;

    @Unique
    private int b173$ticks;

    private PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void b173$betaMenu(CallbackInfo ci) {
        if (!this.showPauseMenu || !BetaScreens.active()) {
            return;
        }
        ci.cancel();
        Screen menu = this;
        int x = this.width / 2 - 100;
        int y = this.height / 4 - 16;
        this.addRenderableWidget(Button.builder(Component.literal(
                        this.minecraft.isLocalServer() ? "Save and quit to title" : "Disconnect"), button -> {
                    button.active = false;
                    this.minecraft.getReportingContext().draftReportHandled(this.minecraft, menu,
                            () -> this.minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE), true);
                })
                .bounds(x, y + 120, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Back to game"), button -> {
                    this.minecraft.setScreen(null);
                    this.minecraft.mouseHandler.grabMouse();
                })
                .bounds(x, y + 24, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Options..."),
                        button -> this.minecraft.setScreen(new OptionsScreen(menu, this.minecraft.options, true)))
                .bounds(x, y + 96, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Achievements"),
                        button -> this.minecraft.setScreen(
                                new AdvancementsScreen(this.minecraft.player.connection.getAdvancements(), menu)))
                .bounds(x, y + 48, 98, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.stats"),
                        button -> this.minecraft.setScreen(new StatsScreen(menu, this.minecraft.player.getStats())))
                .bounds(this.width / 2 + 2, y + 48, 98, 20).build());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void b173$count(CallbackInfo ci) {
        this.b173$ticks++;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void b173$betaText(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!this.showPauseMenu || !BetaScreens.active()) {
            return;
        }
        if (this.b173$ticks < 20) {
            float fraction = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
            float pulse = ((float) (this.b173$ticks % 10) + fraction) / 10.0F;
            pulse = Mth.sin(pulse * 3.141593F * 2.0F) * 0.2F + 0.8F;
            int shade = (int) (255.0F * pulse);
            graphics.text(this.font, "Saving level..", 8, this.height - 16, 0xFF000000 | shade << 16 | shade << 8 | shade);
        }
        graphics.centeredText(this.font, "Game menu", this.width / 2, 40, 0xFFFFFFFF);
    }

    @Inject(method = "rendersNowPlayingToast", at = @At("HEAD"), cancellable = true)
    private void b173$noNowPlaying(CallbackInfoReturnable<Boolean> cir) {
        if (BetaScreens.active()) {
            cir.setReturnValue(false);
        }
    }
}
