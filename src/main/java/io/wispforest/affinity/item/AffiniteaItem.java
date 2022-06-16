package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.UseAction;

public class AffiniteaItem extends Item {
    public AffiniteaItem() {
        super(AffinityItems.settings(0)
                .food(new FoodComponent.Builder()
                        .statusEffect(new StatusEffectInstance(AffinityStatusEffects.AFFINE, 300 * 20), 1.0F)
                        .alwaysEdible()
                        .build()));
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public SoundEvent getEatSound() {
        return getDrinkSound();
    }
}
