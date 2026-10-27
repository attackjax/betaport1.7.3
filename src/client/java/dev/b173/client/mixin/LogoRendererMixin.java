package dev.b173.client.mixin;

import dev.b173.client.visual.BetaPack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoRenderer.class)
public abstract class LogoRendererMixin {
    @Unique
    private static final Identifier BETA_LOGO = Identifier.fromNamespaceAndPath("b173", "textures/gui/title/mclogo.png");

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFI)V", at = @At("HEAD"),
            cancellable = true)
    private void b173$betaLogo(GuiGraphicsExtractor graphics, int screenWidth, float alpha, int y, CallbackInfo callback) {
        if (!BetaPack.active()) {
            return;
        }
        int x = screenWidth / 2 - 137;
        int color = ARGB.white(alpha);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BETA_LOGO, x, y, 0.0F, 0.0F, 155, 44, 256, 256, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BETA_LOGO, x + 155, y, 0.0F, 45.0F, 155, 44, 256, 256, color);
        callback.cancel();
    }
}
