package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VillagerEntityRenderer.class)
public abstract class VillagerEntityRendererMixin extends MobEntityRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>> {

    public VillagerEntityRendererMixin(EntityRendererFactory.Context context, VillagerResemblingModel<VillagerEntity> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Override
    public void render(VillagerEntity villager, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        var hasNoArms = AffinityComponents.ENTITY_FLAGS.get(villager).hasFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS);

        if (hasNoArms) this.model.getChild(EntityModelPartNames.ARMS).ifPresent(modelPart -> modelPart.visible = false);
        super.render(villager, f, g, matrixStack, vertexConsumerProvider, i);
        if (hasNoArms) this.model.getChild(EntityModelPartNames.ARMS).ifPresent(modelPart -> modelPart.visible = true);
    }
}
