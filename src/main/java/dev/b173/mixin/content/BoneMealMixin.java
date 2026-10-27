package dev.b173.mixin.content;

import dev.b173.content.BetaMechanics;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
abstract class BoneMealMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void b173$betaBoneMeal(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        cir.setReturnValue(BetaMechanics.boneMeal(context));
    }
}
