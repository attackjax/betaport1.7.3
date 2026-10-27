package dev.b173.rules.ai;

import dev.b173.core.BetaMath;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.function.Predicate;

public abstract class BetaBrain extends Goal {
    private static final float GROUND_SPEED = 0.1F;
    private static final float TARGET_PATH_RANGE = 16.0F;
    private static final float WANDER_PATH_RANGE = 10.0F;
    private static final double LOOK_RANGE = 8.0D;
    private static final double BETA_PI = 3.1415927410125732D;
    protected static final double PLAYER_POS_Y = 1.62D;
    protected static final Predicate<Entity> TARGETABLE = EntitySelector.NO_CREATIVE_OR_SPECTATOR;

    protected final Mob mob;
    protected final RandomSource random;
    private final float moveSpeed;
    protected Entity target;
    private Path path;
    protected boolean hasAttacked;
    protected int attackTime;
    private int age;
    private long noWayFrom = Long.MIN_VALUE;
    private long noWayTo;
    private int noWayUntil;
    private boolean jumping;
    private Entity lookTarget;
    private int lookTicks;
    private float randomYawVelocity;

    protected BetaBrain(Mob mob, float moveSpeed) {
        this.mob = mob;
        this.random = mob.getRandom();
        this.moveSpeed = moveSpeed;
        setFlags(EnumSet.allOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (attackTime > 0) {
            attackTime--;
        }
        beforeThinking();
        think();
        if (mob.isRemoved()) {
            return;
        }
        afterThinking();
        randomYawVelocity *= 0.9F;
        mob.setJumping(jumping);
        float forward = mob.zza;
        float strafe = mob.xxa;
        mob.setSpeed(GROUND_SPEED);
        mob.setZza(forward);
        mob.setXxa(strafe);
        mob.setYHeadRot(mob.getYRot());
    }

    protected void think() {
        hasAttacked = movementCeased();
        if (target == null) {
            target = findTarget();
            if (target != null) {
                path = pathTo(target);
            }
        } else if (!isValidTarget(target)) {
            target = null;
        } else {
            float distance = betaDistanceTo(target);
            if (mob.getSensing().hasLineOfSight(target)) {
                attack(target, distance);
            } else {
                attackBlocked(target, distance);
            }
        }

        if (!hasAttacked && target != null && (path == null || random.nextInt(20) == 0)) {
            path = pathTo(target);
        } else if (!hasAttacked && (path == null && random.nextInt(80) == 0 || random.nextInt(80) == 0)) {
            wander();
        }

        int feetY = Mth.floor(mob.getBoundingBox().minY + 0.5D);
        boolean inWater = mob.isInWater();
        boolean inLava = mob.isInLava();
        mob.setXRot(0.0F);
        if (path == null || random.nextInt(100) == 0) {
            idle(inWater, inLava);
            path = null;
            return;
        }

        Vec3 next = path.isDone() ? null : path.getNextEntityPos(mob);
        double reach = mob.getBbWidth() * 2.0F;
        while (next != null && horizontalDistanceSqr(next) < reach * reach) {
            path.advance();
            if (path.isDone()) {
                next = null;
                path = null;
            } else {
                next = path.getNextEntityPos(mob);
            }
        }

        jumping = false;
        if (next != null) {
            double dx = next.x - mob.getX();
            double dz = next.z - mob.getZ();
            double dy = next.y - (double) feetY;
            float wanted = (float) (Math.atan2(dz, dx) * 180.0D / BETA_PI) - 90.0F;
            float turn = wanted - mob.getYRot();
            mob.setZza(moveSpeed());
            while (turn < -180.0F) {
                turn += 360.0F;
            }
            while (turn >= 180.0F) {
                turn -= 360.0F;
            }
            turn = Mth.clamp(turn, -30.0F, 30.0F);
            mob.setYRot(mob.getYRot() + turn);
            if (hasAttacked && target != null) {
                double tx = target.getX() - mob.getX();
                double tz = target.getZ() - mob.getZ();
                float before = mob.getYRot();
                mob.setYRot((float) (Math.atan2(tz, tx) * 180.0D / BETA_PI) - 90.0F);
                float angle = (before - mob.getYRot() + 90.0F) * 3.1415927F / 180.0F;
                mob.setXxa(-BetaMath.sin(angle) * mob.zza * 1.0F);
                mob.setZza(BetaMath.cos(angle) * mob.zza * 1.0F);
            }
            if (dy > 0.0D) {
                jumping = true;
            }
        }
        if (target != null) {
            faceEntity(target, 30.0F, 30.0F);
        }
        if (mob.horizontalCollision && path == null) {
            jumping = true;
        }
        if (random.nextFloat() < 0.8F && (inWater || inLava)) {
            jumping = true;
        }
    }

    private void idle(boolean inWater, boolean inLava) {
        age++;
        age = BetaDespawn.check(mob, age);
        if (mob.isRemoved()) {
            return;
        }
        mob.setXxa(0.0F);
        mob.setZza(0.0F);
        if (random.nextFloat() < 0.02F) {
            Player player = nearestPlayer(LOOK_RANGE, EntitySelector.NO_SPECTATORS);
            if (player != null) {
                lookTarget = player;
                lookTicks = 10 + random.nextInt(20);
            } else {
                randomYawVelocity = (random.nextFloat() - 0.5F) * 20.0F;
            }
        }
        if (lookTarget != null) {
            faceEntity(lookTarget, 10.0F, verticalFaceSpeed());
            if (lookTicks-- <= 0 || lookTarget.isRemoved()
                    || betaDistanceSqrTo(lookTarget) > LOOK_RANGE * LOOK_RANGE) {
                lookTarget = null;
            }
        } else {
            if (random.nextFloat() < 0.05F) {
                randomYawVelocity = (random.nextFloat() - 0.5F) * 20.0F;
            }
            mob.setYRot(mob.getYRot() + randomYawVelocity);
            mob.setXRot(0.0F);
        }
        if (inWater || inLava) {
            jumping = random.nextFloat() < 0.8F;
        }
    }

    private void wander() {
        boolean found = false;
        int bestX = -1;
        int bestY = -1;
        int bestZ = -1;
        float best = -99999.0F;
        for (int i = 0; i < 10; i++) {
            int x = Mth.floor(mob.getX() + (double) random.nextInt(13) - 6.0D);
            int y = Mth.floor(mob.getY() + (double) random.nextInt(7) - 3.0D);
            int z = Mth.floor(mob.getZ() + (double) random.nextInt(13) - 6.0D);
            float weight = pathWeight(x, y, z);
            if (weight > best) {
                best = weight;
                bestX = x;
                bestY = y;
                bestZ = z;
                found = true;
            }
        }
        if (found) {
            path = BetaPaths.toSpot(mob, bestX, bestY, bestZ, WANDER_PATH_RANGE);
        }
    }

    protected final Path pathTo(Entity target) {
        if (betaDistanceTo(target) >= TARGET_PATH_RANGE + 1.0F) {
            return null;
        }
        long from = mob.blockPosition().asLong();
        long to = target.blockPosition().asLong();
        if (from == noWayFrom && to == noWayTo && mob.tickCount < noWayUntil) {
            return null;
        }
        Path found = BetaPaths.toEntity(mob, target, TARGET_PATH_RANGE);
        if (found == null) {
            noWayFrom = from;
            noWayTo = to;
            noWayUntil = mob.tickCount + 10;
        }
        return found;
    }

    protected final void faceEntity(Entity entity, float yawSpeed, float pitchSpeed) {
        double dx = entity.getX() - mob.getX();
        double dz = entity.getZ() - mob.getZ();
        double dy;
        if (entity instanceof LivingEntity) {
            dy = betaEyeY(mob) - betaEyeY(entity);
        } else {
            AABB box = entity.getBoundingBox();
            dy = (box.minY + box.maxY) / 2.0D - betaEyeY(mob);
        }
        double horizontal = Mth.sqrt((float) (dx * dx + dz * dz));
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / BETA_PI) - 90.0F;
        float pitch = (float) -(Math.atan2(dy, horizontal) * 180.0D / BETA_PI);
        mob.setXRot(-updateRotation(mob.getXRot(), pitch, pitchSpeed));
        mob.setYRot(updateRotation(mob.getYRot(), yaw, yawSpeed));
    }

