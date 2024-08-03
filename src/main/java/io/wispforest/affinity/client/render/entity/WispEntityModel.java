package io.wispforest.affinity.client.render.entity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.entity.WispEntity;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;

public class WispEntityModel extends EntityModel<WispEntity> implements ModelWithHead {

    public static final EntityModelLayer LAYER = new EntityModelLayer(Affinity.id("wisp"), "main");
    private final ModelPart main;

    private int color;

    public WispEntityModel(ModelPart root) {
        this.main = root.getChild("main");
    }

    public static TexturedModelData createModelData() {
        var modelData = new ModelData();

        modelData.getRoot().addChild("main",
                ModelPartBuilder.create().uv(0, 0).cuboid(-1f, -3f, -1f, 2f, 2f, 2f),
                ModelTransform.pivot(0f, 24f, 0f));

        return TexturedModelData.of(modelData, 16, 16);
    }

    @Override
    public void setAngles(WispEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.color =
            entity.isRaving()
                ? Color.ofHsv((animationProgress * 5 % 360L) / 360f, .65f, 1f).rgb()
                : entity.type().color();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        this.main.render(matrices, vertices, light, overlay, ColorHelper.Argb.withAlpha(ColorHelper.Argb.getAlpha(color), this.color));
    }

    @Override
    public ModelPart getHead() {
        return this.main;
    }
}