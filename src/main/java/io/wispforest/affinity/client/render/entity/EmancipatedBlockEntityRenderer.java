package io.wispforest.affinity.client.render.entity;

import io.wispforest.affinity.client.render.EmancipationVertexConsumerProvider;
import io.wispforest.affinity.entity.EmancipatedBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class EmancipatedBlockEntityRenderer extends EntityRenderer<EmancipatedBlockEntity> {

    private final BlockRenderManager blockRenderManager;

    public EmancipatedBlockEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.blockRenderManager = ctx.getBlockRenderManager();
    }

    @Override
    public void render(EmancipatedBlockEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        matrices.push();
        matrices.translate(-.5f, 0, -.5f);

        float animationProgress = (float) (1 - Math.pow(1 - (entity.age + tickDelta) / entity.maxAge(), 3));
        var random = Random.create(entity.getId());

        matrices.translate(
                animationProgress * (random.nextFloat() - .5) * .35,
                animationProgress * (random.nextFloat() - .5) * .35,
                animationProgress * (random.nextFloat() - .5) * .35
        );

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(animationProgress * (random.nextFloat() - .5f) * 20f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(animationProgress * (random.nextFloat() - .5f) * 20f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(animationProgress * (random.nextFloat() - .5f) * 20f));

        if (entity.emancipatedState().getRenderType() == BlockRenderType.MODEL) {
            this.blockRenderManager.getModelRenderer().render(
                    entity.getWorld(),
                    blockRenderManager.getModel(entity.emancipatedState()),
                    entity.emancipatedState(),
                    BlockPos.ofFloored(entity.getX(), entity.getBoundingBox().maxY, entity.getZ()),
                    matrices,
                    new EmancipationVertexConsumerProvider(vertexConsumers, matrices.peek(), animationProgress).getBuffer(RenderLayers.getBlockLayer(entity.emancipatedState())),
                    false,
                    Random.create(),
                    entity.emancipatedState().getRenderingSeed(entity.getBlockPos()),
                    OverlayTexture.DEFAULT_UV
            );
        }

        if (entity.emancipatedBlockEntityData() != null) {
            if (entity.renderBlockEntity == null) {
                entity.renderBlockEntity = BlockEntity.createFromNbt(entity.getBlockPos(), entity.emancipatedState(), entity.emancipatedBlockEntityData());
                entity.renderBlockEntity.setWorld(MinecraftClient.getInstance().world);
            }

            var renderer = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(entity.renderBlockEntity);
            renderer.render(
                    entity.renderBlockEntity,
                    tickDelta,
                    matrices,
                    new EmancipationVertexConsumerProvider(vertexConsumers, matrices.peek(), animationProgress),
                    light,
                    OverlayTexture.DEFAULT_UV
            );
        }

        matrices.pop();
    }

    @Override
    public Identifier getTexture(EmancipatedBlockEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }
}
