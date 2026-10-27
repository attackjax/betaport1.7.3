package dev.b173.client.mixin.render;

import com.mojang.blaze3d.vertex.QuadInstance;
import dev.b173.client.visual.BetaSmoothLighting;
import dev.b173.core.BetaSmoothLight;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelLighter.class)
abstract class BetaSmoothLightMixin {
    @Inject(method = "prepareQuadAmbientOcclusion", at = @At("HEAD"), cancellable = true)
    private void b173$betaSmooth(BlockAndTintGetter level, BlockState state, BlockPos pos, BakedQuad quad,
                                 QuadInstance out, CallbackInfo ci) {
        if (BetaSmoothLighting.flat(state)) {
            ci.cancel();
            ((BlockModelLighter) (Object) this).prepareQuadFlat(level, state, pos, BlockModelLighter.CHECK_LIGHT, quad, out);
            return;
        }
        if (!BetaSmoothLighting.applies(state)) {
            return;
        }
        int face = BetaSmoothLighting.face(quad.direction());
        int axis = face <= 1 ? 1 : face <= 3 ? 2 : 0;
        float[] a = new float[4];
        float[] b = new float[4];
        float first = coordinate(quad.position(0), axis);
        for (int i = 0; i < 4; i++) {
            Vector3fc p = quad.position(i);
            if (Math.abs(coordinate(p, axis) - first) > 1.0E-4F) {
                return;
            }
        }
        float minA = Float.MAX_VALUE, maxA = -Float.MAX_VALUE, minB = Float.MAX_VALUE, maxB = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            Vector3fc p = quad.position(i);
            a[i] = face <= 3 ? p.x() : p.y();
            b[i] = face <= 1 ? p.z() : face <= 3 ? p.y() : p.z();
            minA = Math.min(minA, a[i]);
            maxA = Math.max(maxA, a[i]);
            minB = Math.min(minB, b[i]);
            maxB = Math.max(maxB, b[i]);
        }
        ci.cancel();
        float[] corners = BetaSmoothLighting.corners(level, pos, state, face);
        float shade = quad.materialInfo().shade() ? BetaSmoothLight.SHADE[face] : 1.0F;
        for (int i = 0; i < 4; i++) {
            Vector3fc p = quad.position(i);
            int corner = BetaSmoothLight.corner(face, p.x(), p.y(), p.z(), minA, maxA, minB, maxB);
            out.setColor(i, ARGB.gray(shade * corners[corner]));
            out.setLightCoords(i, BetaSmoothLighting.FULL_LIGHT);
        }
    }

    private static float coordinate(Vector3fc p, int axis) {
        return axis == 0 ? p.x() : axis == 1 ? p.y() : p.z();
    }
}
