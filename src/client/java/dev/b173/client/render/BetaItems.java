package dev.b173.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.b173.client.model.BetaModels;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SlabBlock;

import java.util.Random;

public final class BetaItems {
    private static final Random SCATTER = new Random();
    private static final RandomSource PICK = RandomSource.create(0L);

    private BetaItems() {
    }

    public static boolean active() {
        return BetaModels.packActive();
    }

    public static int copies(int count) {
        return count > 20 ? 4 : count > 5 ? 3 : count > 1 ? 2 : 1;
    }

    public static float blockScale(ItemStack stack, ItemStackRenderState model) {
        if (!model.usesBlockLight()) {
            return 0.0F;
        }
        if (stack.getItem() instanceof BlockItem item) {
            Block block = item.getBlock();
            if (block.defaultBlockState().isSolidRender() || block instanceof SlabBlock || block instanceof ChestBlock) {
                return 0.25F;
            }
        }
        return 0.5F;
    }

    public static void submit(ItemEntityRenderState state, float blockScale, PoseStack poseStack,
                              SubmitNodeCollector collector, CameraRenderState camera) {
        float age = state.ageInTicks;
        float bob = Mth.sin(age / 10.0F + state.bobOffset) * 0.1F + 0.1F;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.125F + bob, 0.0F);
        SCATTER.setSeed(187L);
        if (blockScale > 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees((age / 20.0F + state.bobOffset) * 57.295776F));
            poseStack.scale(blockScale, blockScale, blockScale);
            for (int i = 0; i < state.count; i++) {
                poseStack.pushPose();
                if (i > 0) {
                    float dx = (SCATTER.nextFloat() * 2.0F - 1.0F) * 0.2F / blockScale;
                    float dy = (SCATTER.nextFloat() * 2.0F - 1.0F) * 0.2F / blockScale;
                    float dz = (SCATTER.nextFloat() * 2.0F - 1.0F) * 0.2F / blockScale;
                    poseStack.translate(dx, dy, dz);
                }
                state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
        } else {
            Material.Baked material = state.item.pickParticleMaterial(PICK);
            if (material != null) {
                TextureAtlasSprite sprite = material.sprite();
                RenderType type = RenderTypes.entityCutout(sprite.atlasLocation());
                int light = state.lightCoords;
                poseStack.scale(0.5F, 0.5F, 0.5F);
                for (int i = 0; i < state.count; i++) {
                    poseStack.pushPose();
                    if (i > 0) {
                        float dx = (SCATTER.nextFloat() * 2.0F - 1.0F) * 0.3F;
                        float dy = (SCATTER.nextFloat() * 2.0F - 1.0F) * 0.3F;
                        float dz = (SCATTER.nextFloat() * 2.0F - 1.0F) * 0.3F;
                        poseStack.translate(dx, dy, dz);
                    }
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - camera.yRot));
                    collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                        vertex(consumer, pose, -0.5F, -0.25F, sprite.getU0(), sprite.getV1(), light);
                        vertex(consumer, pose, 0.5F, -0.25F, sprite.getU1(), sprite.getV1(), light);
                        vertex(consumer, pose, 0.5F, 0.75F, sprite.getU1(), sprite.getV0(), light);
                        vertex(consumer, pose, -0.5F, 0.75F, sprite.getU0(), sprite.getV0(), light);
                    });
                    poseStack.popPose();
                }
            }
        }
        poseStack.popPose();
    }

    private static void vertex(com.mojang.blaze3d.vertex.VertexConsumer consumer, PoseStack.Pose pose,
                               float x, float y, float u, float v, int light) {
        consumer.addVertex(pose, x, y, 0.0F).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
