package dev.b173.rules;

import dev.b173.core.BetaBiome;
import dev.b173.core.BetaBlocks;
import dev.b173.world.BetaBlockStates;
import dev.b173.world.BetaChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;

public final class BetaBlockTicks {
    private BetaBlockTicks() {
    }

    private static int betaId(BlockState state) {
        return BetaBlockStates.id(BetaBlockStates.beta(state));
    }

    public static void mushroom(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (random.nextInt(100) != 0) {
            return;
        }
        BlockPos spot = new BlockPos(pos.getX() + random.nextInt(3) - 1,
                pos.getY() + random.nextInt(2) - random.nextInt(2), pos.getZ() + random.nextInt(3) - 1);
        if (!level.isEmptyBlock(spot) || !mushroomCanStay(level, spot)) {
            return;
        }
        random.nextInt(3);
        random.nextInt(3);
        if (level.isEmptyBlock(spot) && mushroomCanStay(level, spot)) {
            level.setBlock(spot, state.getBlock().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static boolean mushroomCanStay(ServerLevel level, BlockPos pos) {
        return pos.getY() >= 0 && pos.getY() < 128 && level.getMaxLocalRawBrightness(pos) < 13
                && BetaBlocks.isOpaqueCube(betaId(level.getBlockState(pos.below())));
    }

    public static void column(ServerLevel level, BlockPos pos, BlockState state) {
        if (!level.isEmptyBlock(pos.above())) {
            return;
        }
        int height = 1;
        while (level.getBlockState(pos.below(height)).is(state.getBlock())) {
            height++;
        }
        if (height >= 3) {
            return;
        }
        var age = state.getBlock() instanceof CactusBlock ? CactusBlock.AGE : SugarCaneBlock.AGE;
        int now = state.getValue(age);
        if (now == 15) {
            level.setBlockAndUpdate(pos.above(), state.getBlock().defaultBlockState());
            level.setBlock(pos, state.setValue(age, 0), Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, state.setValue(age, now + 1), Block.UPDATE_ALL);
        }
    }

    public static void ice(ServerLevel level, BlockPos pos, BlockState state) {
        int brightest = 0;
        for (net.minecraft.core.Direction side : net.minecraft.core.Direction.values()) {
            brightest = Math.max(brightest, level.getBrightness(LightLayer.BLOCK, pos.relative(side)));
        }
        int betaLight = Math.max(0, brightest - BetaBlocks.lightOpacity(BetaBlocks.ICE));
        if (betaLight > 11 - BetaBlocks.lightOpacity(BetaBlocks.ICE)) {
            Block.dropResources(state, level, pos);
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        }
    }

    public static void weather(ServerLevel level, BlockPos random) {
        if (!(level.getChunkSource().getGenerator() instanceof BetaChunkGenerator generator)) {
            return;
        }
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, random);
        int y = top.getY();
        BetaBiome biome = generator.climate().biomeAt(top.getX(), top.getZ());
        if (!biome.snows() || y < 0 || y >= 128 || level.getBrightness(LightLayer.BLOCK, top) >= 10) {
            return;
        }
        BlockState belowState = level.getBlockState(top.below());
        int below = betaId(belowState);
        int here = betaId(level.getBlockState(top));
        if (level.isRaining() && here == 0 && below != 0 && BetaBlocks.isOpaqueCube(below)
                && BetaBlocks.isSolid(below) && below != BetaBlocks.ICE) {
            level.setBlockAndUpdate(top, Blocks.SNOW.defaultBlockState());
        }
        if (below == BetaBlocks.WATER && belowState.getFluidState().isSource()
                && !belowState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            level.setBlockAndUpdate(top.below(), Blocks.ICE.defaultBlockState());
        }
    }
}
