package dev.b173.client.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceScreen.class)
abstract class AbstractFurnaceScreenMixin {
    @Unique
    private static final String BLIT_SPRITE = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V";

    @WrapOperation(method = "extractBackground", at = @At(value = "INVOKE", target = BLIT_SPRITE, ordinal = 0))
    private void b173$betaFlame(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                int spriteWidth, int spriteHeight, int u, int v, int x, int y, int width, int height,
                                Operation<Void> original) {
        ContainerData data = this.b173$betaData();
        if (data == null) {
            original.call(graphics, pipeline, sprite, spriteWidth, spriteHeight, u, v, x, y, width, height);
            return;
        }
        int flame = BetaScreens.flameHeight(data);
        original.call(graphics, pipeline, sprite, spriteWidth, spriteHeight, u, spriteHeight - flame, x,
                y + height - flame, width, flame);
    }

    @WrapOperation(method = "extractBackground", at = @At(value = "INVOKE", target = BLIT_SPRITE, ordinal = 1))
    private void b173$betaArrow(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                int spriteWidth, int spriteHeight, int u, int v, int x, int y, int width, int height,
                                Operation<Void> original) {
        ContainerData data = this.b173$betaData();
        original.call(graphics, pipeline, sprite, spriteWidth, spriteHeight, u, v, x, y,
                data == null ? width : BetaScreens.arrowWidth(data), height);
    }

    @Unique
    private ContainerData b173$betaData() {
        if (!BetaScreens.active() || !((Object) this instanceof FurnaceScreen)) {
            return null;
        }
        return ((AbstractFurnaceMenuAccessor) ((AbstractContainerScreen<?>) (Object) this).getMenu()).b173$data();
    }
}
