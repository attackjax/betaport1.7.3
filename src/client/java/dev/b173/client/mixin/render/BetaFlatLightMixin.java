package dev.b173.client.mixin.render;

import com.mojang.blaze3d.vertex.QuadInstance;
import dev.b173.client.visual.BetaSmoothLighting;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelLighter.class)
abstract class BetaFlatLightMixin {
    @Inject(method = "prepareQuadFlat", at = @At("TAIL"))
    private void b173$betaFlat(BlockAndTintGetter level, BlockState state, BlockPos pos, int lightCoords, BakedQuad quad,
                               QuadInstance out, CallbackInfo ci) {
        BlockModelLighter self = (BlockModelLighter) (Object) this;
        Direction direction = quad.direction();
        switch (BetaSmoothLighting.flatRule(state)) {
            case FULL -> out.setLightCoords(BetaSmoothLighting.FULL_LIGHT);
            case SIDES_FACING -> {
                if (direction.getAxis().isHorizontal()) {
                    BlockPos facing = pos.relative(direction);
                    out.setLightCoords(self.getLightCoords(level.getBlockState(facing), level, facing));
                }
            }
            case TOP_ABOVE -> {
                if (direction == Direction.UP) {
                    BlockPos above = pos.above();
                    out.setLightCoords(self.getLightCoords(level.getBlockState(above), level, above));
                }
            }
            case UNSHADED -> out.setColor(ARGB.gray(1.0F));
            case SAME -> {
            }
        }
    }
}
