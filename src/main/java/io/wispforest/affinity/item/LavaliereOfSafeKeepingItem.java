package io.wispforest.affinity.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class LavaliereOfSafeKeepingItem extends TrinketItem {

    public static final AffinityEntityAddon.DataKey<Boolean> IS_EQUIPPED = AffinityEntityAddon.DataKey.withDefaultConstant(false);

    public LavaliereOfSafeKeepingItem() {
        super(AffinityItems.settings().maxCount(1));
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        AffinityEntityAddon.setData(entity, IS_EQUIPPED, true);
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        AffinityEntityAddon.removeData(entity, IS_EQUIPPED);
    }
}
