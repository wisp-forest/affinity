package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    private static final ItemStack AFFINITY$FILLED_MAP = new ItemStack(Items.FILLED_MAP);

    private ItemStack affinity$cachedItem = null;

    @ModifyVariable(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0), argsOnly = true)
    private ItemStack injectMap(ItemStack value) {
        if (!value.isOf(AffinityItems.AETHUM_MAP)) return value;
        this.affinity$cachedItem = value;
        return AFFINITY$FILLED_MAP;
    }

    @ModifyVariable(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1), argsOnly = true)
    private ItemStack restoreMap(ItemStack value) {
        if (this.affinity$cachedItem == null) return value;
        var item = this.affinity$cachedItem;
        this.affinity$cachedItem = null;
        return item;
    }

}
