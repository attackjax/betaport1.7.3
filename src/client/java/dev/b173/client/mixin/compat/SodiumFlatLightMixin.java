package dev.b173.client.mixin.compat;

import dev.b173.client.visual.BetaSmoothLighting;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline", remap = false)
abstract class SodiumFlatLightMixin {
    @Shadow
    @Final
    protected LightDataAccess lightCache;

    @Inject(method = "calculate", at = @At("TAIL"), require = 0)
    private void b173$betaFlat(ModelQuadView quad, BlockPos pos, QuadLightData out, Direction cullFace,
                               Direction lightFace, boolean shade, boolean enhanced, CallbackInfo ci) {
        BetaSmoothLighting.Flat rule = BetaSmoothLighting.flatRule(this.lightCache.getLevel().getBlockState(pos));
        switch (rule) {
            case FULL -> java.util.Arrays.fill(out.lm, BetaSmoothLighting.FULL_LIGHT);
            case SIDES_FACING -> {
                if (lightFace != null && lightFace.getAxis().isHorizontal()) {
                    java.util.Arrays.fill(out.lm, LightDataAccess.getLightmap(this.lightCache.get(pos.relative(lightFace))));
                }
            }
            case TOP_ABOVE -> {
                if (lightFace == Direction.UP) {
                    java.util.Arrays.fill(out.lm, LightDataAccess.getLightmap(this.lightCache.get(pos.above())));
                }
            }
            case UNSHADED -> java.util.Arrays.fill(out.br, 1.0F);
            case SAME -> {
            }
        }
    }
}
