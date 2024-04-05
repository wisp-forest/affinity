package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.EtherealAethumFluxInjectorBlock;
import io.wispforest.affinity.blockentity.impl.EtherealAethumFluxInjectorBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class EtherealAethumFluxInjectorBlockEntityRenderer extends AffinityBlockEntityRenderer<EtherealAethumFluxInjectorBlockEntity> {

    public static final SpriteIdentifier SHARD_TEXTURE_ID = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Affinity.id("block/sculk_resonant_ethereal_amethyst_shard_node"));
    public static final ModelPart FLOATING_SHARD;

    static {
        var floatingShardData = new ModelData();
        floatingShardData.getRoot().addChild(
                "floating_shard",
                ModelPartBuilder.create()
                        .cuboid(0, 0, 0, 2, 4, 2)
                        .uv(0, 0),
                ModelTransform.NONE
        );

        FLOATING_SHARD = TexturedModelData.of(floatingShardData, 16, 16).createModel();
    }

    public EtherealAethumFluxInjectorBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected void render(EtherealAethumFluxInjectorBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var transform = new Matrix4f();

        transform.translate(.5f, .5f, .5f);
        transform.rotate(entity.getCachedState().get(EtherealAethumFluxInjectorBlock.FACING).getOpposite().getRotationQuaternion());
        transform.translate(-.5f, -.5f, -.5f);

        transform.translate(.5f - .0625f, -.03125f, .5f - .0625f);

        transform.translate(.0625f, .125f, .0625f);
        transform.rotate(RotationAxis.POSITIVE_Z.rotationDegrees(90));
        transform.rotate(RotationAxis.POSITIVE_X.rotationDegrees(time / 10f));
        transform.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(time));
        transform.translate(-.0625f, -.125f, -.0625f);

        entity.particle1Offset = transform.transform(new Vector4f(.0625f, -.0625f, .0625f, 1f));
        entity.particle2Offset = transform.transform(new Vector4f(.0625f, .0625f * 5, .0625f, 1f));

        matrices.push();
        matrices.multiplyPositionMatrix(transform);

        var consumer = SHARD_TEXTURE_ID.getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getSolid());
        FLOATING_SHARD.render(matrices, consumer, light, overlay);

        matrices.pop();
    }
}
