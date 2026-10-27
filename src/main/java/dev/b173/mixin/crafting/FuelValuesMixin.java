package dev.b173.mixin.crafting;

import dev.b173.rules.BetaCrafting;
import dev.b173.rules.BetaRules;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.SequencedSet;

@Mixin(FuelValues.class)
abstract class FuelValuesMixin {
    @Inject(method = "burnDuration", at = @At("HEAD"), cancellable = true)
    private void b173$betaBurnTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (BetaRules.betaGame()) {
            cir.setReturnValue(BetaCrafting.burnDuration(stack));
        }
    }

    @Inject(method = "isFuel", at = @At("HEAD"), cancellable = true)
    private void b173$betaFuel(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (BetaRules.betaGame()) {
            cir.setReturnValue(BetaCrafting.burnDuration(stack) > 0);
        }
    }

    @Inject(method = "fuelItems", at = @At("HEAD"), cancellable = true)
    private void b173$betaFuelItems(CallbackInfoReturnable<SequencedSet<Item>> cir) {
        if (BetaRules.betaGame()) {
            cir.setReturnValue(BetaCrafting.fuelItems());
        }
    }
}
