package dev.b173.mixin.vehicle;

import dev.b173.rules.BetaBoat;
import dev.b173.rules.BetaMinecart;
import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VehicleEntity.class)
abstract class VehicleEntityDestroyMixin {
    @Inject(method = "destroy(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/Item;)V",
            at = @At("HEAD"), cancellable = true)
    private void b173$betaWreck(ServerLevel level, Item modern, CallbackInfo info) {
        VehicleEntity self = (VehicleEntity) (Object) this;
        if (!BetaRules.betaWorld(level)) {
            return;
        }
        if (self instanceof Boat boat) {
            dismount(self);
            BetaBoat.dropWreckage(level, boat);
            self.kill(level);
            info.cancel();
        } else if (self instanceof Minecart || self instanceof MinecartChest || self instanceof MinecartFurnace) {
            dismount(self);
            self.kill(level);
            BetaMinecart.dropCart(level, self);
            info.cancel();
        }
    }

    private static void dismount(Entity vehicle) {
        for (Entity rider : List.copyOf(vehicle.getPassengers())) {
            rider.stopRiding();
        }
    }
}
