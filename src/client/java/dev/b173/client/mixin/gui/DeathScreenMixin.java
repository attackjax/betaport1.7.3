package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DeathScreen.class)
abstract class DeathScreenMixin extends Screen {
    @Shadow
    @Final
    private LocalPlayer player;

    @Shadow
    @Final
    private List<Button> exitButtons;

    @Shadow
    private Button exitToTitleButton;

    private DeathScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void b173$betaButtons(CallbackInfo ci) {
        if (!BetaScreens.active()) {
            return;
        }
        ci.cancel();
        this.exitButtons.clear();
        int x = this.width / 2 - 100;
        Button respawn = addRenderableWidget(Button.builder(Component.literal("Respawn"), button -> {
            this.player.respawn();
            button.active = false;
        }).bounds(x, this.height / 4 + 72, 200, 20).build());
        Button title = addRenderableWidget(Button.builder(Component.literal("Title menu"),
                button -> ((DeathScreenInvoker) this).b173$exitToTitle()).bounds(x, this.height / 4 + 96, 200, 20).build());
        this.exitButtons.add(respawn);
        this.exitButtons.add(title);
        this.exitToTitleButton = title;
        for (Button button : this.exitButtons) {
            button.active = false;
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void b173$betaWords(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
                                CallbackInfo ci) {
        if (!BetaScreens.active()) {
            return;
        }
        ci.cancel();
        graphics.pose().pushMatrix();
        graphics.pose().scale(2.0F, 2.0F);
        graphics.centeredText(this.font, "Game over!", this.width / 2 / 2, 30, 0xFFFFFFFF);
        graphics.pose().popMatrix();
        graphics.centeredText(this.font, "Score: §e" + this.player.getScore(), this.width / 2, 100, 0xFFFFFFFF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
}
