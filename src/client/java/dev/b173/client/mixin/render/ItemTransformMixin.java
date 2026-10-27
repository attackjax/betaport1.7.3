package dev.b173.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.b173.client.render.BetaHeldItems;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemTransform.class)
abstract class ItemTransformMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void b173$bare(boolean leftHand, PoseStack.Pose pose, CallbackInfo ci) {
        if (BetaHeldItems.bareModel) {
            pose.translate(-0.5F, -0.5F, -0.5F);
            ci.cancel();
        }
    }
}
