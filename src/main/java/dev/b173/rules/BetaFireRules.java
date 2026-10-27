package dev.b173.rules;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaFire;
import dev.b173.world.BetaBlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

public final class BetaFireRules {
    public static final int TICK_RATE = 40;

    private BetaFireRules() {
    }

    private static int betaId(BlockGetter level, BlockPos pos) {
        return BetaBlockStates.id(BetaBlockStates.beta(level.getBlockState(pos)));
    }

    private static boolean catches(BlockGetter level, BlockPos pos) {
        return BetaFire.encourage(betaId(level, pos)) > 0;
    }

    private static boolean neighbourBurns(BlockGetter level, BlockPos pos) {
        return catches(level, pos.east()) || catches(level, pos.west()) || catches(level, pos.below())
                || catches(level, pos.above()) || catches(level, pos.north()) || catches(level, pos.south());
    }

    public static boolean canStay(BlockGetter level, BlockPos pos) {
        return BetaBlocks.isNormalCube(betaId(level, pos.below())) || neighbourBurns(level, pos);
    }

    private static boolean rainAt(ServerLevel level, int x, int y, int z) {
        return level.isRainingAt(new BlockPos(x, y, z));
    }

    public static boolean switchedOff(ServerLevel level) {
        return level.getGameRules().get(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER) == 0;
    }

    public static void tick(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (switchedOff(level)) {
            level.scheduleTick(pos, state.getBlock(), TICK_RATE);
            return;
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean onNetherrack = betaId(level, pos.below()) == BetaBlocks.NETHERRACK;
        if (!canStay(level, pos)) {
            level.removeBlock(pos, false);
            return;
        }
        if (!onNetherrack && level.isRaining() && (rainAt(level, x, y, z) || rainAt(level, x - 1, y, z)
                || rainAt(level, x + 1, y, z) || rainAt(level, x, y, z - 1) || rainAt(level, x, y, z + 1))) {
            level.removeBlock(pos, false);
            return;
        }
        int age = state.getValue(FireBlock.AGE);
        if (age < 15) {
            int aged = age + random.nextInt(3) / 2;
            if (aged != age) {
                level.setBlock(pos, state.setValue(FireBlock.AGE, aged), Block.UPDATE_NONE);
            }
        }
        level.scheduleTick(pos, state.getBlock(), TICK_RATE);
        if (!onNetherrack && !neighbourBurns(level, pos)) {
            if (!BetaBlocks.isNormalCube(betaId(level, pos.below())) || age > 3) {
                level.removeBlock(pos, false);
            }
            return;
        }
        if (!onNetherrack && !catches(level, pos.below()) && age == 15 && random.nextInt(4) == 0) {
            level.removeBlock(pos, false);
            return;
        }
        burn(level, pos.east(), 300, random, age);
        burn(level, pos.west(), 300, random, age);
        burn(level, pos.below(), 250, random, age);
        burn(level, pos.above(), 250, random, age);
        burn(level, pos.north(), 300, random, age);
        burn(level, pos.south(), 300, random, age);
        for (int i = x - 1; i <= x + 1; i++) {
            for (int k = z - 1; k <= z + 1; k++) {
                for (int j = y - 1; j <= y + 4; j++) {
                    if (i == x && j == y && k == z) {
                        continue;
                    }
                    int odds = 100;
                    if (j > y + 1) {
                        odds += (j - (y + 1)) * 100;
                    }
                    BlockPos spot = new BlockPos(i, j, k);
                    int encourage = encouragement(level, spot);
                    if (encourage <= 0) {
                        continue;
                    }
                    int chance = (encourage + 40) / (age + 30);
                    if (chance <= 0 || random.nextInt(odds) > chance) {
                        continue;
                    }
                    if (level.isRaining() && rainAt(level, i, j, k) || rainAt(level, i - 1, j, z)
                            || rainAt(level, i + 1, j, k) || rainAt(level, i, j, k - 1) || rainAt(level, i, j, k + 1)) {
                        continue;
                    }
                    int newAge = Math.min(15, age + random.nextInt(5) / 4);
                    level.setBlock(spot, fire(level, spot, newAge), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static void burn(ServerLevel level, BlockPos pos, int chance, RandomSource random, int age) {
        int id = betaId(level, pos);
        if (random.nextInt(chance) >= BetaFire.ability(id)) {
            return;
        }
        boolean tnt = id == 46;
        if (random.nextInt(age + 10) < 5 && !rainAt(level, pos.getX(), pos.getY(), pos.getZ())) {
            int newAge = Math.min(15, age + random.nextInt(5) / 4);
            level.setBlock(pos, fire(level, pos, newAge), Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (tnt) {
            TntBlock.prime(level, pos);
        }
    }

    private static int encouragement(ServerLevel level, BlockPos pos) {
        if (betaId(level, pos) != 0) {
            return 0;
        }
        int best = 0;
        best = Math.max(best, BetaFire.encourage(betaId(level, pos.east())));
        best = Math.max(best, BetaFire.encourage(betaId(level, pos.west())));
        best = Math.max(best, BetaFire.encourage(betaId(level, pos.below())));
        best = Math.max(best, BetaFire.encourage(betaId(level, pos.above())));
        best = Math.max(best, BetaFire.encourage(betaId(level, pos.north())));
        best = Math.max(best, BetaFire.encourage(betaId(level, pos.south())));
        return best;
    }

    private static BlockState fire(ServerLevel level, BlockPos pos, int age) {
        BlockState state = BaseFireBlock.getState(level, pos);
        return state.hasProperty(FireBlock.AGE) ? state.setValue(FireBlock.AGE, age) : state;
    }
}
