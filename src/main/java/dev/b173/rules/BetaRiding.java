package dev.b173.rules;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;

public final class BetaRiding {
    private BetaRiding() {
    }

    private static double seat(Entity vehicle) {
        if (vehicle.getType() == EntityType.PIG) {
            return (double) vehicle.getBbHeight() * 0.75D;
        }
        if (vehicle.getType() == EntityType.SPIDER) {
            return (double) vehicle.getBbHeight() * 0.75D - 0.5D;
        }
        if (vehicle instanceof AbstractMinecart) {
            return 0.35D - 0.30000001192092896D;
        }
        if (vehicle instanceof AbstractBoat) {
            return 0.3D - 0.30000001192092896D;
        }
        return Double.NaN;
    }

    private static double riderDrop(Entity rider) {
        return rider instanceof Player ? 0.5D : 0.0D;
    }

    public static Vec3 riderPosition(Entity vehicle, Entity rider) {
        double seat = seat(vehicle);
        if (Double.isNaN(seat)) {
            return null;
        }
        double x = vehicle.getX();
        double z = vehicle.getZ();
        if (vehicle instanceof AbstractBoat) {
            float yaw = vehicle.getYRot() * 0.017453292F;
            x += Math.sin(yaw) * 0.4D;
            z -= Math.cos(yaw) * 0.4D;
        }
        return new Vec3(x, vehicle.getY() + seat - riderDrop(rider), z);
    }

    public static Vec3 dismountPosition(Entity vehicle) {
        if (Double.isNaN(seat(vehicle))) {
            return null;
        }
        return new Vec3(vehicle.getX(), vehicle.getBoundingBox().maxY, vehicle.getZ());
    }
}
