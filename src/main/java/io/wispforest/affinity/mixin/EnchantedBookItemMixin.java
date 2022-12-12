package io.wispforest.affinity.mixin;

import net.minecraft.item.EnchantedBookItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnchantedBookItem.class)
public class EnchantedBookItemMixin {

    // TODO pain 3.0
//    @Inject(method = "appendStacks",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;add(Ljava/lang/Object;)Z",
//                    shift = At.Shift.AFTER),
//            locals = LocalCapture.CAPTURE_FAILHARD)
//    private void removeAffinityEnchantments(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci, Iterator<Enchantment> var3, Enchantment enchantment) {
//        if (Registries.ENCHANTMENT.getId(enchantment).getNamespace().equals(Affinity.MOD_ID)) {
//            stacks.remove(stacks.size() - 1);
//        }
//    }

}
