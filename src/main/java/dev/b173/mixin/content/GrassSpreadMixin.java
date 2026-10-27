package dev.b173.mixin.content;

import dev.b173.core.BetaBlocks;
import dev.b173.rules.BetaRules;
import dev.b173.world.BetaBlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpreadingSnowyBlock.class)
abstract class GrassSpreadMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void b173$betaGrass(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!state.is(Blocks.GRASS_BLOCK) || !BetaRules.betaWorld(level)) {
            return;
        }
        ci.cancel();
        BlockPos above = pos.above();
        int light = level.getMaxLocalRawBrightness(above);
        if (light < 4 && b173$opacity(level, above) > 2) {
            if (random.nextInt(4) == 0) {
                level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            }
        } else if (light >= 9) {
            BlockPos spot = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            BlockPos spotAbove = spot.above();
            if (level.getBlockState(spot).is(Blocks.DIRT) && level.getMaxLocalRawBrightness(spotAbove) >= 4
                    && b173$opacity(level, spotAbove) <= 2) {
                BlockState top = level.getBlockState(spotAbove);
                level.setBlockAndUpdate(spot, Blocks.GRASS_BLOCK.defaultBlockState()
                        .setValue(SnowyBlock.SNOWY, top.is(Blocks.SNOW) || top.is(Blocks.SNOW_BLOCK)));
            }
        }
    }

    private static int b173$opacity(ServerLevel level, BlockPos pos) {
        return BetaBlocks.lightOpacity(BetaBlockStates.id(BetaBlockStates.beta(level.getBlockState(pos))));
    }
}
