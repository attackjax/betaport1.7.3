package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaRules;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
abstract class NoWaterloggingMixin {
    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState b173$dry(BlockState state, BlockPlaceContext context) {
        if (state != null && state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED) && BetaRules.betaWorld(context.getLevel())) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }
}
