package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.client.render.item.StaffRevolverRenderer;
import io.wispforest.affinity.item.StaffItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void invokeStaffRevolverRenderer(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        if (!(stack.getItem() instanceof StaffItem) || !stack.contains(StaffItem.BUNDLED_STAFFS)) return;

        StaffRevolverRenderer.INSTANCE.render(stack, mode, matrices, vertexConsumers, light, overlay);
        ci.cancel();
    }

}
