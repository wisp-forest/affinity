package io.wispforest.affinity.client.render.blockentity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

import java.util.Random;

public interface RotatingItemRenderer {

    Random RANDOM = new Random();

    default void renderItem(BlockEntity entity, MatrixStack matrices, VertexConsumerProvider consumers, ItemStack stack, double speedDivisor, float scale, float x, float y, float z, int light, int overlay) {
        this.renderItem(entity, matrices, consumers, stack, speedDivisor, scale, x, y, z, 0, 0, light, overlay);
    }

    default void renderItem(BlockEntity entity, MatrixStack matrices, VertexConsumerProvider consumers, ItemStack stack, double speedDivisor, float scale, float x, float y, float z, float zRotation, float bobMultiplier, int light, int overlay) {
        RANDOM.setSeed(entity.getPos().asLong());
        long time = System.currentTimeMillis() + RANDOM.nextLong(25000);

        matrices.push();

        final var client = MinecraftClient.getInstance();
        final var depthModel = client.getItemRenderer().getModel(stack, client.world, null, 0).hasDepth();
        if (depthModel) scale = scale * 1.2f;

        if (bobMultiplier != 0) {
            matrices.translate(0, Math.sin(time / speedDivisor) * bobMultiplier, 0);
        }

        matrices.translate(x, depthModel ? y - .05 : y, z);
        matrices.scale(scale, scale, scale);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) ((time / speedDivisor) % (2 * Math.PI))));

        matrices.translate(0, .125, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(zRotation));
        matrices.translate(0, -.125, 0);

        client.getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, consumers, 0);

        matrices.pop();
    }

}
