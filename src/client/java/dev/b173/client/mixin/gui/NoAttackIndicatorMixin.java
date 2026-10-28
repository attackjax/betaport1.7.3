package dev.b173.client.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.b173.rules.BetaRules;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
abstract class NoAttackIndicatorMixin {
    @ModifyExpressionValue(method = {"extractCrosshair", "extractItemHotbar"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    private Object b173$noAttackIndicator(Object value) {
        return BetaRules.clientInBetaWorld ? AttackIndicatorStatus.OFF : value;
    }
}
