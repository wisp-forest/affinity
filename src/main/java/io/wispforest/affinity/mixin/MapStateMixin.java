package io.wispforest.affinity.mixin;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapState.class)
public class MapStateMixin {

    @Shadow
    @Final
    @Mutable
    private boolean showIcons;

    @Unique
    private boolean affinity$iconStateCache;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isInFrame()Z", ordinal = 2))
    private void disableFrameMarkers(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        this.affinity$iconStateCache = this.showIcons;

        if (!stack.isOf(AffinityItems.REALIZED_AETHUM_MAP)) return;
        this.showIcons = false;
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getNbt()Lnet/minecraft/nbt/NbtCompound;"))
    private void resetIconState(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        this.showIcons = this.affinity$iconStateCache;
    }

}
