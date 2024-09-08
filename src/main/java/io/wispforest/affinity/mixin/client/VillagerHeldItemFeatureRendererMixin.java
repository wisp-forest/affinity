package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerHeldItemFeatureRenderer.class)
public class VillagerHeldItemFeatureRendererMixin<T extends LivingEntity> {

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void stopHeldItemRendering(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T villager, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (AffinityComponents.ENTITY_FLAGS.get(villager).hasFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS)) {
            ci.cancel();
        }
    }

}
