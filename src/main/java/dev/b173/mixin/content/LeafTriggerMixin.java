package dev.b173.mixin.content;

import dev.b173.rules.BetaLeaves;
import dev.b173.rules.BetaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class LeafTriggerMixin {
    @Inject(method = "affectNeighborsAfterRemoval", at = @At("HEAD"))
    private void b173$markLeaves(ServerLevel level, BlockPos pos, boolean moved, CallbackInfo ci) {
        if (BetaRules.betaWorld(level)) {
            BetaLeaves.removed(level, pos, (BlockState) (Object) this);
        }
    }
}
