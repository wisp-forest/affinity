package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrowablePotionItem.class)
public class ThrowablePotionItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void death(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!MixinHooks.isDoomPotion(user.getStackInHand(hand))) return;

        user.damage(MixinHooks.THREW_DOOM_POTION_DAMAGE.source(world), Float.MAX_VALUE);
        user.playSound(SoundEvents.ENTITY_SPLASH_POTION_BREAK, 1, 1);

        cir.setReturnValue(TypedActionResult.success(ItemStack.EMPTY));
    }

}
