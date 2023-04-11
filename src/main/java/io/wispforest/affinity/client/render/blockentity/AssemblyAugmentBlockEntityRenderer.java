package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class AssemblyAugmentBlockEntityRenderer implements BlockEntityRenderer<AssemblyAugmentBlockEntity>, RotatingItemRenderer {

    public AssemblyAugmentBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AssemblyAugmentBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var client = MinecraftClient.getInstance();

        var offset = client.player.getPos().subtract(Vec3d.ofCenter(entity.getPos()));
        double targetAngle = Math.atan2(offset.z, offset.x);

        entity.previewAngle += Delta.compute(entity.previewAngle, targetAngle, client.getLastFrameDuration() * .1);

        matrices.push();

        matrices.translate(.5, 0, .5);

        if (!entity.craftingInput().isEmpty()) {
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotation((float) (entity.previewAngle + Math.PI / 2)));

            matrices.translate(0, .5, 0);
            matrices.scale(.125f, .125f, .125f);
            matrices.translate(1, -1, 0);

            var stacks = entity.craftingInput().stacks;
            for (int i = 0; i < stacks.size(); i++) {
                matrices.push();
                matrices.translate(-(i % 3), -(i / 3), 0);
                client.getItemRenderer().renderItem(stacks.get(i), ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, client.world, 0);
                matrices.pop();
            }
        } else {
            this.renderItem(entity, matrices, vertexConsumers, entity.outputInventory().getStack(0), 5000, .5f, 0, .15f, 0, light, overlay);
        }

        matrices.pop();
    }
}
