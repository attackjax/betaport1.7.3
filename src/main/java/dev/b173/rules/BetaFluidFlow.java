package dev.b173.rules;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaFire;
import dev.b173.world.BetaBlockStates;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public final class BetaFluidFlow {
    private static final int WATER_RATE = 5;
    private static final int LAVA_RATE = 30;

    private static final Map<ServerLevel, Long2IntOpenHashMap> HELD = new WeakHashMap<>();

    private static BlockPos selfWrite;

    private BetaFluidFlow() {
    }

    public static boolean handles(Level level, BlockState state) {
        return level instanceof ServerLevel && (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) && BetaRules.betaWorld(level);
    }

    public static int tickRate(boolean lava) {
        return lava ? LAVA_RATE : WATER_RATE;
    }

    private static BlockState state(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) ? level.getBlockState(pos) : Blocks.AIR.defaultBlockState();
    }

    private static int betaId(ServerLevel level, BlockPos pos) {
        return BetaBlockStates.id(BetaBlockStates.beta(state(level, pos)));
    }

    private static boolean sameMaterial(boolean lava, BlockState state) {
        return state.is(lava ? Blocks.LAVA : Blocks.WATER);
    }

    private static int decay(ServerLevel level, BlockPos pos, boolean lava) {
        BlockState state = state(level, pos);
        return sameMaterial(lava, state) ? state.getValue(LiquidBlock.LEVEL) : -1;
    }

    private static boolean blocksFlow(ServerLevel level, BlockPos pos) {
        int id = betaId(level, pos);
        if (id == BetaBlocks.WOODEN_DOOR || id == BetaBlocks.IRON_DOOR || id == BetaBlocks.SIGN
                || id == BetaBlocks.LADDER || id == BetaBlocks.REEDS) {
            return true;
        }
        return id != 0 && BetaBlocks.isSolid(id);
    }

    private static boolean canDisplace(ServerLevel level, BlockPos pos, boolean lava) {
        BlockState state = state(level, pos);
        if (sameMaterial(lava, state) || state.is(Blocks.LAVA)) {
            return false;
        }
        return !blocksFlow(level, pos);
    }

    private static BlockState liquid(boolean lava, int meta) {
        return (lava ? Blocks.LAVA : Blocks.WATER).defaultBlockState().setValue(LiquidBlock.LEVEL, meta);
    }

    private static Fluid fluid(BlockState state) {
        return state.getFluidState().getType();
    }

    public static void tick(ServerLevel level, BlockPos pos, BlockState state) {
        boolean lava = state.is(Blocks.LAVA);
        RandomSource random = level.getRandom();
        unhold(level, pos);
        int decay = state.getValue(LiquidBlock.LEVEL);
        int spread = lava && level.dimension() != Level.NETHER ? 2 : 1;
        boolean settle = true;
        if (decay > 0) {
            int[] sources = new int[1];
            int smallest = -100;
            smallest = smallest(level, pos.west(), lava, smallest, sources);
            smallest = smallest(level, pos.east(), lava, smallest, sources);
            smallest = smallest(level, pos.north(), lava, smallest, sources);
            smallest = smallest(level, pos.south(), lava, smallest, sources);
            int next = smallest + spread;
            if (next >= 8 || smallest < 0) {
                next = -1;
            }
            int above = decay(level, pos.above(), lava);
            if (above >= 0) {
                next = above >= 8 ? above : above + 8;
            }
            if (sources[0] >= 2 && !lava) {
                BlockState below = state(level, pos.below());
                if (BetaBlocks.isSolid(BetaBlockStates.id(BetaBlockStates.beta(below)))) {
                    next = 0;
                } else if (sameMaterial(false, below) && decay == 0) {
                    next = 0;
                }
            }
            if (lava && decay < 8 && next < 8 && next > decay && random.nextInt(4) != 0) {
                next = decay;
                settle = false;
            }
            if (next != decay) {
                decay = next;
                if (decay < 0) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    BlockState now = liquid(lava, decay);
                    selfWrite = pos;
                    try {
                        level.setBlock(pos, now, Block.UPDATE_ALL | Block.UPDATE_SKIP_ON_PLACE);
                    } finally {
                        selfWrite = null;
                    }
                    level.scheduleTick(pos, fluid(now), tickRate(lava));
                }
            } else if (!settle) {
                hold(level, pos, decay);
            }
        }
        BlockPos below = pos.below();
        if (canDisplace(level, below, lava)) {
            setLiquid(level, below, lava, decay >= 8 ? decay : decay + 8);
        } else if (decay >= 0 && (decay == 0 || blocksFlow(level, below))) {
            boolean[] directions = optimalDirections(level, pos, lava);
            int next = decay >= 8 ? 1 : decay + spread;
            if (next >= 8) {
                return;
            }
            if (directions[0]) {
                flowInto(level, pos.west(), lava, next);
            }
            if (directions[1]) {
                flowInto(level, pos.east(), lava, next);
            }
            if (directions[2]) {
                flowInto(level, pos.north(), lava, next);
            }
            if (directions[3]) {
                flowInto(level, pos.south(), lava, next);
            }
        }
    }

    private static int smallest(ServerLevel level, BlockPos pos, boolean lava, int smallest, int[] sources) {
        int decay = decay(level, pos, lava);
        if (decay < 0) {
            return smallest;
        }
        if (decay == 0) {
            sources[0]++;
        }
        if (decay >= 8) {
            decay = 0;
        }
        return smallest >= 0 && decay >= smallest ? smallest : decay;
    }

    private static void flowInto(ServerLevel level, BlockPos pos, boolean lava, int meta) {
        if (!canDisplace(level, pos, lava)) {
            return;
        }
        BlockState there = state(level, pos);
        if (!there.isAir()) {
            if (lava) {
                level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
            } else {
                Block.dropResources(there, level, pos, level.getBlockEntity(pos));
            }
        }
        setLiquid(level, pos, lava, meta);
    }

    private static void setLiquid(ServerLevel level, BlockPos pos, boolean lava, int meta) {
        if (level.hasChunkAt(pos)) {
            level.setBlock(pos, liquid(lava, meta), Block.UPDATE_ALL);
        }
    }

    private static boolean[] optimalDirections(ServerLevel level, BlockPos pos, boolean lava) {
        int[] cost = new int[4];
        for (int d = 0; d < 4; d++) {
            cost[d] = 1000;
            BlockPos side = step(pos, d);
            if (blocksFlow(level, side) || decay(level, side, lava) == 0) {
                continue;
            }
            cost[d] = blocksFlow(level, side.below()) ? flowCost(level, side, lava, 1, d) : 0;
        }
        int best = cost[0];
        for (int d = 1; d < 4; d++) {
            best = Math.min(best, cost[d]);
        }
        boolean[] optimal = new boolean[4];
        for (int d = 0; d < 4; d++) {
            optimal[d] = cost[d] == best;
        }
        return optimal;
    }

    private static int flowCost(ServerLevel level, BlockPos pos, boolean lava, int depth, int from) {
        int best = 1000;
        for (int d = 0; d < 4; d++) {
            if (d == 0 && from == 1 || d == 1 && from == 0 || d == 2 && from == 3 || d == 3 && from == 2) {
                continue;
            }
            BlockPos side = step(pos, d);
            if (blocksFlow(level, side) || decay(level, side, lava) == 0) {
                continue;
            }
            if (!blocksFlow(level, side.below())) {
                return depth;
            }
            if (depth < 4) {
                best = Math.min(best, flowCost(level, side, lava, depth + 1, d));
            }
        }
        return best;
    }

    private static BlockPos step(BlockPos pos, int d) {
        return switch (d) {
            case 0 -> pos.west();
            case 1 -> pos.east();
            case 2 -> pos.north();
            default -> pos.south();
        };
    }

    public static boolean shouldUpdate(Level level, BlockPos pos, BlockState state) {
        if (pos.equals(selfWrite)) {
            return false;
        }
        if (state.is(Blocks.LAVA) && harden(level, pos, state)) {
            return false;
        }
        return !(level instanceof ServerLevel server && held(server, pos, state));
    }

    private static boolean harden(Level level, BlockPos pos, BlockState state) {
        boolean touching = level.getBlockState(pos.north()).is(Blocks.WATER)
                || level.getBlockState(pos.south()).is(Blocks.WATER)
                || level.getBlockState(pos.west()).is(Blocks.WATER)
                || level.getBlockState(pos.east()).is(Blocks.WATER)
                || level.getBlockState(pos.above()).is(Blocks.WATER);
        if (!touching) {
            return false;
        }
        int meta = state.getValue(LiquidBlock.LEVEL);
        boolean hardened = false;
        if (meta == 0) {
            level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
            hardened = true;
        } else if (meta <= 4) {
            level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
            hardened = true;
        }
        level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
        return hardened;
    }

    public static void placed(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server && !pos.equals(selfWrite)) {
            unhold(server, pos);
        }
    }

    private static void hold(ServerLevel level, BlockPos pos, int decay) {
        HELD.computeIfAbsent(level, l -> new Long2IntOpenHashMap()).put(pos.asLong(), decay);
    }

    private static void unhold(ServerLevel level, BlockPos pos) {
        Long2IntOpenHashMap held = HELD.get(level);
        if (held != null) {
            held.remove(pos.asLong());
        }
    }

    private static boolean held(ServerLevel level, BlockPos pos, BlockState state) {
        Long2IntOpenHashMap held = HELD.get(level);
        if (held == null || !held.containsKey(pos.asLong())) {
            return false;
        }
        if (state.is(Blocks.LAVA) && state.getValue(LiquidBlock.LEVEL) == held.get(pos.asLong())) {
            return true;
        }
        held.remove(pos.asLong());
        return false;
    }

    private static boolean flowing(ServerLevel level, BlockPos pos, BlockState state) {
        return held(level, pos, state) || level.getFluidTicks().hasScheduledTick(pos, fluid(state));
    }

    public static void randomTick(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (flowing(level, pos, state)) {
            tick(level, pos, state);
            return;
        }
        if (!state.is(Blocks.LAVA) || BetaFireRules.switchedOff(level)) {
            return;
        }
        int steps = random.nextInt(3);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockPos.MutableBlockPos spot = new BlockPos.MutableBlockPos();
        for (int i = 0; i < steps; i++) {
            x += random.nextInt(3) - 1;
            y++;
            z += random.nextInt(3) - 1;
            spot.set(x, y, z);
            int id = betaId(level, spot);
            if (id == 0) {
                if (burns(level, spot.west()) || burns(level, spot.east()) || burns(level, spot.below())
                        || burns(level, spot.above()) || burns(level, spot.north()) || burns(level, spot.south())) {
                    level.setBlock(spot, BaseFireBlock.getState(level, spot), Block.UPDATE_ALL);
                    return;
                }
            } else if (BetaBlocks.isSolid(id)) {
                return;
            }
        }
    }

    private static boolean burns(ServerLevel level, BlockPos pos) {
        return BetaFire.materialBurns(betaId(level, pos));
    }
}
