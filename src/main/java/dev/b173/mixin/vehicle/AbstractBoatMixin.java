package dev.b173.mixin.vehicle;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.b173.rules.BetaBoat;
import dev.b173.rules.BetaBoatRider;
import dev.b173.rules.BetaVehicles;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBoat.class)
abstract class AbstractBoatMixin implements BetaBoatRider {
    @Unique
    private Vec3 b173$riderMotion = Vec3.ZERO;

    @Override
    public Vec3 b173$riderMotion() {
        return this.b173$riderMotion;
    }

    @Override
    public void b173$setRiderMotion(Vec3 motion) {
        this.b173$riderMotion = motion;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;isLocalInstanceAuthoritative()Z"),
            cancellable = true)
    private void b173$betaBoat(CallbackInfo info) {
        AbstractBoat self = (AbstractBoat) (Object) this;
        if (BetaBoat.isBeta(self)) {
            BetaBoat.tick(self);
            info.cancel();
        }
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;ejectPassengers()V"))
    private boolean b173$keepRider(AbstractBoat self) {
        return !BetaBoat.isBeta(self);
    }

    @ModifyReturnValue(method = "getControllingPassenger", at = @At("RETURN"))
    private LivingEntity b173$nobodySteers(LivingEntity modern) {
        return BetaBoat.isBeta((AbstractBoat) (Object) this) ? null : modern;
    }

    @Inject(method = "clampRotation", at = @At("HEAD"), cancellable = true)
    private void b173$lookAnywhere(Entity passenger, CallbackInfo info) {
        if (BetaBoat.isBeta((AbstractBoat) (Object) this)) {
            info.cancel();
        }
    }

    @Inject(method = "positionRider", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/VehicleEntity;positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            shift = At.Shift.AFTER), cancellable = true)
    private void b173$seatOnly(Entity passenger, Entity.MoveFunction move, CallbackInfo info) {
        if (BetaBoat.isBeta((AbstractBoat) (Object) this)) {
            info.cancel();
        }
    }

    @ModifyReturnValue(method = "getMaxPassengers", at = @At("RETURN"))
    private int b173$oneRider(int modern) {
        return BetaBoat.isBeta((AbstractBoat) (Object) this) ? 1 : modern;
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void b173$betaClick(Player player, InteractionHand hand, Vec3 at,
                                CallbackInfoReturnable<InteractionResult> info) {
        InteractionResult beta = BetaVehicles.interact((AbstractBoat) (Object) this, player);
        if (beta != null) {
            info.setReturnValue(beta);
        }
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void b173$betaPush(Entity entity, CallbackInfo info) {
        AbstractBoat self = (AbstractBoat) (Object) this;
        if (BetaBoat.isBeta(self)) {
            info.cancel();
            BetaVehicles.push(self, entity);
        }
    }

    @ModifyReturnValue(method = "canCollideWith", at = @At("RETURN"))
    private boolean b173$hitsEverything(boolean modern) {
        return modern || BetaBoat.isBeta((AbstractBoat) (Object) this);
    }
}
