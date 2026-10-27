package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Creeper.class)
public abstract class CreeperIgniteMixin {
    @ModifyExpressionValue(method = "mobInteract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean b173$noLighting(boolean igniter) {
        return igniter && !BetaRules.betaWorld(((Creeper) (Object) this).level());
    }
}
