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
import net.minecraft.village.VillagerProfession;

import java.util.function.UnaryOperator;

import static net.minecraft.entity.EntityType.VILLAGER;

public class VillagerArmsItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    public static final Identifier NORMIE = Identifier.ofVanilla("textures/entity/villager/villager.png");

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(-0.5, -1/7f, -0.75);
        this.renderArms(NORMIE, matrices, vertexConsumers, light, overlay);
        var data = stack.get(VillagerArmsItem.VILLAGER_DATA);
        if (data == null) return;
        this.renderArms(this.findTexture("type", Registries.VILLAGER_TYPE.getId(data.getType())), matrices, vertexConsumers, light, overlay);
        var profession = data.getProfession();
        if (profession == VillagerProfession.NONE) return;
        this.renderArms(this.findTexture("profession", Registries.VILLAGER_PROFESSION.getId(profession)), matrices, vertexConsumers, light, overlay);
        if (profession == VillagerProfession.NITWIT) return;
        this.renderArms(this.findTexture("profession_level", VillagerClothingFeatureRendererAccessor.affinity$LevelToIdMap().get(MathHelper.clamp(data.getLevel(), 1, VillagerClothingFeatureRendererAccessor.affinity$LevelToIdMap().size()))), matrices, vertexConsumers, light, overlay);
    }

    private void renderArms(Identifier id, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(id));
        VillagerArmatureBlockEntityRenderer.ARMS.render(matrices, buffer, light, overlay);
    }

    private Identifier findTexture(String keyType, Identifier keyId) {
        return keyId.withPath(path -> "textures/entity/villager/" + keyType + "/" + path + ".png");
    }
}
