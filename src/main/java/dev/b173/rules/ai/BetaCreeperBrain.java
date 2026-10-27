package dev.b173.rules.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;

public final class BetaCreeperBrain extends BetaMonsterBrain {
    private final Creeper creeper;

    public BetaCreeperBrain(Creeper creeper) {
        super(creeper, 0.7F);
        this.creeper = creeper;
    }

    @Override
    protected void attack(Entity target, float distance) {
        int state = creeper.getSwellDir();
        if (state <= 0 && distance < 3.0F || state > 0 && distance < 7.0F) {
            creeper.setSwellDir(1);
            hasAttacked = true;
        } else {
            creeper.setSwellDir(-1);
        }
    }

    @Override
    protected void attackBlocked(Entity target, float distance) {
        creeper.setSwellDir(-1);
    }

    @Override
    protected void afterThinking() {
        if (target == null) {
            creeper.setSwellDir(-1);
        }
    }
}
