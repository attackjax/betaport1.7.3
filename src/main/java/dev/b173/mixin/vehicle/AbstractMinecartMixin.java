package dev.b173.mixin.vehicle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.b173.rules.BetaMinecart;
import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
abstract class AbstractMinecartMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/minecart/MinecartBehavior;tick()V"))
    private void b173$betaRun(MinecartBehavior behavior, Operation<Void> original) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        if (self.level() instanceof ServerLevel level && behavior instanceof OldMinecartBehavior
                && BetaRules.betaWorld(level)) {
            BetaMinecart.tick(self, level);
            return;
        }
        original.call(behavior);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;isInLava()Z"))
    private boolean b173$lavaProof(boolean inLava) {
        return inLava && !BetaRules.betaWorld(((AbstractMinecart) (Object) this).level());
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void b173$betaPush(Entity entity, CallbackInfo info) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        if (!BetaRules.betaWorld(self.level())) {
            return;
        }
        info.cancel();
        if (entity.noPhysics || self.noPhysics) {
            return;
        }
        if (!self.level().isClientSide()) {
            BetaMinecart.collide(self, entity);
        } else if (entity instanceof Player && !(entity instanceof AbstractMinecart)) {
            BetaMinecart.pushBack(self, entity);
        }
    }

    @ModifyReturnValue(method = "canCollideWith", at = @At("RETURN"))
    private boolean b173$hitsEverything(boolean modern) {
        return modern || BetaRules.betaWorld(((AbstractMinecart) (Object) this).level());
    }

    @ModifyReturnValue(method = "getDefaultDisplayOffset", at = @At("RETURN"))
    private int b173$betaFurnaceOffset(int modern) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        return self instanceof MinecartFurnace && self.level() != null && BetaRules.betaWorld(self.level())
                ? BetaMinecart.DISPLAY_OFFSET : modern;
    }
}
