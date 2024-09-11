package io.wispforest.affinity.client.render.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.VillagerArmsItem;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

public class VillagerArmatureItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var client = MinecraftClient.getInstance();

        matrices.push();
        matrices.translate(.5, .5, .5);

        client.getItemRenderer().renderItem(
            stack,
            ModelTransformationMode.NONE,
            false,
            matrices,
            vertexConsumers,
            light,
            overlay,
            client.getItemRenderer().getModels().getModelManager().getModel(Affinity.id("item/villager_armature_base"))
        );

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(0, -.5, 0);

        VillagerArmsItemRenderer.renderArms(stack.getOrDefault(VillagerArmsItem.VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1)), matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }
}
