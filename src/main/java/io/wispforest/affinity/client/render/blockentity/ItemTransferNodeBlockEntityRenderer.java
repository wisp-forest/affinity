package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.ItemTransferNodeBlockEntity;
import io.wispforest.affinity.item.IridescenceWandItem;
import io.wispforest.owo.ui.util.Delta;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class ItemTransferNodeBlockEntityRenderer implements BlockEntityRenderer<ItemTransferNodeBlockEntity>, RotatingItemRenderer {

    private static float linkAlpha = 0f;

    public ItemTransferNodeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}

    @Override
    public void render(ItemTransferNodeBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (linkAlpha > .05f) {
            var nodeLinkPos = nodeLinkPos(entity);

            for (var link : entity.links()) {
                if (!(entity.getWorld().getBlockEntity(link) instanceof ItemTransferNodeBlockEntity node)) continue;
                LinkRenderer.addLink(nodeLinkPos, nodeLinkPos(node), ((int) (0xFF * linkAlpha) << 24) | 0x00FFAB);
            }
        }

        matrices.push();

        matrices.translate(.5, .5, .5);
        matrices.multiply(entity.facing().getOpposite().getRotationQuaternion());
        matrices.translate(-.5, -.5, -.5);

        final var stack = entity.previewItem();
        if (!stack.isEmpty()) {
            this.renderItem(
                    entity, matrices, vertexConsumers, stack,
                    3000, .5f,
                    .5f,
                    .335f,
                    .5f,
                    light, overlay
            );
        }

        final var filterStack = entity.filterStack();
        if (!filterStack.isEmpty()) {
            final var client = MinecraftClient.getInstance();
            final var depthModel = client.getItemRenderer().getModel(filterStack, client.world, null, 0).hasDepth();

            matrices.translate(.5, 0, .5);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            matrices.translate(-.5, 0, -.5);

            matrices.translate(.5, .255, .5);

            if (depthModel) {
                matrices.translate(0, -.01, 0);
            }

            matrices.scale(.15f, .15f, .15f);
            matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(90));

            client.getItemRenderer().renderItem(filterStack, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, client.world, 0);
        }

        matrices.pop();
    }

    private static Vec3d nodeLinkPos(ItemTransferNodeBlockEntity node) {
        return new Vec3d(
                node.getPos().getX() + .5 + node.facing().getOffsetX() * .3,
                node.getPos().getY() + .5 + node.facing().getOffsetY() * .3,
                node.getPos().getZ() + .5 + node.facing().getOffsetZ() * .3
        );
    }

    static {
        WorldRenderEvents.LAST.register(context -> {
            var client = MinecraftClient.getInstance();
            if (client.player == null) return;

            linkAlpha += Delta.compute(
                    linkAlpha,
                    client.player.isHolding(stack -> stack.getItem() instanceof IridescenceWandItem) ? 1f : 0f,
                    client.getLastFrameDuration() * .25f
            );
        });
    }
}
