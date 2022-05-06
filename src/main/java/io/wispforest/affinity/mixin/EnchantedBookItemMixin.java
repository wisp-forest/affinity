package io.wispforest.affinity.mixin;

import io.wispforest.affinity.Affinity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(EnchantedBookItem.class)
public class EnchantedBookItemMixin {

    @Inject(method = "appendStacks",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;add(Ljava/lang/Object;)Z",
                    shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void removeAffinityEnchantments(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci, Iterator<Enchantment> var3, Enchantment enchantment) {
        if (Registry.ENCHANTMENT.getId(enchantment).getNamespace().equals(Affinity.MOD_ID)) {
            stacks.remove(stacks.size() - 1);
        }
    }

}
