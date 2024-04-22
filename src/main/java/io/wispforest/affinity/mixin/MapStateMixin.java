package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MapState.class)
public class MapStateMixin {

    @ModifyExpressionValue(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/item/map/MapState;showIcons:Z", ordinal = 1))
    private boolean disableFrameMarkers(boolean original, @Local(argsOnly = true) ItemStack stack) {
        if (!stack.isOf(AffinityItems.REALIZED_AETHUM_MAP)) return original;
        return false;
    }
}
