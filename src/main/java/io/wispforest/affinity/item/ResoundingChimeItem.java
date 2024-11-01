package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.callback.ReplaceAttackDamageTextCallback;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.affinity.object.AffinityStatusEffects;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Formatting;

public class ResoundingChimeItem extends SwordItem {

    public ResoundingChimeItem() {
        super(Material.INSTANCE, AffinityItems.settings().maxCount(1).attributeModifiers(SwordItem.createAttributeModifiers(Material.INSTANCE, 0, -2.4F)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var resonantEntry = Registries.STATUS_EFFECT.getEntry(AffinityStatusEffects.RESONANT);

        var amplifier = 0;
        if (target.hasStatusEffect(resonantEntry)) {
            amplifier = Math.min(target.getStatusEffect(resonantEntry).getAmplifier() + 1, 5);
        }

        target.addStatusEffect(new StatusEffectInstance(resonantEntry, 100, amplifier));
        target.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), AffinitySoundEvents.ITEM_RESOUNDING_CHIME_DING, SoundCategory.PLAYERS, 1f, 1 + amplifier * .2f);

        return super.postHit(stack, target, attacker);
    }

    @Environment(EnvType.CLIENT)
    private static void registerTooltipAddition() {
        ReplaceAttackDamageTextCallback.EVENT.register(stack -> {
            if (!stack.isOf(AffinityItems.RESOUNDING_CHIME)) return null;

            return TextOps.withFormatting(" Stacking ", Formatting.DARK_GREEN);
        });
    }

    static {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerTooltipAddition();
        }
    }

    private enum Material implements ToolMaterial {
        INSTANCE;

        @Override
        public int getDurability() {return 234;}

        @Override
        public float getMiningSpeedMultiplier() {return 0;}

        @Override
        public float getAttackDamage() {return -1 + 1e-7f;}

        @Override
        public TagKey<Block> getInverseTag() {
            return BlockTags.INCORRECT_FOR_IRON_TOOL;
        }

        @Override
        public int getEnchantability() {return 0;}

        @Override
        public Ingredient getRepairIngredient() {return Ingredient.EMPTY;}
    }

}
