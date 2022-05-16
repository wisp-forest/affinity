package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArrowEntity.class)
public class ArrowEntityMixin {
    @Shadow private Potion potion;
    private NbtCompound affinity$extraPotionNbt;

    @Inject(method = "initFromStack", at = @At("RETURN"))
    private void addExtraData(ItemStack stack, CallbackInfo ci) {
        if (stack.hasNbt() && stack.getNbt().contains("ExtraPotionNbt", NbtElement.COMPOUND_TYPE)) {
            affinity$extraPotionNbt = stack.getSubNbt("ExtraPotionNbt");
        }
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/Potion;getEffects()Ljava/util/List;"))
    private void doPotionApplication(LivingEntity target, CallbackInfo ci) {
        potion.getEffects().forEach(x -> MixinHooks.tryInvokePotionApplied(x, target, affinity$extraPotionNbt));
    }
}