    private static float updateRotation(float current, float wanted, float maxStep) {
        float step = wanted - current;
        while (step < -180.0F) {
            step += 360.0F;
        }
        while (step >= 180.0F) {
            step -= 360.0F;
        }
        if (step > maxStep) {
            step = maxStep;
        }
        if (step < -maxStep) {
            step = -maxStep;
        }
        return current + step;
    }

    protected final Player nearestPlayer(double range, Predicate<Entity> predicate) {
        return mob.level().getNearestPlayer(mob.getX(), mob.getY() - PLAYER_POS_Y, mob.getZ(), range, predicate);
    }

    protected final float betaDistanceTo(Entity entity) {
        return Mth.sqrt((float) betaDistanceSqrTo(entity));
    }

    protected final double betaDistanceSqrTo(Entity entity) {
        double dx = entity.getX() - mob.getX();
        double dy = betaPosY(entity) - mob.getY();
        double dz = entity.getZ() - mob.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private double horizontalDistanceSqr(Vec3 point) {
        double dx = point.x - mob.getX();
        double dz = point.z - mob.getZ();
        return dx * dx + dz * dz;
    }

    protected static double betaPosY(Entity entity) {
        return entity instanceof Player ? entity.getY() + PLAYER_POS_Y : entity.getY();
    }

    protected static double betaEyeY(Entity entity) {
        if (entity instanceof Player) {
            return entity.getY() + PLAYER_POS_Y + 0.12F;
        }
        return entity.getY() + entity.getBbHeight() * 0.85F;
    }

    protected boolean isValidTarget(Entity entity) {
        if (!entity.isAlive() || entity.level() != mob.level()) {
            return false;
        }
        return !(entity instanceof Player player) || !player.isCreative() && !player.isSpectator();
    }

    protected Entity findTarget() {
        return null;
    }

    protected void attack(Entity target, float distance) {
    }

    protected void attackBlocked(Entity target, float distance) {
    }

    protected void beforeThinking() {
    }

    protected void afterThinking() {
    }

    protected final void growOlder(int ticks) {
        age += ticks;
    }

    public void resetAge() {
        age = 0;
    }

    protected boolean movementCeased() {
        return false;
    }

    protected float verticalFaceSpeed() {
        return 40.0F;
    }

    protected float moveSpeed() {
        return moveSpeed;
    }

    public void hurtAttempted(Entity attacker) {
    }

    public final Entity target() {
        return target;
    }

    public final void setTarget(Entity entity) {
        target = entity;
    }

    protected final void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public final boolean hasPath() {
        return path != null;
    }

    protected final void setPath(Path path) {
        this.path = path;
    }

    protected final Entity lookTarget() {
        return lookTarget;
    }

    protected final void keepLooking(int ticks) {
        lookTicks = ticks;
    }

    protected float pathWeight(int x, int y, int z) {
        return 0.0F;
    }

    public void hurtBy(Entity attacker) {
    }

    static final class StillMoveControl extends MoveControl {
        StillMoveControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
        }
    }

    static final class StillLookControl extends LookControl {
        StillLookControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
        }
    }

    static final class StillJumpControl extends JumpControl {
        StillJumpControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
        }
    }
}
