package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.item.AttunedShardItem;
import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "isItemBarVisible", at = @At("HEAD"), cancellable = true)
    private void injectAttunedShardBar(CallbackInfoReturnable<Boolean> cir) {
        if (!this.affinity$isDamagedShard()) return;
        cir.setReturnValue(true);
    }

    @Inject(method = "getItemBarStep", at = @At("HEAD"), cancellable = true)
    private void injectAttunedShardBarStep(CallbackInfoReturnable<Integer> cir) {
        if (!this.affinity$isDamagedShard()) return;

        float health = AttunedShardItem.getShardHealth((ItemStack) (Object) this);
        cir.setReturnValue((int) (13 * health));
    }

    @Inject(method = "getItemBarColor", at = @At("HEAD"), cancellable = true)
    private void injectAttunedShardBarColor(CallbackInfoReturnable<Integer> cir) {
        if (!this.affinity$isDamagedShard()) return;

        cir.setReturnValue(MathHelper.hsvToRgb(
                Math.max(0f, AttunedShardItem.getShardHealth((ItemStack) (Object) this)) / 3,
                1f, 1f
        ));
    }

    private boolean affinity$isDamagedShard() {
        return !AttunedShardTier.forItem(this.getItem()).isNone() && ((ItemStack) (Object) this).contains(AttunedShardItem.HEALTH);
    }

}
