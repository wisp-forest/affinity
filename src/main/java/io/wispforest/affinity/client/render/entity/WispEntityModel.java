package io.wispforest.affinity.client.render.entity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.entity.WispEntity;
import io.wispforest.affinity.misc.util.MathUtil;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

public class WispEntityModel extends EntityModel<WispEntity> {

    public static final EntityModelLayer LAYER = new EntityModelLayer(Affinity.id("wisp"), "main");
    private final ModelPart main;

    private float r, g, b;

    public WispEntityModel(ModelPart root) {
        this.main = root.getChild("main");
    }

    public static TexturedModelData createModelData() {
        var modelData = new ModelData();

        modelData.getRoot().addChild("main",
                ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        return TexturedModelData.of(modelData, 16, 16);
    }

    @Override
    public void setAngles(WispEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        var colors = MathUtil.splitRGBToFloats(entity.type().color());
        this.r = colors[0];
        this.g = colors[1];
        this.b = colors[2];
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        main.render(matrices, vertices, light, overlay, this.r, this.g, this.b, alpha);
    }
}