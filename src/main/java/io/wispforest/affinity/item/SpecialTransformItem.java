package io.wispforest.affinity.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public interface SpecialTransformItem {

    @Environment(EnvType.CLIENT)
    void applyUseActionTransform(ItemStack stack, AbstractClientPlayerEntity player, MatrixStack matrices, float tickDelta, float swingProgress);

    @Environment(EnvType.CLIENT)
    void applyUseActionLeftArmPose(ItemStack stack, AbstractClientPlayerEntity player, PlayerEntityModel<AbstractClientPlayerEntity> model);

    @Environment(EnvType.CLIENT)
    void applyUseActionRightArmPose(ItemStack stack, AbstractClientPlayerEntity player, PlayerEntityModel<AbstractClientPlayerEntity> model);
}
