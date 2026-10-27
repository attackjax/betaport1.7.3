package dev.b173.mixin;

import dev.b173.rules.BetaHealthLeft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityHealthLeftMixin implements BetaHealthLeft {
    @Unique
    private float b173$healthLeft;

    @Inject(method = "setHealth", at = @At("HEAD"))
    private void b173$rememberHealthLeft(float health, CallbackInfo callback) {
        b173$healthLeft = health;
    }

    @Override
    public float b173$healthLeft() {
        return b173$healthLeft;
    }
}
