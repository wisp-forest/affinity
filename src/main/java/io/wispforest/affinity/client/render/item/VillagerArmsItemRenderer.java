package io.wispforest.affinity.client.render.item;

import io.wispforest.affinity.client.render.blockentity.VillagerArmatureBlockEntityRenderer;
import io.wispforest.affinity.item.VillagerArmsItem;
import io.wispforest.affinity.mixin.VillagerClothingFeatureRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.Nullable;

public class VillagerArmsItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    public static final Identifier NORMIE = Identifier.ofVanilla("textures/entity/villager/villager.png");

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(-0.5, -1/7f, -0.75);

        renderArms(stack.get(VillagerArmsItem.VILLAGER_DATA), matrices, vertexConsumers, light, overlay);
    }

    public static void renderArms(@Nullable VillagerArmsItem.ArmsData data, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        renderArms(data != null ? data.unwrap() : null, matrices, vertexConsumers, light, overlay);
    }

    public static void renderArms(@Nullable VillagerData data, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        renderArms(NORMIE, matrices, vertexConsumers, light, overlay);

        if (data == null) return;
        renderArms(findTexture("type", Registries.VILLAGER_TYPE.getId(data.getType())), matrices, vertexConsumers, light, overlay);

        var profession = data.getProfession();
        if (profession == VillagerProfession.NONE) return;
        renderArms(findTexture("profession", Registries.VILLAGER_PROFESSION.getId(profession)), matrices, vertexConsumers, light, overlay);

        if (profession == VillagerProfession.NITWIT) return;
        renderArms(findTexture("profession_level", VillagerClothingFeatureRendererAccessor.affinity$LevelToIdMap().get(MathHelper.clamp(data.getLevel(), 1, VillagerClothingFeatureRendererAccessor.affinity$LevelToIdMap().size()))), matrices, vertexConsumers, light, overlay);
    }

    private static void renderArms(Identifier id, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(id));
        VillagerArmatureBlockEntityRenderer.ARMS.render(matrices, buffer, light, overlay);
    }

    private static Identifier findTexture(String keyType, Identifier keyId) {
        return keyId.withPath(path -> "textures/entity/villager/" + keyType + "/" + path + ".png");
    }
}
