package dev.b173.rules.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;

public final class BetaPigmanBrain extends BetaMonsterBrain {
    private static final long ANGER = Integer.MAX_VALUE;

    private final ZombifiedPiglin pigman;
    private int squealIn;
    private float speed = 0.5F;

    BetaPigmanBrain(ZombifiedPiglin pigman) {
        super(pigman, 0.5F);
        this.pigman = pigman;
    }

    @Override
    protected void beforeThinking() {
        speed = target != null ? 0.95F : 0.5F;
        if (squealIn > 0 && --squealIn == 0) {
            float pitch = ((random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F) * 1.8F;
            pigman.playSound(SoundEvents.ZOMBIFIED_PIGLIN_ANGRY, 2.0F, pitch);
        }
        super.beforeThinking();
        if (pigman.isAngry() && pigman.getPersistentAngerEndTime() - pigman.level().getGameTime() < ANGER / 2) {
            pigman.setTimeToRemainAngry(ANGER);
        }
    }

    @Override
    protected float moveSpeed() {
        return speed;
    }

    @Override
    protected Entity findTarget() {
        return pigman.isAngry() ? super.findTarget() : null;
    }

    @Override
    public void hurtAttempted(Entity attacker) {
        if (attacker instanceof Player) {
            for (Entity other : pigman.level().getEntities(pigman, pigman.getBoundingBox().inflate(32.0D))) {
                if (other instanceof ZombifiedPiglin && BetaBrains.brainOf(other) instanceof BetaPigmanBrain brain) {
                    brain.becomeAngryAt(attacker);
                }
            }
            becomeAngryAt(attacker);
        }
    }

    private void becomeAngryAt(Entity attacker) {
        target = attacker;
        random.nextInt(400);
        pigman.setTimeToRemainAngry(ANGER);
        squealIn = random.nextInt(40);
    }
}
