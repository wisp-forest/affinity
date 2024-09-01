package io.wispforest.affinity.client.render.entity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.entity.WispEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class WispEntityRenderer extends MobEntityRenderer<WispEntity, WispEntityModel> {

    public static final Identifier TEXTURE = Affinity.id("textures/entity/wisp.png");

    private final ZombieEntityModel<ZombieEntity> innerArmorModel;
    private final ZombieEntityModel<ZombieEntity> outerArmorModel;

    public WispEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new WispEntityModel(ctx.getPart(WispEntityModel.LAYER)), 0);
        this.addFeature(new HeadFeatureRenderer<>(this, ctx.getModelLoader(), ctx.getHeldItemRenderer()));

        this.innerArmorModel = new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_INNER_ARMOR));
        this.outerArmorModel = new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR));
        //noinspection rawtypes,unchecked
        this.addFeature(new ArmorFeatureRenderer(new FeatureRendererContext() {
            @Override
            public EntityModel getModel() {
                WispEntityRenderer.this.innerArmorModel.head.copyTransform(WispEntityRenderer.this.model.getHead());
                WispEntityRenderer.this.innerArmorModel.head.scale(new Vector3f(-.65f));
                WispEntityRenderer.this.innerArmorModel.head.pivotY -= 9.5f;

                return WispEntityRenderer.this.innerArmorModel;
            }

            @Override
            public Identifier getTexture(Entity entity) {
                return Identifier.of("textures/entity/zombie/zombie.png");
            }
        }, this.innerArmorModel, this.outerArmorModel, ctx.getModelManager()));
    }

    @Nullable
    @Override
    protected RenderLayer getRenderLayer(WispEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
        return super.getRenderLayer(entity, showBody, true, showOutline);
    }

    @Override
    public Identifier getTexture(WispEntity entity) {
        return TEXTURE;
    }
}
