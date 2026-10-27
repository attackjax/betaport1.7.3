package dev.b173.rules.ai;

import dev.b173.mixin.ai.WolfAccessor;
import dev.b173.rules.BetaLighting;
import dev.b173.rules.BetaMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class BetaWolfBrain extends BetaBrain {
    private static final long ANGER = Integer.MAX_VALUE;

    private final Wolf wolf;

    BetaWolfBrain(Wolf wolf) {
        super(wolf, 1.1F);
        this.wolf = wolf;
    }

    @Override
    protected boolean movementCeased() {
        return wolf.isInSittingPose() || ((WolfAccessor) wolf).b173$isShaking();
    }

    @Override
    protected float verticalFaceSpeed() {
        return wolf.isInSittingPose() ? 20.0F : 40.0F;
    }

    @Override
    protected float pathWeight(int x, int y, int z) {
        if (mob.level().getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) {
            return 10.0F;
        }
        return BetaLighting.brightness(mob.level(), new BlockPos(x, y, z)) - 0.5F;
    }

    @Override
    protected Entity findTarget() {
        return wolf.isAngry() ? nearestPlayer(16.0D, TARGETABLE) : null;
    }

    @Override
    protected void attack(Entity target, float distance) {
        if (distance > 2.0F && distance < 6.0F && random.nextInt(10) == 0) {
            if (wolf.onGround()) {
                double dx = target.getX() - wolf.getX();
                double dz = target.getZ() - wolf.getZ();
                float horizontal = Mth.sqrt((float) (dx * dx + dz * dz));
                if (horizontal > 0.0F) {
                    Vec3 motion = wolf.getDeltaMovement();
                    wolf.setDeltaMovement(
                            dx / (double) horizontal * 0.5D * 0.800000011920929D + motion.x * 0.20000000298023224D,
                            0.4000000059604645D,
                            dz / (double) horizontal * 0.5D * 0.800000011920929D + motion.z * 0.20000000298023224D);
                }
            }
        } else if ((double) distance < 1.5D) {
            AABB own = wolf.getBoundingBox();
            AABB theirs = target.getBoundingBox();
            if (theirs.maxY > own.minY && theirs.minY < own.maxY) {
                attackTime = 20;
                if (wolf.level() instanceof ServerLevel level) {
                    float damage = BetaMobs.attackDamage(wolf, (float) wolf.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    if (target.hurtServer(level, wolf.damageSources().mobAttack(wolf), damage)) {
                        wolf.setLastHurtMob(target);
                    }
                }
            }
        }
    }

    @Override
    protected void afterThinking() {
        if (!hasAttacked && !hasPath() && wolf.isTame() && wolf.getVehicle() == null) {
            Player player = owner();
            if (player != null && player.level() == wolf.level()) {
                float distance = ownerDistance(player);
                if (distance > 5.0F) {
                    followOwner(player, distance);
                }
            } else if (!wolf.isInWater()) {
                sit(true);
            }
        } else if (target == null && !hasPath() && !wolf.isTame()
                && wolf.level().getRandom().nextInt(100) == 0) {
            List<Sheep> sheep = wolf.level().getEntitiesOfClass(Sheep.class, betaBox(wolf.getX(), wolf.getY(),
                    wolf.getZ()));
            if (!sheep.isEmpty()) {
                target = sheep.get(wolf.level().getRandom().nextInt(sheep.size()));
            }
        }
        if (wolf.isInWater()) {
            sit(false);
        }
        beg();
        if (wolf.isAngry() && wolf.getPersistentAngerEndTime() - wolf.level().getGameTime() < ANGER / 2) {
            wolf.setTimeToRemainAngry(ANGER);
        }
    }

    private void beg() {
        boolean interested = false;
        if (lookTarget() instanceof Player player && !hasPath() && !wolf.isAngry()) {
            ItemStack held = player.getMainHandItem();
            if (!held.isEmpty()) {
                if (!wolf.isTame()) {
                    interested = held.is(Items.BONE);
                } else {
                    interested = held.is(Items.PORKCHOP) || held.is(Items.COOKED_PORKCHOP);
                }
            }
        }
        if (wolf.isInterested() != interested) {
            wolf.setIsInterested(interested);
        }
        if (interested) {
            keepLooking(10);
        }
    }

    private void followOwner(Player owner, float distance) {
        Path found = pathTo(owner);
        if (found == null && distance > 12.0F) {
            int x0 = Mth.floor(owner.getX()) - 2;
            int z0 = Mth.floor(owner.getZ()) - 2;
            int y = Mth.floor(owner.getBoundingBox().minY);
            for (int dx = 0; dx <= 4; dx++) {
                for (int dz = 0; dz <= 4; dz++) {
                    if ((dx < 1 || dz < 1 || dx > 3 || dz > 3) && solid(x0 + dx, y - 1, z0 + dz)
                            && !solid(x0 + dx, y, z0 + dz) && !solid(x0 + dx, y + 1, z0 + dz)) {
                        wolf.snapTo((double) ((float) (x0 + dx) + 0.5F), y, (double) ((float) (z0 + dz) + 0.5F),
                                wolf.getYRot(), wolf.getXRot());
                        setPath(null);
                        return;
                    }
                }
            }
        } else {
            setPath(found);
        }
    }

    private boolean solid(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return wolf.level().getBlockState(pos).isRedstoneConductor(wolf.level(), pos);
    }

    private Player owner() {
        if (wolf.getOwner() instanceof Player player) {
            return player;
        }
        EntityReference<LivingEntity> reference = wolf.getOwnerReference();
        if (reference != null && wolf.level() instanceof ServerLevel level) {
            return level.getServer().getPlayerList().getPlayer(reference.getUUID());
        }
        return null;
    }

    private float ownerDistance(Player owner) {
        double dx = owner.getX() - wolf.getX();
        double dy = owner.getY() + PLAYER_POS_Y - wolf.getY();
        double dz = owner.getZ() - wolf.getZ();
        return Mth.sqrt((float) (dx * dx + dy * dy + dz * dz));
    }

    void sit(boolean sitting) {
        if (wolf.isOrderedToSit() != sitting) {
            wolf.setOrderedToSit(sitting);
        }
        if (wolf.isInSittingPose() != sitting) {
            wolf.setInSittingPose(sitting);
        }
    }

    void anger() {
        wolf.setTimeToRemainAngry(ANGER);
    }

    @Override
    public void hurtAttempted(Entity attacker) {
        sit(false);
    }

    @Override
    public void hurtBy(Entity attacker) {
        if (!wolf.isTame() && !wolf.isAngry()) {
            if (attacker instanceof Player) {
                anger();
                target = attacker;
            }
            if (attacker instanceof LivingEntity) {
                for (Wolf other : wolf.level().getEntitiesOfClass(Wolf.class,
                        betaBox(wolf.getX(), wolf.getY(), wolf.getZ()))) {
                    if (!other.isTame() && BetaBrains.brainOf(other) instanceof BetaWolfBrain brain
                            && brain.target == null) {
                        brain.target = attacker;
                        if (attacker instanceof Player) {
                            brain.anger();
                        }
                    }
                }
            }
        } else if (attacker != null && attacker != wolf) {
            if (wolf.isTame() && attacker instanceof Player player && wolf.isOwnedBy(player)) {
                return;
            }
            target = attacker;
        }
    }

    static AABB betaBox(double x, double y, double z) {
        return new AABB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D).inflate(16.0D, 4.0D, 16.0D);
    }
}
