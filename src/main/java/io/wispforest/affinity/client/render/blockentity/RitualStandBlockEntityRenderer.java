package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.RitualStandBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;

public class RitualStandBlockEntityRenderer implements BlockEntityRenderer<RitualStandBlockEntity> {

    public static final ModelPart ROD;
    public static final ModelPart RECEIVER;

    private static final SpriteIdentifier AZALEA_ROD = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            Affinity.id("block/azalea_rod"));

    private static final SpriteIdentifier COPPER_RECEIVER = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            Affinity.id("block/copper_receiver"));

    static {
        ModelData rodData = new ModelData();
        rodData.getRoot().addChild("rod", ModelPartBuilder.create()
                .cuboid(0, 0, 0, 2, 6, 2)
                .uv(0, 0), ModelTransform.NONE);
        ROD = TexturedModelData.of(rodData, 16, 16).createModel();

        ModelData receiverData = new ModelData();
        receiverData.getRoot().addChild("receiver", ModelPartBuilder.create()
                .cuboid(1, 0, 1, 4, 1, 4)
                .uv(0, 10)
                .cuboid(1, 1, 0, 4, 1, 1)
                .cuboid(1, 1, 5, 4, 1, 1)
                .uv(0, 5)
                .cuboid(0, 1, 1, 1, 1, 4)
                .cuboid(5, 1, 1, 1, 1, 4), ModelTransform.NONE);

        RECEIVER = TexturedModelData.of(receiverData, 16, 16).createModel();
    }

    public RitualStandBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(RitualStandBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        final var rodConsumer = AZALEA_ROD.getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());
        final var receiverConsumer = COPPER_RECEIVER.getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());
        final double scaledTime = System.currentTimeMillis() / -5000d;

        matrices.push();

        final var offset = Math.sin(scaledTime * 3.5) * .015;
        matrices.translate(0.4375, 0.375 + offset, 0.4375);
        ROD.render(matrices, rodConsumer, light, overlay);

        matrices.translate(-0.0625 * 2, 0.4375 - offset * .25, -0.0625 * 2);

        matrices.translate(0.1875, 0, 0.1875);
        matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion((float) (scaledTime % (2 * Math.PI))));
        matrices.translate(-0.1875, 0, -0.1875);

        RECEIVER.render(matrices, receiverConsumer, light, overlay);

        matrices.pop();

        final var stack = entity.getItem();
        if (!stack.isEmpty()) {
            matrices.push();

            final var scaleFactor = stack.getItem() instanceof BlockItem ? .5f : .35f;

            matrices.translate(.5, .925, .5);
            matrices.scale(scaleFactor, scaleFactor, scaleFactor);
            matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion((float) (scaledTime * -2 % (2 * Math.PI))));

            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND,
                    light, overlay, matrices, vertexConsumers, 0);

            matrices.pop();
        }

    }
}
