package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.callback.ReplaceAttackDamageTextCallback;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityStatusEffects;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;

public class ResoundingChimeItem extends SwordItem {

    public ResoundingChimeItem() {
        super(Material.INSTANCE, 0, -2.4f, AffinityItems.settings().maxCount(1));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var amplifier = 0;
        if (target.hasStatusEffect(AffinityStatusEffects.RESONANT)) {
            amplifier = Math.min(target.getStatusEffect(AffinityStatusEffects.RESONANT).getAmplifier() + 1, 5);
        }

        target.addStatusEffect(new StatusEffectInstance(AffinityStatusEffects.RESONANT, 100, amplifier));
        target.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 2f, 1 + amplifier * .2f);

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
        public int getMiningLevel() {return 0;}

        @Override
        public int getEnchantability() {return 0;}

        @Override
        public Ingredient getRepairIngredient() {return Ingredient.EMPTY;}
    }

}
