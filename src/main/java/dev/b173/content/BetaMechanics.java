package dev.b173.content;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaRandom;
import dev.b173.core.populate.BetaGrowth;
import dev.b173.world.LiveBetaWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BetaMechanics {
    private BetaMechanics() {
    }

    public static int saplingKind(BlockState state) {
        if (state.is(Blocks.OAK_SAPLING)) {
            return BetaBlocks.WOOD_OAK;
        }
        if (state.is(Blocks.SPRUCE_SAPLING)) {
            return BetaBlocks.WOOD_SPRUCE;
        }
        if (state.is(Blocks.BIRCH_SAPLING)) {
            return BetaBlocks.WOOD_BIRCH;
        }
        return -1;
    }

    public static boolean growTree(ServerLevel level, BlockPos pos, BlockState sapling, RandomSource random) {
        int kind = saplingKind(sapling);
        if (kind < 0) {
            return false;
        }
        return BetaGrowth.growTree(new LiveBetaWorld(level), new BetaRandom(random.nextLong()),
                pos.getX(), pos.getY(), pos.getZ(), kind);
    }

    public static InteractionResult boneMeal(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        boolean sapling = saplingKind(state) >= 0;
        boolean crops = state.is(Blocks.WHEAT);
        boolean grass = state.is(Blocks.GRASS_BLOCK);
        if (!sapling && !crops && !grass) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            if (sapling) {
                growTree(server, pos, state, server.getRandom());
            } else if (crops) {
                server.setBlock(pos, state.setValue(CropBlock.AGE, 7), Block.UPDATE_ALL);
            } else {
                BetaGrowth.boneMealGrass(new LiveBetaWorld(server), new BetaRandom(server.getRandom().nextLong()),
                        pos.getX(), pos.getY(), pos.getZ());
            }
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
