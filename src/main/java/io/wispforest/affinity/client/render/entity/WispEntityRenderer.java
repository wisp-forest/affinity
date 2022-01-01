package io.wispforest.affinity.client.render.entity;

import io.wispforest.affinity.entity.WispEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class WispEntityRenderer extends EntityRenderer<WispEntity> {

    public WispEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(WispEntity entity) {
        return null;
    }
}
