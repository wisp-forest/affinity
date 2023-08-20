package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.FieldCoherenceModulatorBlockEntity;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.RotationAxis;

public class FieldCoherenceModulatorBlockEntityRenderer implements BlockEntityRenderer<FieldCoherenceModulatorBlockEntity> {

    public static final ModelPart CORE;

    static {
        ModelData floatingShardData = new ModelData();
        floatingShardData.getRoot().addChild("core", ModelPartBuilder.create()
                .cuboid(0, 0, 0, 6, 6, 6)
                .uv(0, 0), ModelTransform.NONE);
        CORE = TexturedModelData.of(floatingShardData, 32, 16).createModel();
    }

    public FieldCoherenceModulatorBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(FieldCoherenceModulatorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        entity.spinSpeed += Delta.compute(entity.spinSpeed, 1f, MinecraftClient.getInstance().getLastFrameDuration() * .05f);
        entity.time += MinecraftClient.getInstance().getLastFrameDuration() * entity.spinSpeed;

        var time = entity.time + entity.timeOffset;
        var consumer = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Affinity.id("block/field_coherence_modulator"))
                .getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());

        matrices.push();
        matrices.translate(.5, .5, .5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) (time / 5d % 360d)));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (time % 360d)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) (time / 10d % 360d)));
        matrices.translate(-3 / 16f, -3 / 16f, -3 / 16f);

        CORE.render(matrices, consumer, light, overlay);

        matrices.pop();
    }
}
