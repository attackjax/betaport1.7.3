package dev.b173.rules.ai;

import dev.b173.core.BetaMath;
import dev.b173.mixin.ai.SquidAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BetaSquidBrain extends BetaBrain {
    private static final float PI = 3.1415927F;

    private final Squid squid;
    private float dirX;
    private float dirY;
    private float dirZ;
    private float speed;

    BetaSquidBrain(Squid squid) {
        super(squid, 0.0F);
        this.squid = squid;
    }

    @Override
    protected void think() {
        if (random.nextInt(50) == 0 || !squid.isInWater() || dirX == 0.0F && dirY == 0.0F && dirZ == 0.0F) {
            float angle = random.nextFloat() * PI * 2.0F;
            dirX = BetaMath.cos(angle) * 0.2F;
            dirY = -0.1F + random.nextFloat() * 0.2F;
            dirZ = BetaMath.sin(angle) * 0.2F;
        }
        BetaDespawn.checkAgeless(squid);
    }

    public void swim() {
        SquidAccessor fields = (SquidAccessor) squid;
        squid.xBodyRotO = squid.xBodyRot;
        squid.zBodyRotO = squid.zBodyRot;
        squid.oldTentacleMovement = squid.tentacleMovement;
        squid.oldTentacleAngle = squid.tentacleAngle;
        squid.tentacleMovement += fields.b173$tentacleSpeed();
        if (squid.tentacleMovement > PI * 2.0F) {
            squid.tentacleMovement -= PI * 2.0F;
            if (random.nextInt(10) == 0) {
                fields.b173$setTentacleSpeed(1.0F / (random.nextFloat() + 1.0F) * 0.2F);
            }
            squid.level().broadcastEntityEvent(squid, (byte) 19);
        }
        float rotateSpeed = fields.b173$rotateSpeed();
        if (betaInWater(squid)) {
            if (squid.tentacleMovement < PI) {
                float stroke = squid.tentacleMovement / PI;
                squid.tentacleAngle = BetaMath.sin(stroke * stroke * PI) * PI * 0.25F;
                if ((double) stroke > 0.75D) {
                    speed = 1.0F;
                    rotateSpeed = 1.0F;
                } else {
                    rotateSpeed *= 0.8F;
                }
            } else {
                squid.tentacleAngle = 0.0F;
                speed *= 0.9F;
                rotateSpeed *= 0.99F;
            }
            squid.setDeltaMovement(dirX * speed, dirY * speed, dirZ * speed);
            Vec3 motion = squid.getDeltaMovement();
            float horizontal = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            squid.yBodyRot += (-((float) Math.atan2(motion.x, motion.z)) * 180.0F / PI - squid.yBodyRot) * 0.1F;
            squid.setYRot(squid.yBodyRot);
            squid.zBodyRot += PI * rotateSpeed * 1.5F;
            squid.xBodyRot += (-((float) Math.atan2(horizontal, motion.y)) * 180.0F / PI - squid.xBodyRot) * 0.1F;
        } else {
            squid.tentacleAngle = Mth.abs(BetaMath.sin(squid.tentacleMovement)) * PI * 0.25F;
            Vec3 motion = squid.getDeltaMovement();
            squid.setDeltaMovement(0.0D, (motion.y - 0.08D) * 0.9800000190734863D, 0.0D);
            squid.xBodyRot = (float) ((double) squid.xBodyRot + (double) (-90.0F - squid.xBodyRot) * 0.02D);
        }
        fields.b173$setRotateSpeed(rotateSpeed);
    }

    public static boolean betaInWater(Entity squid) {
        AABB box = squid.getBoundingBox();
        int x0 = Mth.floor(box.minX);
        int x1 = Mth.floor(box.maxX + 1.0D);
        int y0 = Mth.floor(box.minY + 0.6D);
        int y1 = Mth.floor(box.maxY - 0.6D + 1.0D);
        int z0 = Mth.floor(box.minZ);
        int z1 = Mth.floor(box.maxZ + 1.0D);
        Level level = squid.level();
        if (!level.hasChunksAt(x0, z0, x1, z1)) {
            return false;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (level.getFluidState(pos.set(x, y, z)).is(FluidTags.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
