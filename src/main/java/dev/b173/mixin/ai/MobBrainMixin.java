package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.b173.rules.ai.BetaBrain;
import dev.b173.rules.ai.BetaBrainHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobBrainMixin implements BetaBrainHolder {
    @Unique
    private BetaBrain b173$brain;

    @Override
    public BetaBrain b173$brain() {
        return b173$brain;
    }

    @Override
    public void b173$setBrain(BetaBrain brain) {
        b173$brain = brain;
    }

    @ModifyReturnValue(method = "getControllingPassenger", at = @At("RETURN"))
    private LivingEntity b173$betaMount(LivingEntity controller) {
        return b173$brain != null ? null : controller;
    }

    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;burnUndead()V"))
    private boolean b173$betaSunburn(Mob self) {
        return b173$brain == null;
    }
}
