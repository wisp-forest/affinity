package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArrowEntity.class)
public class ArrowEntityMixin {

    @Shadow private Potion potion;

    @Unique
    private @Nullable NbtCompound affinity$extraPotionNbt;

    @Inject(method = "initFromStack", at = @At("RETURN"))
    private void addExtraData(ItemStack stack, CallbackInfo ci) {
        this.affinity$extraPotionNbt = stack.get(PotionMixture.EXTRA_DATA);
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/Potion;getEffects()Ljava/util/List;"))
    private void doPotionApplication(LivingEntity target, CallbackInfo ci) {
        this.potion.getEffects().forEach(x -> MixinHooks.potionApplied(x, target, this.affinity$extraPotionNbt));
    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"), index = 0)
    private int addDuration(int duration) {
        if (this.affinity$extraPotionNbt == null) return duration;

        duration *= this.affinity$extraPotionNbt.get(PotionMixture.EXTEND_DURATION_BY);
        return duration;
    }

}
