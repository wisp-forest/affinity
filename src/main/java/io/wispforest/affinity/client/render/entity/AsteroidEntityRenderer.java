package io.wispforest.affinity.client.render.entity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.entity.AsteroidEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class AsteroidEntityRenderer extends EntityRenderer<AsteroidEntity> {

    public static final Identifier TEXTURE = Affinity.id("textures/entity/asteroid.png");

    private final AsteroidEntityModel model;

    public AsteroidEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new AsteroidEntityModel(ctx.getPart(AsteroidEntityModel.LAYER));
    }

    @Override
    public void render(AsteroidEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        matrices.push();
        matrices.translate(0, .25, 0);

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) ((System.currentTimeMillis() / 10) % 360d)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) ((System.currentTimeMillis() / 5) % 360d)));
        matrices.translate(0, -1.25, 0);

        this.model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(this.getTexture(entity))), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(AsteroidEntity entity) {
        return TEXTURE;
    }
}
