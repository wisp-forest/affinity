package io.wispforest.affinity.mixin;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.AffinityItemGroup;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.item.ItemGroup$EntriesImpl")
public class ItemGroupEntriesMixin {

    @Shadow
    @Final
    private ItemGroup group;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void yeetAffinityEnchantments(ItemStack stack, ItemGroup.StackVisibility visibility, CallbackInfo ci) {
        if (this.group == AffinityItemGroup.GROUP) return;
        if (!(stack.getItem() instanceof EnchantedBookItem)) return;

        for (var enchantment : EnchantmentHelper.get(stack).keySet()) {
            if (!Registries.ENCHANTMENT.getId(enchantment).getNamespace().equals(Affinity.MOD_ID)) continue;

            ci.cancel();
            return;
        }
    }

}
