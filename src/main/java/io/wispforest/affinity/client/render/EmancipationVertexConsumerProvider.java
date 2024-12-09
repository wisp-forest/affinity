package io.wispforest.affinity.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.client.AffinityClient;
import io.wispforest.affinity.mixin.client.MultiPhaseParametersAccessor;
import io.wispforest.affinity.mixin.client.MultiPhaseRenderLayerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import org.joml.*;

import java.lang.Math;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class EmancipationVertexConsumerProvider implements VertexConsumerProvider {

    private static final VertexFormat EMANCIPATE_VERTEX_FORMAT = VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL;
    private static final BiFunction<Supplier<ShaderProgram>, RenderPhase.TextureBase, RenderLayer> EMANCIPATE_LAYER = Util.memoize((shader, texture) -> RenderLayer.of(
            "affinity:fizzle",
            EMANCIPATE_VERTEX_FORMAT,
            VertexFormat.DrawMode.QUADS,
            256,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(new RenderPhase.ShaderProgram(shader))
                    .texture(texture)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(new RenderPhase.Overlay(false) {
                        @Override
                        public void startDrawing() {
                            RenderSystem.setupOverlayColor(MinecraftClient.getInstance().getTextureManager().getTexture(Affinity.id("textures/fizzle_alpha.png")).getGlId(), 16);
                        }
                    })
                    .cull(RenderPhase.DISABLE_CULLING)
                    .build(true)
    ));

    private static final RenderLayer BLOCK_LAYER = EMANCIPATE_LAYER.apply(AffinityClient.EMANCIPATE_BLOCK_PROGRAM::program, RenderPhase.MIPMAP_BLOCK_ATLAS_TEXTURE);
    private static final Function<RenderPhase.TextureBase, RenderLayer> ENTITY_LAYER = texture -> EMANCIPATE_LAYER.apply(AffinityClient.EMANCIPATE_ENTITY_PROGRAM::program, texture);

    private final VertexConsumerProvider delegate;
    private final MatrixStack.Entry matrices;
    private final float alphaCutoff;

    public EmancipationVertexConsumerProvider(VertexConsumerProvider delegate, MatrixStack.Entry matrices, float alphaCutoff) {
        this.delegate = delegate;
        this.matrices = matrices;
        this.alphaCutoff = alphaCutoff;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        if (RenderLayer.getBlockLayers().contains(layer)) {
            return new AlphaMaskConsumer(this.delegate.getBuffer(BLOCK_LAYER), this.matrices.getPositionMatrix(), this.matrices.getNormalMatrix(), this.alphaCutoff);
        } else if (layer instanceof RenderLayer.MultiPhase multiPhase) {
            var texture = ((MultiPhaseParametersAccessor) (Object) ((MultiPhaseRenderLayerAccessor) (Object) multiPhase).affinity$getPhases()).affinity$getTexture();
            return new AlphaMaskConsumer(this.delegate.getBuffer(ENTITY_LAYER.apply(texture)), this.matrices.getPositionMatrix(), this.matrices.getNormalMatrix(), this.alphaCutoff);
        }

        return this.delegate.getBuffer(layer);
    }

    private static class AlphaMaskConsumer implements VertexConsumer {

        private final VertexConsumer delegate;
        private final Matrix4f inverseViewMatrix;
        private final Matrix3f inverseNormalMatrix;
        private final float alphaCutoff;

        private final Vector3f pos = new Vector3f();;
        private final Vector3f color = new Vector3f();
        private final Vector2f texture = new Vector2f();

        private int light;

        public AlphaMaskConsumer(VertexConsumer delegate, Matrix4f viewMatrix, Matrix3f normalMatrix, float alphaCutoff) {
            this.delegate = delegate;
            this.inverseViewMatrix = new Matrix4f(viewMatrix).invert();
            this.inverseNormalMatrix = new Matrix3f(normalMatrix).invert();
            this.alphaCutoff = alphaCutoff;

            this.reset();
        }

        private void reset() {
            this.pos.set(0f);
            this.color.set(1f);
            this.texture.set(0f);
            this.light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            this.pos.set(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.color.set(red / 255f, green / 255f, blue / 255f);
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            this.texture.set(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            this.light = u | v << 16;
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            var blockSpaceNormal = this.inverseNormalMatrix.transform(new Vector3f(x, y, z));
            var facing = Direction.getFacing(blockSpaceNormal.x(), blockSpaceNormal.y(), blockSpaceNormal.z());

            var blockSpacePos = this.inverseViewMatrix.transform(new Vector4f(this.pos, 1f));
            blockSpacePos.rotateY((float) Math.PI);
            blockSpacePos.rotateX((float) (-Math.PI / 2));
            blockSpacePos.rotate(facing.getRotationQuaternion());

            float overlayU = -blockSpacePos.x();
            float overlayV = -blockSpacePos.y();

            this.delegate
                .vertex(this.pos.x, this.pos.y, this.pos.z)
                .color(this.color.x, this.color.y, this.color.z, this.alphaCutoff)
                .texture(this.texture.x, this.texture.y)
                .overlay(Math.round(overlayU * 16), Math.round(overlayV * 16))
                .light(this.light)
                .normal(x, y, z);

            this.reset();
            return this;
        }
    }
}
