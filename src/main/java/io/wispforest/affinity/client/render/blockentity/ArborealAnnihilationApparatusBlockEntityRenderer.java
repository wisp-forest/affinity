package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.ArborealAnnihilationApparatusBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.client.render.entity.WispEntityRenderer;
import io.wispforest.affinity.object.wisps.AffinityWispTypes;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ArborealAnnihilationApparatusBlockEntityRenderer extends AffinityBlockEntityRenderer<ArborealAnnihilationApparatusBlockEntity> {

    private static final ModelPart FLOATING_WISP;

    static {
        var floatingWispData = new ModelData();
        floatingWispData.getRoot().addChild(
                "main",
                ModelPartBuilder.create()
                        .cuboid(-1f, -1f, -1f, 2f, 2f, 2f)
                        .uv(0, 0),
                ModelTransform.NONE
        );

        FLOATING_WISP = TexturedModelData.of(floatingWispData, 16, 16).createModel();
    }

    public ArborealAnnihilationApparatusBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected void render(ArborealAnnihilationApparatusBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(.5, .75, .5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(time / 50f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time / 50f + 100));
        matrices.scale(1.15f, 1.15f, 1.15f);

        var wispColor = Color.ofRgb(AffinityWispTypes.VICIOUS.color());
        FLOATING_WISP.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(WispEntityRenderer.TEXTURE)), light, overlay, wispColor.red(), wispColor.green(), wispColor.blue(), 1f);

        matrices.pop();

        if (entity.beamTarget != null && entity.beamStrength >= .1f) {
            matrices.push();
            matrices.translate(.5, .75, .5);

            var targetBlock = entity.beamTarget;
            entity.beamStrength += Delta.compute(entity.beamStrength, 0, frameDelta * .5f);

            matrices.multiply(new Quaternionf().rotationTo(0, 1, 0, targetBlock.getX(), targetBlock.getY() - .4f, targetBlock.getZ()));
            matrices.scale(1, Vector3f.length(targetBlock.getX(), targetBlock.getY() - .4f, targetBlock.getZ()), 1);

            var lineBuffer = vertexConsumers.getBuffer(CuboidRenderer.BOX_LAYER);
            var lineThickness = .025f * entity.beamStrength;

            CuboidRenderer.line(matrices, lineBuffer, 0, lineThickness, 0, 0, 1, 0, Color.ofArgb(0x7f69274f), lineThickness);

            matrices.pop();

            CuboidRenderer.drawCuboid(
                    matrices,
                    vertexConsumers,
                    CuboidRenderer.Cuboid.of(
                            targetBlock,
                            targetBlock.add(1, 1, 1),
                            Color.ofRgb(0x69274f),
                            Color.ofArgb(0)
                    ),
                    entity.beamStrength
            );
        }
    }
}
