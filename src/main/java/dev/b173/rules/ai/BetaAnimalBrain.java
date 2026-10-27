package dev.b173.rules.ai;

import dev.b173.rules.BetaLighting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;

public final class BetaAnimalBrain extends BetaBrain {
    public BetaAnimalBrain(Mob mob) {
        super(mob, 0.7F);
    }

    @Override
    protected float pathWeight(int x, int y, int z) {
        if (mob.level().getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) {
            return 10.0F;
        }
        return BetaLighting.brightness(mob.level(), new BlockPos(x, y, z)) - 0.5F;
    }
}
