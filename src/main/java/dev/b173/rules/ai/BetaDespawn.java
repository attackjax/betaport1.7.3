package dev.b173.rules.ai;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class BetaDespawn {
    private static final double FAR = 128.0D * 128.0D;
    private static final double NEAR = 32.0D * 32.0D;

    private BetaDespawn() {
    }

    public static boolean ageless(EntityType<?> type) {
        return type == EntityType.SLIME || type == EntityType.GHAST || type == EntityType.SQUID;
    }

    static int check(Mob mob, int age) {
        Player player = nearestPlayer(mob);
        if (player == null) {
            return age;
        }
        double distance = distanceSqr(mob, player);
        if (!canDespawn(mob, distance)) {
            return age;
        }
        if (distance > FAR) {
            mob.discard();
        }
        if (age > 600 && mob.getRandom().nextInt(800) == 0) {
            if (distance < NEAR) {
                return 0;
            }
            mob.discard();
        }
        return age;
    }

    public static void checkAgeless(Mob mob) {
        Player player = nearestPlayer(mob);
        if (player != null) {
            double distance = distanceSqr(mob, player);
            if (distance > FAR && canDespawn(mob, distance)) {
                mob.discard();
            }
        }
    }

    private static boolean canDespawn(Mob mob, double distanceSqr) {
        return !mob.isPersistenceRequired() && !mob.isLeashed() && mob.removeWhenFarAway(distanceSqr);
    }

    private static Player nearestPlayer(Mob mob) {
        return mob.level().getNearestPlayer(mob.getX(), mob.getY() - BetaBrain.PLAYER_POS_Y, mob.getZ(), -1.0D,
                EntitySelector.NO_SPECTATORS);
    }

    private static double distanceSqr(Mob mob, Player player) {
        double dx = player.getX() - mob.getX();
        double dy = player.getY() + BetaBrain.PLAYER_POS_Y - mob.getY();
        double dz = player.getZ() - mob.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
