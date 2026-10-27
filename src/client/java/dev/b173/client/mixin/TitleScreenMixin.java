package dev.b173.client.mixin;

import dev.b173.client.visual.BetaPack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void b173$betaButtons(CallbackInfo callback) {
        if (!BetaPack.active()) {
            return;
        }
        for (GuiEventListener child : List.copyOf(children())) {
            if (child instanceof SpriteIconButton) {
                removeWidget(child);
            } else if (child instanceof Button button && button.getMessage().getContents() instanceof TranslatableContents key
                    && key.getKey().equals("menu.online")) {
                removeWidget(button);
                addRenderableWidget(Button.builder(Component.literal("Mods and Texture Packs"), pressed -> openPacks())
                        .bounds(button.getX(), button.getY(), button.getWidth(), button.getHeight()).build());
            }
        }
    }

    private void openPacks() {
        Screen title = this;
        minecraft.setScreen(new PackSelectionScreen(minecraft.getResourcePackRepository(), repository -> {
            minecraft.options.updateResourcePacks(repository);
            minecraft.setScreen(title);
        }, minecraft.getResourcePackDirectory(), Component.translatable("resourcePack.title")));
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/TitleScreen;extractPanorama(Lnet/minecraft/client/gui/GuiGraphicsExtractor;F)V"))
    private void b173$dirt(TitleScreen screen, GuiGraphicsExtractor graphics, float partialTick) {
        if (BetaPack.active()) {
            extractMenuBackground(graphics);
        } else {
            extractPanorama(graphics, partialTick);
        }
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/SplashRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;ILnet/minecraft/client/gui/Font;F)V"))
    private void b173$splash(SplashRenderer splash, GuiGraphicsExtractor graphics, int width, Font font, float alpha) {
        if (!BetaPack.active()) {
            splash.extractRenderState(graphics, width, font, alpha);
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(-33.0F, 1.0F);
        splash.extractRenderState(graphics, width, font, alpha);
        graphics.pose().popMatrix();
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"))
    private void b173$version(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
        if (BetaPack.active()) {
            graphics.text(font, "Minecraft Beta 1.7.3", 2, 2, color & 0xFF000000 | 0x505050);
        } else {
            graphics.text(font, text, x, y, color);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void b173$setupNotice(CallbackInfo callback) {
        dev.b173.client.gui.BetaSetupNotice.maybeShow(minecraft, this);
    }

    @Inject(method = "realmsNotificationsEnabled", at = @At("HEAD"), cancellable = true)
    private void b173$noRealms(CallbackInfoReturnable<Boolean> callback) {
        if (BetaPack.active()) {
            callback.setReturnValue(false);
        }
    }
}
