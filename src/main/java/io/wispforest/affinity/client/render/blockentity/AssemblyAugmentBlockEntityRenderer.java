package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class AssemblyAugmentBlockEntityRenderer extends AffinityBlockEntityRenderer<AssemblyAugmentBlockEntity> {

    private final FloatingItemRenderer itemRenderer;

    public AssemblyAugmentBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);

        this.itemRenderer = new FloatingItemRenderer(ctx.getItemRenderer());
        this.itemRenderer.scale = .5f;
        this.itemRenderer.y = .15f;
    }

    @Override
    protected void render(AssemblyAugmentBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var client = MinecraftClient.getInstance();

        var offset = client.player.getPos().subtract(Vec3d.ofCenter(entity.getPos()));
        double targetAngle = Math.atan2(offset.z, offset.x);

        entity.previewAngle += Delta.compute(entity.previewAngle, targetAngle, frameDelta * .1);

        matrices.push();
        matrices.translate(.5, 0, .5);

        boolean inputEmpty = true;
        for (int i = 0; i < 9; i++) {
            if (!entity.inventory().getStack(i).isEmpty()) {
                inputEmpty = false;
                break;
            }
        }

        if (!inputEmpty) {
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotation((float) (entity.previewAngle + Math.PI / 2)));

            matrices.translate(0, .5, 0);
            matrices.scale(.125f, .125f, .125f);
            matrices.translate(1, -1, 0);

            var stacks = entity.inventory().heldStacks;
            for (int i = 0; i < 9; i++) {
                matrices.push();
                matrices.translate(-(i % 3), -(i / 3), 0);
                this.ctx.getItemRenderer().renderItem(stacks.get(i), ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
                matrices.pop();
            }
        } else {
            this.itemRenderer.renderFloatingItem(matrices, vertexConsumers, time, entity.getPos().asLong(), entity.inventory().getStack(AssemblyAugmentBlockEntity.OUTPUT_SLOT), entity.getWorld(), light, overlay);
        }

        matrices.pop();
    }
}
