package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtElement;
import net.minecraft.potion.PotionUtil;
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
        AffinityComponents.GLOWING_COLOR.get(user).setColor(nbt.getString("Color"));
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void doPotionApplication(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        PotionUtil.getPotionEffects(stack).forEach(x -> MixinHooks.tryInvokePotionApplied(x, user, stack.getSubNbt("ExtraPotionNbt")));
    }

}
