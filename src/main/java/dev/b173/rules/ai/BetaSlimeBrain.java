package dev.b173.rules.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;

public final class BetaSlimeBrain extends BetaBrain {
    public static final float VOLUME = 0.6F;

    private final Slime slime;
    private int jumpDelay;

    BetaSlimeBrain(Slime slime) {
        super(slime, 0.0F);
        this.slime = slime;
        jumpDelay = random.nextInt(20) + 10;
    }

    @Override
    protected void think() {
        BetaDespawn.checkAgeless(slime);
        if (slime.isRemoved()) {
            return;
        }
        Player player = nearestPlayer(16.0D, TARGETABLE);
        if (player != null) {
            faceEntity(player, 10.0F, 20.0F);
        }
        if (slime.onGround() && jumpDelay-- <= 0) {
            jumpDelay = random.nextInt(20) + 10;
            if (player != null) {
                jumpDelay /= 3;
            }
            setJumping(true);
            if (slime.getSize() > 1) {
                slime.playSound(SoundEvents.SLIME_JUMP, VOLUME,
                        ((random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
            }
            slime.setXxa(1.0F - random.nextFloat() * 2.0F);
            slime.setZza((float) slime.getSize());
        } else {
            setJumping(false);
            if (slime.onGround()) {
                slime.setXxa(0.0F);
                slime.setZza(0.0F);
            }
        }
    }

    public static void touch(Slime slime, Player player) {
        int size = slime.getSize();
        if (size <= 1 || !(slime.level() instanceof ServerLevel level)
                || !player.getBoundingBox().inflate(1.0D, 0.0D, 1.0D).intersects(slime.getBoundingBox())
                || !slime.hasLineOfSight(player)) {
            return;
        }
        float dx = (float) (slime.getX() - player.getX());
        float dy = (float) (slime.getY() - (player.getY() + PLAYER_POS_Y));
        float dz = (float) (slime.getZ() - player.getZ());
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if ((double) distance < 0.6D * (double) size
                && player.hurtServer(level, slime.damageSources().mobAttack(slime), (float) size)) {
            slime.playSound(SoundEvents.SLIME_ATTACK, 1.0F,
                    (slime.getRandom().nextFloat() - slime.getRandom().nextFloat()) * 0.2F + 1.0F);
        }
    }

    public static void split(Slime slime, float healthLeft) {
        int size = slime.getSize();
        if (size <= 1 || healthLeft != 0.0F || !(slime.level() instanceof ServerLevel level)) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            float dx = ((float) (i % 2) - 0.5F) * (float) size / 4.0F;
            float dz = ((float) (i / 2) - 0.5F) * (float) size / 4.0F;
            Slime child = EntityType.SLIME.create(level, EntitySpawnReason.TRIGGERED);
            if (child == null) {
                continue;
            }
            child.setSize(size / 2, true);
            child.snapTo(slime.getX() + (double) dx, slime.getY() + 0.5D, slime.getZ() + (double) dz,
                    slime.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(child);
        }
    }
}
