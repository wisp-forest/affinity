package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "setModelPose", at = @At("TAIL"))
    private void dontRenderNormalArmsWhenHoldingBetterOnes(AbstractClientPlayerEntity player, CallbackInfo ci) {
        this.model.leftArm.visible &= !player.isHolding(AffinityItems.VILLAGER_ARMS);
        this.model.rightArm.visible &= !player.isHolding(AffinityItems.VILLAGER_ARMS);
    }

}
