package dev.b173.client.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.rules.BetaRules;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractMinecartRenderer.class)
abstract class AbstractMinecartRendererMixin {
    @ModifyConstant(method = "oldRender", constant = @Constant(floatValue = 0.375F))
    private static float b173$betaHeight(float modern, @Local(argsOnly = true) MinecartRenderState state) {
        if (!BetaRules.clientInBetaWorld) {
            return modern;
        }
        return state.posOnRail != null ? 0.4375F : 0.35F;
    }
}
