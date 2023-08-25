package io.wispforest.affinity.client.render.blockentity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class AffinityBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    private static final long MS_TO_NS = 1000000L;

    private final Map<T, TimeTrackingData> trackingData = new WeakHashMap<>();
    protected final BlockEntityRendererFactory.Context ctx;

    protected AffinityBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        float frameDelta = 0f;

        var trackingData = this.trackingData.computeIfAbsent(entity, $ -> new TimeTrackingData());
        long measuringTime = Util.getMeasuringTimeNano();

        if (!MinecraftClient.getInstance().isPaused()) {
            frameDelta = (float) ((measuringTime - trackingData.lastInvocationTime) / MS_TO_NS / 50d);
            trackingData.trackedTime += measuringTime - trackingData.lastInvocationTime;
        }

        trackingData.lastInvocationTime = measuringTime;
        this.render(entity, tickDelta, frameDelta, trackingData.trackedTime / MS_TO_NS, matrices, vertexConsumers, light, overlay);
    }

    protected abstract void render(T entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

    private static class TimeTrackingData {
        public long lastInvocationTime = 0;
        public long trackedTime = 0;
    }
}
