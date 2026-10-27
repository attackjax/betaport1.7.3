package dev.b173.client.visual;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.b173.core.BetaSky;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class BetaSkyRenderer {
    private static final RenderPipeline PLANE = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath("b173", "pipeline/beta_sky_plane"))
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("Fog", UniformType.UNIFORM_BUFFER)
            .withVertexShader("core/sky")
            .withFragmentShader("core/sky")
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .build();

    private BetaSkyRenderer() {
    }

    public static final class Buffers implements AutoCloseable {
        private final Mesh above;
        private final Mesh below;
        private final Mesh stars;

        private Buffers(Mesh above, Mesh below, Mesh stars) {
            this.above = above;
            this.below = below;
            this.stars = stars;
        }

        public static Buffers build() {
            return new Buffers(Mesh.upload("Beta sky", BetaSky.skyPlane(true)),
                    Mesh.upload("Beta dark plane", BetaSky.skyPlane(false)),
                    Mesh.upload("Beta stars", BetaSky.stars()));
        }

        public void drawSky(float[] sky) {
            drawPlane(this.above, sky[0], sky[1], sky[2]);
        }

        public void drawDarkPlane(float[] sky) {
            drawPlane(this.below, sky[0] * 0.2F + 0.04F, sky[1] * 0.2F + 0.04F, sky[2] * 0.6F + 0.1F);
        }

        public void drawStars(float brightness, PoseStack poseStack) {
            Matrix4fStack modelView = RenderSystem.getModelViewStack();
            modelView.pushMatrix();
            modelView.mul(poseStack.last().pose());
            draw(RenderPipelines.STARS, this.stars, modelView, brightness, brightness, brightness, brightness);
            modelView.popMatrix();
        }

        @Override
        public void close() {
            this.above.buffer.close();
            this.below.buffer.close();
            this.stars.buffer.close();
        }
    }

    private static void drawPlane(Mesh mesh, float red, float green, float blue) {
        draw(PLANE, mesh, RenderSystem.getModelViewMatrix(), red, green, blue, 1.0F);
    }

    private static void draw(RenderPipeline pipeline, Mesh mesh, Matrix4f modelView,
                             float red, float green, float blue, float alpha) {
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(mesh.indexCount);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelView,
                new Vector4f(red, green, blue, alpha), new Vector3f(), new Matrix4f());
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Beta sky",
                target.getColorTextureView(), OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, mesh.buffer);
            pass.setIndexBuffer(indexBuffer, indices.type());
            pass.drawIndexed(0, 0, mesh.indexCount, 1);
        }
    }

    private record Mesh(GpuBuffer buffer, int indexCount) {
        static Mesh upload(String label, float[] corners) {
            VertexFormat format = DefaultVertexFormat.POSITION;
            try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(corners.length / 3 * format.getVertexSize())) {
                BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, format);
                for (int i = 0; i < corners.length; i += 3) {
                    builder.addVertex(corners[i], corners[i + 1], corners[i + 2]);
                }
                try (MeshData mesh = builder.buildOrThrow()) {
                    GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> label, GpuBuffer.USAGE_VERTEX,
                            mesh.vertexBuffer());
                    return new Mesh(buffer, mesh.drawState().indexCount());
                }
            }
        }
    }
}
