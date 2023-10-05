package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.block.impl.HolographicStereopticonBlock;
import io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity;
import io.wispforest.affinity.client.AffinityClient;
import io.wispforest.affinity.client.render.BasicVertexConsumerProvider;
import io.wispforest.affinity.client.render.PostEffectBuffer;
import io.wispforest.affinity.misc.quack.AffinityFramebufferExtension;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.Delta;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class HolographicStereopticonBlockEntityRenderer extends AffinityBlockEntityRenderer<HolographicStereopticonBlockEntity> {

    private static final PostEffectBuffer BUFFER = new PostEffectBuffer();
    private static final BasicVertexConsumerProvider VERTEX_CONSUMERS = new BasicVertexConsumerProvider(4096);

    private static boolean rendering = false;

    public HolographicStereopticonBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected void render(HolographicStereopticonBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var rotationOffset = entity.getCachedState().get(HolographicStereopticonBlock.FACING).asRotation();

        var delegate = entity.renderer();
        if (delegate != HolographicStereopticonBlockEntity.Renderer.EMPTY && delegate.ready()) {
            entity.visualRenderScale += Delta.compute(entity.visualRenderScale, entity.renderScale(), frameDelta);

            entity.currentRotation = entity.spin()
                    ? (entity.currentRotation + frameDelta * .15f) % 360f
                    : entity.currentRotation + Delta.compute(entity.currentRotation, entity.currentRotation < 180 ? 0 : 360, frameDelta * .1f);
        }

        var nested = rendering;
        if (!nested) {
            BUFFER.beginWrite(false, 0);
            matrices.push();

            rendering = true;
        }

        delegate.render(entity.visualRenderScale, entity.currentRotation - rotationOffset, matrices, VERTEX_CONSUMERS, tickDelta, light, overlay);
        VERTEX_CONSUMERS.draw();

        if (!nested) {
            matrices.pop();
            BUFFER.endWrite();

            rendering = false;
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(HolographicStereopticonBlockEntity beaconBlockEntity) {
        return true;
    }

    static {
        ((AffinityFramebufferExtension) BUFFER.buffer()).affinity$setBlitProgram(AffinityClient.DEPTH_MERGE_BLIT_PROGRAM::program);

        WorldRenderEvents.START.register(context -> BUFFER.clear());
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            AffinityClient.DEPTH_MERGE_BLIT_PROGRAM.setupSamplers(BUFFER.buffer().getDepthAttachment());
            BUFFER.draw(new Color(1f, 1f, 1f, .75f));
        });
    }
}