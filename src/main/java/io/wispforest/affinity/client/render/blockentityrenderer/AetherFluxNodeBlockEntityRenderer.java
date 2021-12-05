package io.wispforest.affinity.client.render.blockentityrenderer;

import io.wispforest.affinity.blockentity.AetherFluxNodeBlockEntity;
import io.wispforest.affinity.blockentity.AetherNetworkMemberBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class AetherFluxNodeBlockEntityRenderer implements BlockEntityRenderer<AetherFluxNodeBlockEntity> {

    public AetherFluxNodeBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AetherFluxNodeBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        for (var linkedMember : entity.linkedMembers()) {
            var offset = Vec3d.ofCenter(linkedMember).subtract(Vec3d.of(entity.getPos()));

            vertexConsumers.getBuffer(RenderLayer.LINES)
                    .vertex(matrices.peek().getPositionMatrix(), .5f, .5f, .5f)
                    .color(0xff00ff00)
                    .normal(1, 0, 1).next();
            vertexConsumers.getBuffer(RenderLayer.LINES)
                    .vertex(matrices.peek().getPositionMatrix(), (float) offset.x, (float) offset.y, (float) offset.z)
                    .color(0xff0000ff)
                    .normal(1, 0, 1).next();
        }

        ((VertexConsumerProvider.Immediate) vertexConsumers).draw();
    }

}
