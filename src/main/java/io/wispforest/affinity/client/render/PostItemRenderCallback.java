package io.wispforest.affinity.client.render;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface PostItemRenderCallback {

    Event<PostItemRenderCallback> EVENT = EventFactory.createArrayBacked(PostItemRenderCallback.class, callbacks -> (stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, item) -> {
        for (var callback : callbacks) {
            callback.postRender(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, item);
        }
    });

    void postRender(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, @Nullable ItemEntity item);

}
