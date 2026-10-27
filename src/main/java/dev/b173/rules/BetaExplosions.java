package dev.b173.rules;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public final class BetaExplosions {
    public static final float DROP_RADIUS = 1.0F / 0.3F;
    public static final double TNT_MIDDLE = 0.49D;

    private BetaExplosions() {
    }

    public static double betaPosY(Entity entity) {
        double y = entity.getY();
        if (entity instanceof Player) {
            return y + 1.62D;
        }
        if (entity instanceof ItemEntity) {
            return y + 0.125D;
        }
        if (entity instanceof PrimedTnt || entity instanceof FallingBlockEntity) {
            return y + TNT_MIDDLE;
        }
        if (entity instanceof AbstractMinecart) {
            return y + 0.35D;
        }
        if (entity instanceof AbstractBoat) {
            return y + 0.3D;
        }
        return y;
    }

    public static void hurtEntities(Explosion explosion, ServerLevel level, Entity source, Vec3 center, float radius,
                                    DamageSource damageSource, ExplosionDamageCalculator calculator,
                                    Map<Player, Vec3> hitPlayers) {
        if (radius < 1.0E-5F) {
            return;
        }
        float size = radius * 2.0F;
        int x0 = Mth.floor(center.x - (double) size - 1.0D);
        int x1 = Mth.floor(center.x + (double) size + 1.0D);
        int y0 = Mth.floor(center.y - (double) size - 1.0D);
        int y1 = Mth.floor(center.y + (double) size + 1.0D);
        int z0 = Mth.floor(center.z - (double) size - 1.0D);
        int z1 = Mth.floor(center.z + (double) size + 1.0D);
        List<Entity> reached = level.getEntities(source, new AABB(x0, y0, z0, x1, y1, z1));
        for (Entity entity : reached) {
            if (entity.ignoreExplosion(explosion)) {
                continue;
            }
            double dx = entity.getX() - center.x;
            double dy = betaPosY(entity) - center.y;
            double dz = entity.getZ() - center.z;
            double length = (double) (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            double distance = length / (double) size;
            if (distance > 1.0D) {
                continue;
            }
            if (length > 0.0D) {
                dx /= length;
                dy /= length;
                dz /= length;
            }
            boolean hurts = calculator.shouldDamageEntity(explosion, entity);
            float knockback = calculator.getKnockbackMultiplier(entity);
            double seen = hurts || knockback != 0.0F ? (double) seen(level, center, entity) : 0.0D;
            double impact = (1.0D - distance) * seen;
            if (hurts) {
                int damage = (int) ((impact * impact + impact) / 2.0D * 8.0D * (double) size + 1.0D);
                entity.hurtServer(level, damageSource, (float) damage);
            }
            Vec3 push = new Vec3(dx * impact * knockback, dy * impact * knockback, dz * impact * knockback);
            entity.push(push);
            if (entity instanceof Player player && !player.isSpectator()
                    && (!player.isCreative() || !player.getAbilities().flying)) {
                hitPlayers.put(player, push);
            }
            entity.onExplosionHit(source);
        }
    }

    public static float seen(ServerLevel level, Vec3 center, Entity entity) {
        AABB box = entity.getBoundingBox();
        double stepX = 1.0D / ((box.maxX - box.minX) * 2.0D + 1.0D);
        double stepY = 1.0D / ((box.maxY - box.minY) * 2.0D + 1.0D);
        double stepZ = 1.0D / ((box.maxZ - box.minZ) * 2.0D + 1.0D);
        int clear = 0;
        int all = 0;
        for (float fx = 0.0F; fx <= 1.0F; fx = (float) ((double) fx + stepX)) {
            for (float fy = 0.0F; fy <= 1.0F; fy = (float) ((double) fy + stepY)) {
                for (float fz = 0.0F; fz <= 1.0F; fz = (float) ((double) fz + stepZ)) {
                    Vec3 point = new Vec3(box.minX + (box.maxX - box.minX) * (double) fx,
                            box.minY + (box.maxY - box.minY) * (double) fy,
                            box.minZ + (box.maxZ - box.minZ) * (double) fz);
                    if (level.clip(new ClipContext(point, center, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                            entity)).getType() == HitResult.Type.MISS) {
                        clear++;
                    }
                    all++;
                }
            }
        }
        return (float) clear / (float) all;
    }
}
