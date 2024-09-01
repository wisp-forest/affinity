package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.LocalDisplacementGatewayBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class LocalDisplacementGatewayBlockEntityRenderer implements BlockEntityRenderer<LocalDisplacementGatewayBlockEntity> {

    public LocalDisplacementGatewayBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(LocalDisplacementGatewayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var matrix = matrices.peek().getPositionMatrix();
        this.renderSides(matrix, vertexConsumers.getBuffer(RenderLayer.getEndGateway()));
    }

    private void renderSides(Matrix4f matrix, VertexConsumer vertexConsumer) {
        float min = 0.1875f, max = 1 - min;

        this.renderSide(matrix, vertexConsumer, min, max, min, max, max, max, max, max);
        this.renderSide(matrix, vertexConsumer, min, max, max, min, min, min, min, min);
        this.renderSide(matrix, vertexConsumer, max, max, max, min, min, max, max, min);
        this.renderSide(matrix, vertexConsumer, min, min, min, max, min, max, max, min);
        this.renderSide(matrix, vertexConsumer, min, max, min, min, min, min, max, max);
        this.renderSide(matrix, vertexConsumer, min, max, max, max, max, max, min, min);
    }

    private void renderSide(Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4) {
        vertices.vertex(model, x1, y1, z1);
        vertices.vertex(model, x2, y1, z2);
        vertices.vertex(model, x2, y2, z3);
        vertices.vertex(model, x1, y2, z4);
    }
}
