package io.wispforest.affinity.client.render.item;

import io.wispforest.affinity.blockentity.impl.AzaleaChestBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class AzaleaChestItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private static final AzaleaChestBlockEntity RENDER_CHEST = new AzaleaChestBlockEntity(BlockPos.ORIGIN, AffinityBlocks.AZALEA_CHEST.getDefaultState());

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(RENDER_CHEST, matrices, vertexConsumers, light, overlay);
    }
}
