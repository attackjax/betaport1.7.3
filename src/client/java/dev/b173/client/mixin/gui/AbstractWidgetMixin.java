package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractWidget.class)
abstract class AbstractWidgetMixin {
    @ModifyVariable(method = "extractScrollingStringOverContents", at = @At("HEAD"), argsOnly = true)
    private Component b173$betaButtonText(Component message) {
        AbstractWidget widget = (AbstractWidget) (Object) this;
        if (!widget.active || !(widget instanceof AbstractButton || widget instanceof AbstractSliderButton)
                || !BetaScreens.active()) {
            return message;
        }
        return ComponentUtils.mergeStyles(message, Style.EMPTY.withColor(widget.isHoveredOrFocused() ? 0xFFFFA0 : 0xE0E0E0));
    }
}
