package dev.b173.mixin.ai;

import dev.b173.rules.BetaCombat;
import dev.b173.rules.ai.BetaBrain;
import dev.b173.rules.ai.BetaBrains;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityRevengeMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void b173$betaAge(ServerLevel level, DamageSource source, float amount,
                              CallbackInfoReturnable<Boolean> callback) {
        BetaBrain brain = BetaBrains.brainOf((LivingEntity) (Object) this);
        if (brain != null) {
            brain.resetAge();
            brain.hurtAttempted(BetaCombat.betaAttacker(source));
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void b173$betaRevenge(ServerLevel level, DamageSource source, float amount,
                                  CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ()) {
            BetaBrain brain = BetaBrains.brainOf((LivingEntity) (Object) this);
            if (brain != null) {
                brain.hurtBy(BetaCombat.betaAttacker(source));
            }
        }
    }
}
