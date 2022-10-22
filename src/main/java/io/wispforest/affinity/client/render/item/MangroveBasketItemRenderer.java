package io.wispforest.affinity.client.render.item;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.wispforest.affinity.client.render.blockentity.MangroveBasketBlockEntityRenderer;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class MangroveBasketItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private final static LoadingCache<ItemStack, StackData> CACHE = CacheBuilder.newBuilder()
        .concurrencyLevel(1)
        .maximumSize(50)
        .weakKeys()
        .build(CacheLoader.from(StackData::new));

    @Override
    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();

        client.getBlockRenderManager().renderBlockAsEntity(AffinityBlocks.MANGROVE_BASKET.getDefaultState(), matrices, vertexConsumers, light, overlay);

        StackData data = CACHE.getUnchecked(stack);

        if (data.isInvalid()) {
            CACHE.refresh(stack);
            data = CACHE.getUnchecked(stack);
        }

        if (data.containedState == null) return;

        MangroveBasketBlockEntityRenderer.renderContents(data.containedState, data.containedBlockEntity, client.getTickDelta(), matrices, vertexConsumers, light, overlay);
    }

    private static class StackData {
        private final ItemStack stack;
        private final BlockState containedState;
        private final BlockEntity containedBlockEntity;
        private final NbtCompound defensiveNbtData;

        public StackData(ItemStack stack) {
            this.stack = stack;

            var nbt = BlockItem.getBlockEntityNbt(stack);

            if (nbt != null) {
                this.defensiveNbtData = nbt.copy();

                this.containedState = NbtHelper.toBlockState(nbt.getCompound("ContainedState"));
                this.containedBlockEntity = BlockEntity.createFromNbt(BlockPos.ORIGIN, containedState, nbt.getCompound("ContainedBlockEntity"));

                this.containedBlockEntity.setWorld(MinecraftClient.getInstance().world);
            } else {
                this.containedState = null;
                this.containedBlockEntity = null;
                this.defensiveNbtData = null;
            }
        }

        public boolean isInvalid() {
            return !Objects.equals(BlockItem.getBlockEntityNbt(stack), defensiveNbtData);
        }
    }
}
