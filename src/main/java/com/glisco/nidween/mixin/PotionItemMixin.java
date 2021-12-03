package com.glisco.nidween.mixin;

import com.glisco.nidween.util.components.NidweenComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @Inject(method = "finishUsing", at = @At("RETURN"))
    private void injectColor(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        final var nbt = stack.getOrCreateNbt();
        if (!nbt.contains("Color", NbtElement.STRING_TYPE)) return;
        NidweenComponents.GLOWING_COLOR.get(user).setColor(nbt.getString("Color"));
    }

}
