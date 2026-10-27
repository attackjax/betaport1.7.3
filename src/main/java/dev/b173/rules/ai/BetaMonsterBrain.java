package dev.b173.rules.ai;

import dev.b173.rules.BetaLighting;
import dev.b173.rules.BetaMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class BetaMonsterBrain extends BetaBrain {
    public BetaMonsterBrain(Mob mob, float moveSpeed) {
        super(mob, moveSpeed);
    }

    @Override
    protected void beforeThinking() {
        float brightness = BetaLighting.entityBrightness(mob);
        if (mob.getType() == EntityType.ZOMBIE || mob.getType() == EntityType.SKELETON
                || mob.getType() == EntityType.ZOMBIFIED_PIGLIN) {
            burnInDaylight(brightness);
        }
        if (brightness > 0.5F) {
            growOlder(2);
        }
    }

    private void burnInDaylight(float brightness) {
        if (BetaLighting.skyDarkening(mob.level()) < 4 && brightness > 0.5F
                && mob.level().canSeeSky(BlockPos.containing(mob.getX(), mob.getY(), mob.getZ()))
                && random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F) {
            mob.setRemainingFireTicks(300);
        }
    }

    @Override
    protected Entity findTarget() {
        Player player = nearestPlayer(16.0D, TARGETABLE);
        return player != null && mob.getSensing().hasLineOfSight(player) ? player : null;
    }

    @Override
    protected void attack(Entity target, float distance) {
        melee(target, distance);
    }

    protected final void melee(Entity target, float distance) {
        AABB own = mob.getBoundingBox();
        AABB theirs = target.getBoundingBox();
        if (attackTime <= 0 && distance < 2.0F && theirs.maxY > own.minY && theirs.minY < own.maxY) {
            attackTime = 20;
            if (mob.level() instanceof ServerLevel level) {
                float damage = BetaMobs.attackDamage(mob, (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE));
                if (target.hurtServer(level, mob.damageSources().mobAttack(mob), damage)) {
                    mob.setLastHurtMob(target);
                }
            }
        }
    }

    @Override
    protected float pathWeight(int x, int y, int z) {
        return 0.5F - BetaLighting.brightness(mob.level(), new BlockPos(x, y, z));
    }

    @Override
    public void hurtBy(Entity attacker) {
        if (attacker == null || attacker == mob || mob.hasPassenger(attacker) || mob.getVehicle() == attacker) {
            return;
        }
        target = attacker;
    }

    public static final class Giant extends BetaMonsterBrain {
        public Giant(Mob mob) {
            super(mob, 0.5F);
        }

        @Override
        protected float pathWeight(int x, int y, int z) {
            return BetaLighting.brightness(mob.level(), new BlockPos(x, y, z)) - 0.5F;
        }
    }
}
