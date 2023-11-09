package io.wispforest.affinity.client.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.client.AffinityClient;
import io.wispforest.affinity.entity.EmancipatedBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.*;

import java.lang.Math;

public class EmancipatedBlockEntityRenderer extends EntityRenderer<EmancipatedBlockEntity> {

    private static final RenderLayer FIZZLE_LAYER = RenderLayer.of(
            "affinity:fizzle",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            256,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(new RenderPhase.ShaderProgram(AffinityClient.FIZZLE_PROGRAM::program))
                    .texture(RenderPhase.MIPMAP_BLOCK_ATLAS_TEXTURE)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(new RenderPhase.Overlay(false) {
                        @Override
                        public void startDrawing() {
                            RenderSystem.setupOverlayColor(() -> MinecraftClient.getInstance().getTextureManager().getTexture(Affinity.id("textures/fizzle_alpha.png")).getGlId(), 16);
                        }
                    })
                    .cull(RenderPhase.DISABLE_CULLING)
                    .build(true)
    );

    private final BlockRenderManager blockRenderManager;

    public EmancipatedBlockEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.blockRenderManager = ctx.getBlockRenderManager();
    }

    @Override
    public void render(EmancipatedBlockEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (entity.emancipatedState().getRenderType() != BlockRenderType.MODEL) return;
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        matrices.push();
        matrices.translate(-.5f, 0, -.5f);

        /*(float) (.5f + Math.sin(time / 500d % (Math.PI * 2)) * .5f)*/
        this.blockRenderManager.getModelRenderer().render(
                entity.getWorld(),
                blockRenderManager.getModel(entity.emancipatedState()),
                entity.emancipatedState(),
                BlockPos.ofFloored(entity.getX(), entity.getBoundingBox().maxY, entity.getZ()),
                matrices,
                new AlphaMaskConsumer(vertexConsumers.getBuffer(FIZZLE_LAYER), matrices.peek().getPositionMatrix(), matrices.peek().getNormalMatrix(), (float) (1 - Math.pow(1 - (entity.age + tickDelta) / entity.maxAge(), 3))),
                false,
                Random.create(),
                entity.emancipatedState().getRenderingSeed(entity.getBlockPos()),
                OverlayTexture.DEFAULT_UV
        );

        matrices.pop();
    }

    @Override
    public Identifier getTexture(EmancipatedBlockEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    private static class AlphaMaskConsumer extends FixedColorVertexConsumer {

        private final VertexConsumer delegate;
        private final Matrix4f inverseViewMatrix;
        private final Matrix3f inverseNormalMatrix;
        private final float alphaCutoff;

        private final Vector3f pos = new Vector3f();
        private final Vector3f normal = new Vector3f();
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
            this.normal.set(0f, 1f, 0f);
            this.color.set(1f);
            this.texture.set(0f);
            this.light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        }

        @Override
        public void next() {
            var blockSpaceNormal = this.inverseNormalMatrix.transform(new Vector3f(this.normal));
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
                    .normal(this.normal.x, this.normal.y, this.normal.z)
                    .next();

            this.reset();
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
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
            this.normal.set(x, y, z);
            return this;
        }
    }
}
