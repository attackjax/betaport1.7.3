package dev.b173.rules;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;

public final class BetaVehicles {
    private BetaVehicles() {
    }

    public static boolean isBetaVehicle(Entity entity) {
        return (entity.getType() == EntityType.PIG || entity instanceof Minecart || entity instanceof Boat)
                && BetaRules.betaWorld(entity.level());
    }

    public static InteractionResult interact(Entity vehicle, Player player) {
        if (!(vehicle instanceof Minecart || vehicle instanceof Boat) || !BetaRules.betaWorld(vehicle.level())) {
            return null;
        }
        Entity rider = vehicle.getFirstPassenger();
        if (rider instanceof Player && rider != player) {
            return InteractionResult.SUCCESS;
        }
        if (!vehicle.level().isClientSide()) {
            mount(player, vehicle);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult interactPig(Entity pig, Player player) {
        if (!BetaRules.betaWorld(pig.level())) {
            return null;
        }
        Entity rider = pig.getFirstPassenger();
        if (rider != null && rider != player) {
            return null;
        }
        if (rider == player) {
            if (!pig.level().isClientSide()) {
                player.stopRiding();
            }
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    public static void mount(Entity rider, Entity vehicle) {
        if (rider.getVehicle() == vehicle) {
            rider.stopRiding();
            return;
        }
        for (Entity sitting : vehicle.getPassengers()) {
            sitting.stopRiding();
        }
        rider.startRiding(vehicle, true, true);
    }

    public static void followVehicle(Entity rider, Entity vehicle, double[] owed) {
        owed[0] += (double) (vehicle.getYRot() - vehicle.yRotO);
        owed[1] += (double) (vehicle.getXRot() - vehicle.xRotO);
        while (owed[0] >= 180.0D) {
            owed[0] -= 360.0D;
        }
        while (owed[0] < -180.0D) {
            owed[0] += 360.0D;
        }
        while (owed[1] >= 180.0D) {
            owed[1] -= 360.0D;
        }
        while (owed[1] < -180.0D) {
            owed[1] += 360.0D;
        }
        double yaw = owed[0] * 0.5D;
        double pitch = owed[1] * 0.5D;
        float most = 10.0F;
        if (yaw > (double) most) {
            yaw = (double) most;
        }
        if (yaw < (double) (-most)) {
            yaw = (double) (-most);
        }
        if (pitch > (double) most) {
            pitch = (double) most;
        }
        if (pitch < (double) (-most)) {
            pitch = (double) (-most);
        }
        owed[0] -= yaw;
        owed[1] -= pitch;
        if (yaw == 0.0D && pitch == 0.0D) {
            return;
        }
        rider.setYRot((float) ((double) rider.getYRot() + yaw));
        rider.setXRot((float) ((double) rider.getXRot() + pitch));
        rider.setYHeadRot((float) ((double) rider.getYHeadRot() + yaw));
    }

    public static void push(Entity self, Entity entity) {
        if (self.hasPassenger(entity) || entity.hasPassenger(self)) {
            return;
        }
        double dx = entity.getX() - self.getX();
        double dz = entity.getZ() - self.getZ();
        double distance = Mth.absMax(dx, dz);
        if (distance < (double) 0.01F) {
            return;
        }
        distance = BetaMinecart.sqrtDouble(distance);
        dx /= distance;
        dz /= distance;
        double near = 1.0D / distance;
        if (near > 1.0D) {
            near = 1.0D;
        }
        dx *= near;
        dz *= near;
        dx *= (double) 0.05F;
        dz *= (double) 0.05F;
        self.push(-dx, 0.0D, -dz);
        entity.push(dx, 0.0D, dz);
    }
}
