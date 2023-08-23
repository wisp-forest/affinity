package io.wispforest.affinity.client.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Util;

import java.util.HashMap;

public class BasicVertexConsumerProvider extends VertexConsumerProvider.Immediate {
    public BasicVertexConsumerProvider(int initialBufferSize) {
        super(new BufferBuilder(initialBufferSize), Util.make(new HashMap<>(), buffers -> {
            buffers.put(TexturedRenderLayers.getEntitySolid(), new BufferBuilder(TexturedRenderLayers.getEntitySolid().getExpectedBufferSize()));
            buffers.put(TexturedRenderLayers.getEntityCutout(), new BufferBuilder(TexturedRenderLayers.getEntityCutout().getExpectedBufferSize()));
            buffers.put(TexturedRenderLayers.getBannerPatterns(), new BufferBuilder(TexturedRenderLayers.getBannerPatterns().getExpectedBufferSize()));
            buffers.put(TexturedRenderLayers.getEntityTranslucentCull(), new BufferBuilder(TexturedRenderLayers.getEntityTranslucentCull().getExpectedBufferSize()));
            buffers.put(RenderLayer.getArmorGlint(), new BufferBuilder(RenderLayer.getArmorGlint().getExpectedBufferSize()));
            buffers.put(RenderLayer.getArmorEntityGlint(), new BufferBuilder(RenderLayer.getArmorEntityGlint().getExpectedBufferSize()));
            buffers.put(RenderLayer.getGlint(), new BufferBuilder(RenderLayer.getGlint().getExpectedBufferSize()));
            buffers.put(RenderLayer.getDirectGlint(), new BufferBuilder(RenderLayer.getDirectGlint().getExpectedBufferSize()));
            buffers.put(RenderLayer.getGlintTranslucent(), new BufferBuilder(RenderLayer.getGlintTranslucent().getExpectedBufferSize()));
            buffers.put(RenderLayer.getEntityGlint(), new BufferBuilder(RenderLayer.getEntityGlint().getExpectedBufferSize()));
            buffers.put(RenderLayer.getDirectEntityGlint(), new BufferBuilder(RenderLayer.getDirectEntityGlint().getExpectedBufferSize()));
        }));
    }
}
