package dev.b173.mixin.ai;

import dev.b173.rules.BetaRules;
import dev.b173.rules.ai.BetaBrains;
import dev.b173.rules.ai.BetaDespawn;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobDespawnMixin {
    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void b173$betaDespawn(CallbackInfo callback) {
        Mob self = (Mob) (Object) this;
        boolean brained = BetaBrains.brainOf(self) != null;
        if (!brained && !(BetaDespawn.ageless(self.getType()) && BetaRules.betaWorld(self.level()))) {
            return;
        }
        callback.cancel();
        if (self.level().getDifficulty() == Difficulty.PEACEFUL && !self.getType().isAllowedInPeaceful()
                && self.getType() != EntityType.SLIME) {
            self.discard();
        } else if (!brained) {
            BetaDespawn.checkAgeless(self);
        }
    }
}
