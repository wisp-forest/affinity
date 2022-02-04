package io.wispforest.affinity.client.render.entity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.entity.WispEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class WispEntityRenderer extends LivingEntityRenderer<WispEntity, WispEntityModel> {

    public static final Identifier TEXTURE = Affinity.id("textures/entity/wisp.png");

    public WispEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new WispEntityModel(ctx.getPart(WispEntityModel.LAYER)), 0);
    }

    @Nullable
    @Override
    protected RenderLayer getRenderLayer(WispEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
        return super.getRenderLayer(entity, showBody, true, showOutline);
    }

    @Override
    protected boolean hasLabel(WispEntity wisp) {
        return super.hasLabel(wisp) && (wisp.shouldRenderName() || wisp.hasCustomName() && wisp == this.dispatcher.targetedEntity);
    }

    @Override
    public Identifier getTexture(WispEntity entity) {
        return TEXTURE;
    }
}
