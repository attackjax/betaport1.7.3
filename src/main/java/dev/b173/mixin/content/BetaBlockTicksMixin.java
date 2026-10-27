package dev.b173.mixin.content;

import dev.b173.rules.BetaBlockTicks;
import dev.b173.rules.BetaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MushroomBlock.class, CactusBlock.class, SugarCaneBlock.class, IceBlock.class})
abstract class BetaBlockTicksMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void b173$betaTicks(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        Object self = this;
        if (!BetaRules.betaWorld(level)) {
            return;
        }
        ci.cancel();
        if (self instanceof MushroomBlock) {
            BetaBlockTicks.mushroom(level, pos, state, random);
        } else if (self instanceof IceBlock) {
            BetaBlockTicks.ice(level, pos, state);
        } else {
            BetaBlockTicks.column(level, pos, state);
        }
    }
}
