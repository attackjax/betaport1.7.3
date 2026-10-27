package dev.b173.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BetaHeldItems {
    public static boolean bareModel;

    private BetaHeldItems() {
    }

    public static boolean full3d(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)
                || stack.is(Items.STICK) || stack.is(Items.BONE) || stack.is(Items.FISHING_ROD);
    }

    public static boolean rotatedAround(ItemStack stack) {
        return stack.is(Items.FISHING_ROD);
    }

    public static void thirdPerson(PoseStack poseStack, ItemStackRenderState model, ItemStack stack, boolean player) {
        poseStack.translate(-0.0625F, 0.4375F, 0.0625F);
        if (model.usesBlockLight()) {
            poseStack.translate(0.0F, 0.1875F, -0.3125F);
            poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(0.375F, -0.375F, 0.375F);
            return;
        }
        if (full3d(stack)) {
            if (player && rotatedAround(stack)) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.translate(0.0F, -0.125F, 0.0F);
            }
            poseStack.translate(0.0F, 0.1875F, 0.0F);
            poseStack.scale(0.625F, -0.625F, 0.625F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-100.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        } else {
            poseStack.translate(0.25F, 0.1875F, -0.1875F);
            poseStack.scale(0.375F, 0.375F, 0.375F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(60.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(20.0F));
        }
        flatItem(poseStack);
    }

    public static void firstPerson(PoseStack poseStack, ItemStackRenderState model, ItemStack stack, int side) {
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 45.0F));
        poseStack.scale(0.4F, 0.4F, 0.4F);
        if (rotatedAround(stack)) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
        if (!model.usesBlockLight()) {
            flatItem(poseStack);
        }
    }

    private static void flatItem(PoseStack poseStack) {
        poseStack.translate(0.0F, -0.3F, 0.0F);
        poseStack.scale(1.5F, 1.5F, 1.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(50.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(335.0F));
        poseStack.translate(-0.9375F, -0.0625F, 0.0F);
        poseStack.translate(0.5F, 0.5F, -0.03125F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    }

    public static void submitBare(ItemStackRenderState model, PoseStack poseStack, SubmitNodeCollector collector,
                                  int light, int outlineColor) {
        bareModel = true;
        try {
            model.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, outlineColor);
        } finally {
            bareModel = false;
        }
    }
}
