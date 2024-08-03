package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.wispforest.affinity.misc.quack.ExtendedPotionContentsComponent;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComponentHolder.class)
public interface ComponentHolderMixin {
    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private Object mald(Object original, ComponentType<?> type) {
        if (type != DataComponentTypes.POTION_CONTENTS) return original;

        //noinspection ConstantValue
        if (original instanceof ExtendedPotionContentsComponent contents && (Object) this instanceof ItemStack stack) {
            contents.affinity$attackStack(stack);
        }

        return original;
    }
}
