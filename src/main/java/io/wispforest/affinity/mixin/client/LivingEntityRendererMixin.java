package io.wispforest.affinity.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.affinity.component.AffinityComponents;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @ModifyExpressionValue(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getAnimationCounter(Lnet/minecraft/entity/LivingEntity;F)F")
    )
    private float everybodyFlashes(float original, LivingEntity entity, float f, float tickDelta) {
        var fuseProgress = entity.getComponent(AffinityComponents.INNER_CREEPER).fuseProgress(tickDelta);
        if (fuseProgress == 0) return original;

        return (int) (fuseProgress * 10f) % 2 == 0
                ? 0f
                : MathHelper.clamp(fuseProgress, .5f, 1f);
    }

    @Inject(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;scale(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/util/math/MatrixStack;F)V")
    )
    private void everybodyInflates(LivingEntity entity, float $, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int $$, CallbackInfo ci) {
        var fuseProgress = entity.getComponent(AffinityComponents.INNER_CREEPER).fuseProgress(tickDelta);
        if (fuseProgress == 0) return;

        float h = 1f + MathHelper.sin(fuseProgress * 100f) * fuseProgress * .01f;
        fuseProgress = MathHelper.clamp(fuseProgress, 0f, 1f);
        fuseProgress *= fuseProgress;
        fuseProgress *= fuseProgress;

        var horizontalScale = (1f + fuseProgress * .4f) * h;
        var verticalScale = (1f + fuseProgress * .1f) / h;
        matrixStack.scale(horizontalScale, verticalScale, horizontalScale);
    }

}
