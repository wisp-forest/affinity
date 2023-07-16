package io.wispforest.affinity.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public interface SpecialTransformItem {

    @Environment(EnvType.CLIENT)
    void applyUseActionTransform(ItemStack stack, AbstractClientPlayerEntity player, MatrixStack matrices, float tickDelta, float swingProgress);

}
