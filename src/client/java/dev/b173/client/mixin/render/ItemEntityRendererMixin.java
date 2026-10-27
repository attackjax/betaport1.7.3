package dev.b173.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.b173.client.render.BetaItemEntityState;
import dev.b173.client.render.BetaItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
abstract class ItemEntityRendererMixin {
    @Shadow
    @Final
    private ItemModelResolver itemModelResolver;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",
            at = @At("TAIL"))
    private void b173$betaItem(ItemEntity entity, ItemEntityRenderState state, float partialTick, CallbackInfo ci) {
        if (!BetaItems.active()) {
            return;
        }
        ItemStack stack = entity.getItem();
        this.itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.NONE, entity);
        state.count = BetaItems.copies(stack.getCount());
        ((BetaItemEntityState) state).b173$setBlockScale(BetaItems.blockScale(stack, state.item));
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private void b173$betaSubmit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                 CameraRenderState camera, CallbackInfo ci) {
        if (!BetaItems.active() || state.item.isEmpty()) {
            return;
        }
        BetaItems.submit(state, ((BetaItemEntityState) state).b173$blockScale(), poseStack, collector, camera);
        ci.cancel();
    }
}
