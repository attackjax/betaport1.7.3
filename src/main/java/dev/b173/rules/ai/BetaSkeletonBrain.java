package dev.b173.rules.ai;

import dev.b173.core.BetaMath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BetaSkeletonBrain extends BetaMonsterBrain {
    private static final float BETA_EYE_HEIGHT = 1.8F * 0.85F;

    public BetaSkeletonBrain(Mob mob) {
        super(mob, 0.7F);
    }

    @Override
    protected void attack(Entity target, float distance) {
        if (distance < 10.0F) {
            double dx = target.getX() - mob.getX();
            double dz = target.getZ() - mob.getZ();
            if (attackTime == 0) {
                shoot(target, dx, dz);
                attackTime = 30;
            }
            mob.setYRot((float) (Math.atan2(dz, dx) * 180.0D / 3.1415927410125732D) - 90.0F);
            hasAttacked = true;
        }
    }

    private void shoot(Entity target, double dx, double dz) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack held = mob.getMainHandItem();
        Arrow arrow = new Arrow(level, mob, new ItemStack(Items.ARROW), held.is(Items.BOW) ? held : null);
        float yaw = mob.getYRot() / 180.0F * 3.1415927F;
        double x = mob.getX() - (double) (BetaMath.cos(yaw) * 0.16F);
        double y = mob.getY() + (double) BETA_EYE_HEIGHT - (double) 0.1F + (double) 1.4F;
        double z = mob.getZ() - (double) (BetaMath.sin(yaw) * 0.16F);
        arrow.setPos(x, y, z);
        double aimY = target instanceof Player ? target.getY() + PLAYER_POS_Y + (double) 0.12F
                : target.getY() + (double) (target.getBbHeight() * 0.85F);
        double dy = aimY - (double) 0.2F - y;
        float lift = Mth.sqrt((float) (dx * dx + dz * dz)) * 0.2F;
        arrow.shoot(dx, dy + (double) lift, dz, 0.6F, 12.0F);
        arrow.setOldPosAndRot();
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ARROW_SHOOT, mob.getSoundSource(),
                1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
        level.addFreshEntity(arrow);
    }
}
