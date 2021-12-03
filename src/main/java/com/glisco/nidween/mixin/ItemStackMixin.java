package com.glisco.nidween.mixin;

import com.glisco.nidween.util.potion.GlowingPotion;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void injectNameColorForIncandescence(CallbackInfoReturnable<Text> cir) {
        final var stack = (ItemStack) (Object) this;
        if (!(PotionUtil.getPotion(stack) instanceof GlowingPotion)) return;

        var color = DyeColor.byName(stack.getOrCreateNbt().getString("Color"), DyeColor.WHITE).getColorComponents();
        cir.setReturnValue(cir.getReturnValue().copy().setStyle(
                Style.EMPTY.withColor(((int) (color[0] * 255)) << 16 | ((int) (color[1] * 255)) << 8 | ((int) (color[2] * 255)))));
    }

}
