package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
abstract class EntityPigmanFireMixin {
    @Shadow
    protected abstract boolean getSharedFlag(int flag);

    private boolean b173$betaPigman() {
        Entity self = (Entity) (Object) this;
        return self.getType() == EntityType.ZOMBIFIED_PIGLIN && BetaRules.betaWorld(self.level());
    }

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;clearFire()V", ordinal = 0))
    private void b173$fireDiesDown(Entity self, Operation<Void> original) {
        if (b173$betaPigman()) {
            self.setRemainingFireTicks(Math.max(0, self.getRemainingFireTicks() - 4));
        } else {
            original.call(self);
        }
    }

    @WrapWithCondition(method = "applyEffectsFromBlocks(Ljava/util/List;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;setRemainingFireTicks(I)V"))
    private boolean b173$keepPigmanFire(Entity self, int ticks) {
        return !b173$betaPigman();
    }

    @ModifyReturnValue(method = "displayFireAnimation", at = @At("RETURN"))
    private boolean b173$drawPigmanFire(boolean shown) {
        if (shown || !b173$betaPigman()) {
            return shown;
        }
        return getSharedFlag(0) && !((Entity) (Object) this).isSpectator();
    }
}
