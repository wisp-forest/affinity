package io.wispforest.affinity.client.render.entity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.entity.AsteroidEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

public class AsteroidEntityModel extends EntityModel<AsteroidEntity> {

    public static final EntityModelLayer LAYER = new EntityModelLayer(Affinity.id("asteroid"), "main");

    private final ModelPart main;

    public AsteroidEntityModel(ModelPart root) {
        this.main = root.getChild("main");
    }

    public static TexturedModelData createModelData() {
        var modelData = new ModelData();

        modelData.getRoot().addChild("main",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -8f, -4f, 8f, 8f, 8f),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(AsteroidEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {}

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        this.main.render(matrices, vertices, light, overlay, color);
    }
}
