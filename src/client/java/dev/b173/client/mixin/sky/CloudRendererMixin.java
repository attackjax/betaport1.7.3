package dev.b173.client.mixin.sky;

import dev.b173.client.visual.BetaAtmosphere;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(CloudRenderer.class)
abstract class CloudRendererMixin {
    @Shadow
    private boolean needsRebuild;

    @Unique
    private boolean b173$beta;

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int b173$betaReach(int cloudRange) {
        boolean beta = BetaAtmosphere.cloudsBetaReach();
        if (beta != this.b173$beta) {
            this.b173$beta = beta;
            this.needsRebuild = true;
        }
        return beta ? BetaAtmosphere.CLOUD_RANGE_CHUNKS : cloudRange;
    }

    @Inject(method = "tryBuildCell", at = @At("HEAD"), cancellable = true)
    private void b173$betaCells(@Coerce Object cameraPos, ByteBuffer buffer, int cellX, int cellZ, boolean fancy,
                                int dx, int width, int dz, int height, long[] cells, CallbackInfo ci) {
        if (this.b173$beta && !BetaAtmosphere.cloudCellDrawn(fancy, dx, dz)) {
            ci.cancel();
        }
    }
}
