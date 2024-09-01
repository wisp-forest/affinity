package io.wispforest.affinity.client.render;

import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;

public class LightLeakRenderer {

    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3) / 2);

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Color color) {
        float rotation = (float) ((System.currentTimeMillis() / 50d) % 20000d) / 150;
        var random = Random.create(6969);
        var buffer = vertexConsumers.getBuffer(RenderLayer.getLightning());

        matrices.push();

        var position = matrices.peek().getPositionMatrix();

        for (int n = 0; n < 45; ++n) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(random.nextFloat() * 360.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(random.nextFloat() * 360.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(random.nextFloat() * 360.0F - rotation * 90.0F));

            float radius = random.nextFloat() * 20 + 50;
            float width = random.nextFloat() * 2 + 2;

            sourceVertex(buffer, position, color.alpha());
            negativeXTerminalVertex(buffer, position, radius, width, color);
            positiveXTerminalVertex(buffer, position, radius, width, color);

            sourceVertex(buffer, position, color.alpha());
            positiveXTerminalVertex(buffer, position, radius, width, color);
            positiveZTerminalVertex(buffer, position, radius, width, color);

            sourceVertex(buffer, position, color.alpha());
            positiveZTerminalVertex(buffer, position, radius, width, color);
            negativeXTerminalVertex(buffer, position, radius, width, color);
        }

        matrices.pop();
    }

    private static void sourceVertex(VertexConsumer buffer, Matrix4f matrix, float alpha) {
        buffer.vertex(matrix, 0, 0, 0).color(1f, 1f, 1f, alpha);
    }

    private static void negativeXTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width, Color color) {
        buffer.vertex(matrix, -HALF_SQRT_3 * width, radius, -.5f * width).color(color.red(), color.green(), color.blue(), 0f);
    }

    private static void positiveXTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width, Color color) {
        buffer.vertex(matrix, HALF_SQRT_3 * width, radius, -.5f * width).color(color.red(), color.green(), color.blue(), 0f);
    }

    private static void positiveZTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width, Color color) {
        buffer.vertex(matrix, 0, radius, width).color(color.red(), color.green(), color.blue(), 0f);
    }

}
