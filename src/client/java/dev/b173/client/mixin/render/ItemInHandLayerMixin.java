package dev.b173.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.b173.client.model.BetaModels;
import dev.b173.client.render.BetaHeldItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
abstract class ItemInHandLayerMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void b173$betaHeldItem(ArmedEntityRenderState state, ItemStackRenderState item, ItemStack stack,
                                   HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int light,
                                   CallbackInfo ci) {
        if (!(((RenderLayer<?, ?>) (Object) this).getParentModel() instanceof HumanoidModel<?> model)
                || !BetaModels.isBeta(model.root())) {
            return;
        }
        ci.cancel();
        if (item.isEmpty()) {
            return;
        }
        ModelPart part = arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
        poseStack.pushPose();
        part.translateAndRotate(poseStack);
        if (arm == HumanoidArm.LEFT) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        BetaHeldItems.thirdPerson(poseStack, item, stack, state instanceof AvatarRenderState);
        BetaHeldItems.submitBare(item, poseStack, collector, light, state.outlineColor);
        poseStack.popPose();
    }
}
