package io.wispforest.affinity.client.render;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;

public class DragonDropItemRenderer {

    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3) / 2);

    public static void render(ItemStack stack, ModelTransformation.Mode mode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
        if (mode != ModelTransformation.Mode.GROUND || !stack.isOf(AffinityItems.DRAGON_DROP)) return;
        ((VertexConsumerProvider.Immediate) vertexConsumers).draw();

        float rotation = (float) ((System.currentTimeMillis() / 50d) % 20000d) / 400;
        var random = Random.create(6969);
        var buffer = vertexConsumers.getBuffer(RenderLayer.getLightning());

        matrices.push();
        matrices.translate(.5f, .5f, .5f);
        matrices.scale(.01f, .01f, .01f);

        var position = matrices.peek().getPositionMatrix();

        for (int n = 0; n < 45; ++n) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(random.nextFloat() * 360.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(random.nextFloat() * 360.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(random.nextFloat() * 360.0F + rotation * 90.0F));

            float radius = random.nextFloat() * 20 + 50;
            float width = random.nextFloat() * 2 + 2;

            sourceVertex(buffer, position, 255);
            negativeXTerminalVertex(buffer, position, radius, width);
            positiveXTerminalVertex(buffer, position, radius, width);

            sourceVertex(buffer, position, 255);
            positiveXTerminalVertex(buffer, position, radius, width);
            positiveZTerminalVertex(buffer, position, radius, width);

            sourceVertex(buffer, position, 255);
            positiveZTerminalVertex(buffer, position, radius, width);
            negativeXTerminalVertex(buffer, position, radius, width);
        }

        matrices.pop();
    }

    private static void sourceVertex(VertexConsumer buffer, Matrix4f matrix, int alpha) {
        buffer.vertex(matrix, 0, 0, 0).color(255, 255, 255, alpha).next();
    }

    private static void negativeXTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width) {
        buffer.vertex(matrix, -HALF_SQRT_3 * width, radius, -.5f * width).color(127, 0, 255, 0).next();
    }

    private static void positiveXTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width) {
        buffer.vertex(matrix, HALF_SQRT_3 * width, radius, -.5f * width).color(127, 0, 255, 0).next();
    }

    private static void positiveZTerminalVertex(VertexConsumer buffer, Matrix4f matrix, float radius, float width) {
        buffer.vertex(matrix, 0, radius, width).color(127, 0, 255, 0).next();
    }

}
