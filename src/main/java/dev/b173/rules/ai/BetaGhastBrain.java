package dev.b173.rules.ai;

import dev.b173.core.BetaMath;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BetaGhastBrain extends BetaBrain {
    public static final float VOLUME = 10.0F;

    private final Ghast ghast;
    private int courseChangeCooldown;
    private double waypointX;
    private double waypointY;
    private double waypointZ;
    private Entity targeted;
    private int aggroCooldown;
    private int attackCounter;

    BetaGhastBrain(Ghast ghast) {
        super(ghast, 0.0F);
        this.ghast = ghast;
    }

    @Override
    protected void think() {
        BetaDespawn.checkAgeless(ghast);
        if (ghast.isRemoved()) {
            return;
        }
        double dx = waypointX - ghast.getX();
        double dy = waypointY - ghast.getY();
        double dz = waypointZ - ghast.getZ();
        double distance = (double) (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 1.0D || distance > 60.0D) {
            waypointX = ghast.getX() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            waypointY = ghast.getY() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            waypointZ = ghast.getZ() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
        }
        if (courseChangeCooldown-- <= 0) {
            courseChangeCooldown += random.nextInt(5) + 2;
            if (courseTraversable(distance)) {
                ghast.setDeltaMovement(ghast.getDeltaMovement().add(dx / distance * 0.1D, dy / distance * 0.1D,
                        dz / distance * 0.1D));
            } else {
                waypointX = ghast.getX();
                waypointY = ghast.getY();
                waypointZ = ghast.getZ();
            }
        }
        if (targeted != null && targeted.isRemoved()) {
            targeted = null;
        }
        if (targeted == null || aggroCooldown-- <= 0) {
            targeted = nearestPlayer(100.0D, TARGETABLE);
            if (targeted != null) {
                aggroCooldown = 20;
            }
        }
        if (targeted != null && betaDistanceSqrTo(targeted) < 64.0D * 64.0D) {
            double tx = targeted.getX() - ghast.getX();
            double ty = targeted.getBoundingBox().minY + (double) (targeted.getBbHeight() / 2.0F)
                    - (ghast.getY() + (double) (ghast.getBbHeight() / 2.0F));
            double tz = targeted.getZ() - ghast.getZ();
            face(-((float) Math.atan2(tx, tz)) * 180.0F / 3.1415927F);
            if (ghast.getSensing().hasLineOfSight(targeted)) {
                if (attackCounter == 10) {
                    ghast.playSound(SoundEvents.GHAST_WARN, VOLUME, pitch());
                }
                attackCounter++;
                if (attackCounter == 20) {
                    ghast.playSound(SoundEvents.GHAST_SHOOT, VOLUME, pitch());
                    shoot(tx, ty, tz);
                    attackCounter = -40;
                }
            } else if (attackCounter > 0) {
                attackCounter--;
            }
        } else {
            Vec3 motion = ghast.getDeltaMovement();
            face(-((float) Math.atan2(motion.x, motion.z)) * 180.0F / 3.1415927F);
            if (attackCounter > 0) {
                attackCounter--;
            }
        }
        boolean charging = attackCounter > 10;
        if (ghast.isCharging() != charging) {
            ghast.setCharging(charging);
        }
    }

    private void face(float yaw) {
        ghast.yBodyRot = yaw;
        ghast.setYRot(yaw);
    }

    private float pitch() {
        return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F;
    }

    private boolean courseTraversable(double distance) {
        double stepX = (waypointX - ghast.getX()) / distance;
        double stepY = (waypointY - ghast.getY()) / distance;
        double stepZ = (waypointZ - ghast.getZ()) / distance;
        AABB box = ghast.getBoundingBox();
        for (int i = 1; (double) i < distance; i++) {
            box = box.move(stepX, stepY, stepZ);
            if (!ghast.level().noCollision(ghast, box)) {
                return false;
            }
        }
        return true;
    }

    public static void betaStart(LargeFireball fireball, Vec3 aim) {
        fireball.setDeltaMovement(aim.scale(1.0E-4D));
        fireball.accelerationPower = 0.1D;
    }

    private void shoot(double tx, double ty, double tz) {
        Vec3 aim = new Vec3(tx + random.nextGaussian() * 0.4D, ty + random.nextGaussian() * 0.4D,
                tz + random.nextGaussian() * 0.4D);
        LargeFireball fireball = new LargeFireball(ghast.level(), ghast, aim.normalize(), 1);
        betaStart(fireball, aim.normalize());
        float yaw = -ghast.getYRot() * 0.017453292F - 3.1415927F;
        double lookX = BetaMath.sin(yaw) * -1.0F;
        double lookZ = BetaMath.cos(yaw) * -1.0F;
        fireball.setPos(ghast.getX() + lookX * 4.0D, ghast.getY() + (double) (ghast.getBbHeight() / 2.0F) + 0.5D,
                ghast.getZ() + lookZ * 4.0D);
        ghast.level().addFreshEntity(fireball);
    }
}
