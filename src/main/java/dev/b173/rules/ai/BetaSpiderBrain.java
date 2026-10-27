package dev.b173.rules.ai;

import dev.b173.rules.BetaLighting;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class BetaSpiderBrain extends BetaMonsterBrain {
    public BetaSpiderBrain(Mob mob) {
        super(mob, 0.8F);
    }

    @Override
    protected Entity findTarget() {
        if (BetaLighting.entityBrightness(mob) < 0.5F) {
            return nearestPlayer(16.0D, TARGETABLE);
        }
        return null;
    }

    @Override
    protected void attack(Entity target, float distance) {
        if (BetaLighting.entityBrightness(mob) > 0.5F && random.nextInt(100) == 0) {
            this.target = null;
            return;
        }
        if (distance > 2.0F && distance < 6.0F && random.nextInt(10) == 0) {
            if (mob.onGround()) {
                double dx = target.getX() - mob.getX();
                double dz = target.getZ() - mob.getZ();
                float horizontal = Mth.sqrt((float) (dx * dx + dz * dz));
                if (horizontal > 0.0F) {
                    Vec3 motion = mob.getDeltaMovement();
                    mob.setDeltaMovement(
                            dx / (double) horizontal * 0.5D * 0.800000011920929D + motion.x * 0.20000000298023224D,
                            0.4000000059604645D,
                            dz / (double) horizontal * 0.5D * 0.800000011920929D + motion.z * 0.20000000298023224D);
                }
            }
        } else {
            melee(target, distance);
        }
    }
}
