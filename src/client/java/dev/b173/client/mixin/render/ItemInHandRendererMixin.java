package dev.b173.client.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.b173.client.render.BetaHeldItems;
import dev.b173.client.render.BetaItems;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
abstract class ItemInHandRendererMixin {
    @Shadow
    @Final
    private ItemModelResolver itemModelResolver;

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void b173$betaFirstPerson(LivingEntity entity, ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                                      SubmitNodeCollector collector, int light, CallbackInfo ci) {
        if (!BetaItems.active() || stack.isEmpty()
                || (context != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && context != ItemDisplayContext.FIRST_PERSON_LEFT_HAND)) {
            return;
        }
        ci.cancel();
        ItemStackRenderState model = new ItemStackRenderState();
        this.itemModelResolver.updateForTopItem(model, stack, context, entity.level(), entity, entity.getId() + context.ordinal());
        int side = context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? 1 : -1;
        poseStack.pushPose();
        BetaHeldItems.firstPerson(poseStack, model, stack, side);
        BetaHeldItems.submitBare(model, poseStack, collector, light, 0);
        poseStack.popPose();
    }

    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemSwapScale(F)F"))
    private float b173$noCooldownDip(float scale) {
        return BetaItems.active() ? 1.0F : scale;
    }

    @ModifyExpressionValue(method = "renderArmWithItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z"))
    private boolean b173$noUseAnimation(boolean using) {
        return using && !BetaItems.active();
    }
}
