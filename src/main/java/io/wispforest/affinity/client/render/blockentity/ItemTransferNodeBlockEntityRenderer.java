package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.ItemTransferNodeBlockEntity;
import io.wispforest.affinity.item.IridescenceWandItem;
import io.wispforest.owo.ui.util.Delta;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class ItemTransferNodeBlockEntityRenderer extends AffinityBlockEntityRenderer<ItemTransferNodeBlockEntity> {

    private static float linkAlpha = 0f;

    private final FloatingItemRenderer itemRenderer;

    public ItemTransferNodeBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);

        this.itemRenderer = new FloatingItemRenderer(ctx.getItemRenderer());
        this.itemRenderer.animationSpeed = 1.5f;
        this.itemRenderer.scale = .5f;
        this.itemRenderer.x = .5f;
        this.itemRenderer.y = .335f;
        this.itemRenderer.z = .5f;
    }

    @Override
    protected void render(ItemTransferNodeBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
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

        this.itemRenderer.renderFloatingItem(matrices, vertexConsumers, time, entity.getPos().asLong(), entity.previewItem(), entity.getWorld(), light, overlay);

        final var filterStack = entity.filterStack();
        if (!filterStack.isEmpty()) {
            final var depthModel = this.ctx.getItemRenderer().getModel(filterStack, entity.getWorld(), null, 0).hasDepth();

            matrices.translate(1, .255, .5);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            matrices.translate(-.5, 0, 0);

            if (entity.facing().getAxis() != Direction.Axis.Y) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            }

            if (depthModel) {
                matrices.translate(0, -.01, 0);
            }

            matrices.scale(.15f, .15f, .15f);
            matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(90));

            this.ctx.getItemRenderer().renderItem(filterStack, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
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
                    client.getRenderTickCounter().getLastFrameDuration() * .25f
            );
        });
    }
}
