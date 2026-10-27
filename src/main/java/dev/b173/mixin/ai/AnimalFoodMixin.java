package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.b173.rules.BetaMobs;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Animal.class)
public abstract class AnimalFoodMixin {
    @ModifyExpressionValue(method = "mobInteract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/Animal;isFood(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean b173$noBreeding(boolean food) {
        Animal self = (Animal) (Object) this;
        return food && !(BetaMobs.isBeta(self.getType()) && BetaRules.betaWorld(self.level()));
    }
}
