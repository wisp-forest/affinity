package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.item.SpecialTransformItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unchecked", "ConstantValue"})
@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {

    @Shadow
    public BipedEntityModel.ArmPose rightArmPose;

    @Shadow
    public BipedEntityModel.ArmPose leftArmPose;

    @Inject(method = "positionRightArm", at = @At("TAIL"))
    private void injectRightArmPose(T entity, CallbackInfo ci) {
        if (this.rightArmPose != BipedEntityModel.ArmPose.ITEM || !(entity instanceof AbstractClientPlayerEntity player) || !((Object) this instanceof PlayerEntityModel<?>)) {
            return;
        }

        var stack = player.getStackInHand(player.getMainArm() == Arm.RIGHT ? Hand.MAIN_HAND : Hand.OFF_HAND);
        if (player.getActiveItem() != stack || !(stack.getItem() instanceof SpecialTransformItem item)) return;

        item.applyUseActionRightArmPose(stack, player, (PlayerEntityModel<AbstractClientPlayerEntity>) (Object) this);
    }

    @Inject(method = "positionLeftArm", at = @At("TAIL"))
    private void injectLeftArmPose(T entity, CallbackInfo ci) {
        if (this.leftArmPose != BipedEntityModel.ArmPose.ITEM || !(entity instanceof AbstractClientPlayerEntity player) || !((Object) this instanceof PlayerEntityModel<?>)) {
            return;
        }

        var stack = player.getStackInHand(player.getMainArm() == Arm.LEFT ? Hand.MAIN_HAND : Hand.OFF_HAND);
        if (player.getActiveItem() != stack || !(stack.getItem() instanceof SpecialTransformItem item)) return;

        item.applyUseActionLeftArmPose(stack, player, (PlayerEntityModel<AbstractClientPlayerEntity>) (Object) this);
    }
}
