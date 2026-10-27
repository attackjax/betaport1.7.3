package dev.b173.mixin.content;

import dev.b173.rules.BetaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
abstract class CropGrowthMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void b173$betaWheat(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!state.is(Blocks.WHEAT) || !BetaRules.betaWorld(level)) {
            return;
        }
        ci.cancel();
        CropBlock crop = (CropBlock) (Object) this;
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
            int age = crop.getAge(state);
            if (age < crop.getMaxAge()) {
                float rate = CropGrowthInvoker.b173$growthSpeed(crop, level, pos);
                if (random.nextInt((int) (100.0F / rate)) == 0) {
                    level.setBlock(pos, crop.getStateForAge(age + 1), Block.UPDATE_CLIENTS);
                }
            }
        }
    }
}
