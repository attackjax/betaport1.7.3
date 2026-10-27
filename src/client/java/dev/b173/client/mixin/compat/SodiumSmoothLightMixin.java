package dev.b173.client.mixin.compat;

import dev.b173.client.visual.BetaSmoothLighting;
import dev.b173.core.BetaSmoothLight;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatFluidLightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline", remap = false)
abstract class SodiumSmoothLightMixin {
    @Shadow
    @Final
    private LightDataAccess lightCache;

    @Unique
    private FlatLightPipeline b173$flat;

    @Unique
    private FlatFluidLightPipeline b173$flatFluid;

    @Inject(method = "calculate", at = @At("HEAD"), cancellable = true, require = 0)
    private void b173$betaSmooth(ModelQuadView quad, BlockPos pos, QuadLightData out, Direction cullFace,
                                 Direction lightFace, boolean shade, boolean enhanced, CallbackInfo ci) {
        BlockState state = this.lightCache.getLevel().getBlockState(pos);
        if (BetaSmoothLighting.flat(state)) {
            boolean fluid = !state.getFluidState().isEmpty();
            if (this.b173$flat == null) {
                this.b173$flat = new FlatLightPipeline(this.lightCache);
                this.b173$flatFluid = new FlatFluidLightPipeline(this.lightCache);
            }
            ci.cancel();
            (fluid ? this.b173$flatFluid : this.b173$flat).calculate(quad, pos, out, cullFace, lightFace, shade, enhanced);
            return;
        }
        if (lightFace == null || !BetaSmoothLighting.applies(state)) {
            return;
        }
        int face = BetaSmoothLighting.face(lightFace);
        int axis = face <= 1 ? 1 : face <= 3 ? 2 : 0;
        float first = coordinate(quad, 0, axis);
        float minA = Float.MAX_VALUE, maxA = -Float.MAX_VALUE, minB = Float.MAX_VALUE, maxB = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            if (Math.abs(coordinate(quad, i, axis) - first) > 1.0E-4F) {
                return;
            }
            float a = face <= 3 ? quad.getX(i) : quad.getY(i);
            float b = face <= 1 ? quad.getZ(i) : face <= 3 ? quad.getY(i) : quad.getZ(i);
            minA = Math.min(minA, a);
            maxA = Math.max(maxA, a);
            minB = Math.min(minB, b);
            maxB = Math.max(maxB, b);
        }
        ci.cancel();
        float[] corners = BetaSmoothLighting.corners(this.lightCache.getLevel(), pos, state, face);
        float faceShade = shade ? BetaSmoothLight.SHADE[face] : 1.0F;
        for (int i = 0; i < 4; i++) {
            int corner = BetaSmoothLight.corner(face, quad.getX(i), quad.getY(i), quad.getZ(i), minA, maxA, minB, maxB);
            out.br[i] = faceShade * corners[corner];
            out.lm[i] = BetaSmoothLighting.FULL_LIGHT;
        }
    }

    private static float coordinate(ModelQuadView quad, int i, int axis) {
        return axis == 0 ? quad.getX(i) : axis == 1 ? quad.getY(i) : quad.getZ(i);
    }
}
