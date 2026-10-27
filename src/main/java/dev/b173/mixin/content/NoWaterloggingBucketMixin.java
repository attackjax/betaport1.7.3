package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.rules.BetaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BucketItem.class)
abstract class NoWaterloggingBucketMixin {
    @WrapOperation(method = "emptyContents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/LiquidBlockContainer;canPlaceLiquid(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z"))
    private boolean b173$noFill(LiquidBlockContainer container, LivingEntity user, BlockGetter getter, BlockPos pos,
            BlockState state, Fluid fluid, Operation<Boolean> original, @Local(argsOnly = true) Level level) {
        return !BetaRules.betaWorld(level) && original.call(container, user, getter, pos, state, fluid);
    }
}
