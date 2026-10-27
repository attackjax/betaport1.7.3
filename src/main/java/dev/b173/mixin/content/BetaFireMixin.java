package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.core.BetaFire;
import dev.b173.rules.BetaFireRules;
import dev.b173.rules.BetaRules;
import dev.b173.world.BetaBlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireBlock.class)
abstract class BetaFireMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void b173$betaTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (BetaRules.betaWorld(level)) {
            ci.cancel();
            BetaFireRules.tick(level, pos, state, random);
        }
    }

    @WrapOperation(method = "onPlace", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/FireBlock;getFireTickDelay(Lnet/minecraft/util/RandomSource;)I"))
    private int b173$betaRate(RandomSource random, Operation<Integer> original, @Local(argsOnly = true) Level level) {
        return BetaRules.betaWorld(level) ? BetaFireRules.TICK_RATE : original.call(random);
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void b173$betaStay(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (level instanceof Level real ? BetaRules.betaWorld(real) : BetaRules.betaGame()) {
            cir.setReturnValue(BetaFireRules.canStay(level, pos));
        }
    }

    @Inject(method = "getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At("HEAD"), cancellable = true)
    private void b173$betaEncourage(BlockState state, CallbackInfoReturnable<Integer> cir) {
        if (BetaRules.betaGame()) {
            cir.setReturnValue(BetaFire.encourage(BetaBlockStates.id(BetaBlockStates.beta(state))));
        }
    }

    @Inject(method = "getBurnOdds", at = @At("HEAD"), cancellable = true)
    private void b173$betaAbility(BlockState state, CallbackInfoReturnable<Integer> cir) {
        if (BetaRules.betaGame()) {
            cir.setReturnValue(BetaFire.ability(BetaBlockStates.id(BetaBlockStates.beta(state))));
        }
    }
}
