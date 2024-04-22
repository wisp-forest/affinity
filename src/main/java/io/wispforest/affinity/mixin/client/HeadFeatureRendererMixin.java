package io.wispforest.affinity.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.entity.WispEntity;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeadFeatureRenderer.class)
public class HeadFeatureRendererMixin {

    @ModifyExpressionValue(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isBaby()Z")
    )
    private boolean makeWispHeadsSmol(boolean original, @Local(argsOnly = true) LivingEntity entity) {
        return original || entity instanceof WispEntity;
    }

    @ModifyExpressionValue(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "CONSTANT", args = "floatValue=.7f")
    )
    private float actuallyMakeWispHeadsSmol(float original, @Local(argsOnly = true) LivingEntity entity) {
        if (!(entity instanceof WispEntity)) return original;
        return .35f;
    }

    @ModifyExpressionValue(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "CONSTANT", args = "floatValue=1f")
    )
    private float actuallyMakeWispHeadsSmolAndInTheProperPlace(float original, @Local(argsOnly = true) LivingEntity entity) {
        if (!(entity instanceof WispEntity)) return original;
        return 2.65f;
    }
}
