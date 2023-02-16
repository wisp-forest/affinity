package io.wispforest.affinity.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.MathHelper;

import java.util.UUID;

public class ArtifactBladeItem extends SwordItem {

    private final Tier tier;
    private final Multimap<EntityAttribute, EntityAttributeModifier> modifiers;

    public ArtifactBladeItem(Tier tier) {
        super(tier, 0, tier.attackSpeed, AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1).rarity(tier.rarity));
        this.tier = tier;

        var modifiers = ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder()
                .putAll(super.getAttributeModifiers(EquipmentSlot.MAINHAND));

        if (this.tier == Tier.ASTRAL) {
            modifiers.put(AffinityEntityAttributes.MAX_AETHUM, new EntityAttributeModifier(UUID.randomUUID(), "i hate attributes", 1, EntityAttributeModifier.Operation.MULTIPLY_TOTAL))
                    .put(AffinityEntityAttributes.NATURAL_AETHUM_REGEN_SPEED, new EntityAttributeModifier(UUID.randomUUID(), "i hate attributes episode 2", 1, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        this.modifiers = modifiers.build();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.modifiers : super.getAttributeModifiers(slot);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (stack.getItem() instanceof ArtifactBladeItem blade && blade.tier == Tier.ASTRAL && attacker instanceof PlayerEntity player) {
            target.setHealth(0);
            target.onDeath(DamageSource.player(player));
            return true;
        } else {
            return super.postHit(stack, target, attacker);
        }
    }

    private static MutableText makeInfiniteText() {
        var chars = Language.getInstance().get("text.affinity.infinite_attack_damage").toCharArray();
        float baseHue = (float) (System.currentTimeMillis() % 3000d) / 3000f;

        var outText = Text.literal(" ");

        for (int i = 0; i < chars.length; i++) {
            float hue = baseHue - i * .05f;
            if (hue < 0) hue += 1f;

            outText.append(TextOps.withColor(String.valueOf(chars[i]), MathHelper.hsvToRgb(hue, .8f, 1)));
        }

        return outText.append(Text.literal(" "));
    }

    static {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (!(stack.getItem() instanceof ArtifactBladeItem blade) || blade.tier != Tier.ASTRAL) return;

            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                if (!(line.getContent() instanceof LiteralTextContent) || line.getSiblings().isEmpty()) continue;


                var sibling = line.getSiblings().get(0);
                if (!(sibling.getContent() instanceof TranslatableTextContent modifierTranslatable)) continue;
                if (modifierTranslatable.getArgs().length < 2) continue;

                if (!(modifierTranslatable.getArgs()[1] instanceof Text text) || !(text.getContent() instanceof TranslatableTextContent translatable) || !translatable.getKey().startsWith("attribute.name.generic.attack_damage")) {
                    continue;
                }

                lines.set(i, makeInfiniteText().append(Text.translatable(EntityAttributes.GENERIC_ATTACK_DAMAGE.getTranslationKey()).formatted(Formatting.DARK_GREEN)));
                return;
            }
        });
    }

    public enum Tier implements ToolMaterial {
        FORGOTTEN(500, 2, 20, 7, 7f, -2.4f, Rarity.UNCOMMON),
        STABILIZED(1000, 3, 25, 8, 10f, -2.4f, Rarity.UNCOMMON),
        STRENGTHENED(1500, 4, 35, 12, 12f, -2f, Rarity.RARE),
        SUPERIOR(3000, 5, 40, 15, 15f, -1.8f, Rarity.EPIC),
        ASTRAL(69000, 6, 100, 6969, 75f, 21f, Rarity.EPIC);

        private final int durability;
        private final int miningLevel;
        private final int enchantability;
        private final float attackDamage;
        private final float miningSpeedMultiplier;
        private final float attackSpeed;
        private final Rarity rarity;

        Tier(int durability, int miningLevel, int enchantability, float attackDamage, float miningSpeedMultiplier, float attackSpeed, Rarity rarity) {
            this.durability = durability;
            this.miningLevel = miningLevel;
            this.enchantability = enchantability;
            this.attackDamage = attackDamage;
            this.miningSpeedMultiplier = miningSpeedMultiplier;
            this.attackSpeed = attackSpeed;
            this.rarity = rarity;
        }

        @Override
        public int getDurability() {
            return this.durability;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return this.miningSpeedMultiplier;
        }

        @Override
        public float getAttackDamage() {
            return this.attackDamage;
        }

        @Override
        public int getMiningLevel() {
            return this.miningLevel;
        }

        @Override
        public int getEnchantability() {
            return this.enchantability;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    }
}
