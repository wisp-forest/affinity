package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.FieldCoherenceModulatorBlockEntity;
import io.wispforest.owo.ui.util.Delta;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.RotationAxis;

public class FieldCoherenceModulatorBlockEntityRenderer extends AffinityBlockEntityRenderer<FieldCoherenceModulatorBlockEntity> implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private static final SpriteIdentifier TEXTURE_ID = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Affinity.id("block/field_coherence_modulator"));
    private static final ModelPart MAIN_BODY;

    static {
        var mainBodyData = new ModelData();
        mainBodyData.getRoot().addChild(
                "core",
                ModelPartBuilder.create()
                        .cuboid(0, 0, 0, 6, 6, 6)
                        .uv(0, 0),
                ModelTransform.NONE
        );

        MAIN_BODY = TexturedModelData.of(mainBodyData, 32, 16).createModel();
    }

    public FieldCoherenceModulatorBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected void render(FieldCoherenceModulatorBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        entity.spinSpeed += Delta.compute(entity.spinSpeed, entity.streamTargetPos() != null ? 20f : 1f, frameDelta * .05f);
        entity.spin += frameDelta * entity.spinSpeed;

        var spin = entity.spin + entity.timeOffset();
        renderSpinny(spin, matrices, vertexConsumers, light, overlay);
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        renderSpinny(System.currentTimeMillis() / 20d, matrices, vertexConsumers, light, overlay);
    }

    private static void renderSpinny(double spin, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var consumer = TEXTURE_ID.getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());

        matrices.push();
        matrices.translate(.5, .5, .5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) (spin / 5d % 360d)));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (spin % 360d)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) (spin / 10d % 360d)));
        matrices.translate(-3 / 16f, -3 / 16f, -3 / 16f);

        MAIN_BODY.render(matrices, consumer, light, overlay);

        matrices.pop();
    }
}
