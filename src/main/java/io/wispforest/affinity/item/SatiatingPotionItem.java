package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.potion.PotionUtil;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Supplier;

public class SatiatingPotionItem extends Item {

    private static final int MAX_USE_TIME = 32;
    private static final Supplier<StatusEffectInstance> EFFECT = () -> new StatusEffectInstance(StatusEffects.SATURATION, 5, 0);

    public SatiatingPotionItem() {
        super(AffinityItems.settings().maxCount(1));
    }

    @Override
    public ItemStack getDefaultStack() {
        return PotionUtil.setPotion(new ItemStack(this), Registries.POTION.get(Affinity.id("saturation")));
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return MAX_USE_TIME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.playSound(AffinitySoundEvents.ITEM_SATIATING_POTION_START_DRINKING, 1f, .8f + world.random.nextFloat() * .4f);
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            user.addStatusEffect(EFFECT.get());
        }

        if (user instanceof PlayerEntity player) {
            if (player instanceof ServerPlayerEntity serverPlayer) Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
            player.incrementStat(Stats.USED.getOrCreateStat(this));

            if (!player.isCreative()) stack.decrement(1);
        }

        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        PotionUtil.buildTooltip(List.of(EFFECT.get()), tooltip, 1f, context.getUpdateTickRate());
    }

    @Override
    public SoundEvent getDrinkSound() {
        return AffinitySoundEvents.ITEM_SATIATING_POTION_DRINK;
    }
}
