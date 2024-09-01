package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.enchantment.LivingEntityHealthPredicate;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.effect.value.AddEnchantmentEffect;
import net.minecraft.enchantment.effect.value.MultiplyEnchantmentEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

public class AffinityEnchantments {
    public static final RegistryKey<Enchantment> CURSE_OF_ILLITERACY = of("curse_of_illiteracy");
    public static final RegistryKey<Enchantment> CURSE_OF_HEALTH = of("curse_of_health");
    public static final RegistryKey<Enchantment> AFFINE = of("affine");
    public static final RegistryKey<Enchantment> ENDER_SCOURGE = of("ender_scourge");
    public static final RegistryKey<Enchantment> BERSERKER = of("berserker");
    public static final RegistryKey<Enchantment> GRAVECALLER = of("gravecaller");
    public static final RegistryKey<Enchantment> BASTION = of("bastion");
    public static final RegistryKey<Enchantment> WOUNDING = of("wounding");
    public static final RegistryKey<Enchantment> CRITICAL_GAMBLE = of("critical_gamble");
    public static final RegistryKey<Enchantment> EXECUTE = of("execute");
    public static final RegistryKey<Enchantment> PROSECUTE = of("prosecute");
    public static final RegistryKey<Enchantment> UPDOG = of("updog");

    public static final TagKey<EntityType<?>> END_ENTITIES = TagKey.of(RegistryKeys.ENTITY_TYPE, Affinity.id("end_entities"));
    public static final TagKey<Enchantment> CRIT_EXCLUSIVE_SET = TagKey.of(RegistryKeys.ENCHANTMENT, Affinity.id("exclusive_set/crit"));

    public static void bootstrap(Registerable<Enchantment> registerable) {
        var itemLookup = registerable.getRegistryLookup(RegistryKeys.ITEM);
        var enchantmentLookup = registerable.getRegistryLookup(RegistryKeys.ENCHANTMENT);

        registerable.register(CURSE_OF_ILLITERACY,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE),
                    2,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    4,
                    AttributeModifierSlot.HEAD
                ))
                .addEffect(AffinityEnchantmentEffectComponents.CAUSES_ILLITERACY)
                .build(CURSE_OF_ILLITERACY.getValue()));

        registerable.register(CURSE_OF_HEALTH,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE),
                    1,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    8,
                    AttributeModifierSlot.CHEST
                ))
                .build(CURSE_OF_HEALTH.getValue()));

        registerable.register(AFFINE,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                    2,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    4,
                    AttributeModifierSlot.ANY
                ))
                .addEffect(AffinityEnchantmentEffectComponents.REPAIR_WITH_AFFINE_INFUSER)
                .exclusiveSet(RegistryEntryList.of(registerable.getRegistryLookup(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.MENDING)))
                .build(AFFINE.getValue()));

        registerable.register(GRAVECALLER,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    1,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    8,
                    AttributeModifierSlot.ARMOR
                ))
                .addNonListEffect(AffinityEnchantmentEffectComponents.ABSOLUTE_NAME_HUE, 205)
                .build(GRAVECALLER.getValue()));

        registerable.register(BASTION,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    1,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    8,
                    AttributeModifierSlot.ARMOR
                ))
                .addNonListEffect(AffinityEnchantmentEffectComponents.ABSOLUTE_NAME_HUE, 160)
                .build(BASTION.getValue()));

        registerable.register(BERSERKER,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    1,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    8,
                    AttributeModifierSlot.ARMOR
                ))
                .addNonListEffect(AffinityEnchantmentEffectComponents.ABSOLUTE_NAME_HUE, 343)
                .build(BERSERKER.getValue()));

        registerable.register(WOUNDING,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                    2,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    4,
                    AttributeModifierSlot.MAINHAND
                ))
                .addNonListEffect(AffinityEnchantmentEffectComponents.INCREASES_CRIT_DAMAGE, EnchantmentLevelBasedValue.linear(.1f))
                .exclusiveSet(enchantmentLookup.getOrThrow(CRIT_EXCLUSIVE_SET))
                .build(WOUNDING.getValue()));

        registerable.register(CRITICAL_GAMBLE,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                    2,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    4,
                    AttributeModifierSlot.MAINHAND
                ))
                .addNonListEffect(AffinityEnchantmentEffectComponents.INSTANT_KILL_CHANCE, EnchantmentLevelBasedValue.linear(.01f))
                .exclusiveSet(enchantmentLookup.getOrThrow(CRIT_EXCLUSIVE_SET))
                .build(CRITICAL_GAMBLE.getValue()));

        registerable.register(ENDER_SCOURGE,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                    itemLookup.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                    2,
                    5,
                    Enchantment.leveledCost(-3, 8),
                    Enchantment.leveledCost(17, 8),
                    4,
                    AttributeModifierSlot.MAINHAND
                ))
                .exclusiveSet(registerable.getRegistryLookup(RegistryKeys.ENCHANTMENT).getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE_SET))
                .addEffect(
                    EnchantmentEffectComponentTypes.DAMAGE,
                    new AddEnchantmentEffect(EnchantmentLevelBasedValue.linear(2.5F)),
                    EntityPropertiesLootCondition.builder(
                        LootContext.EntityTarget.THIS, EntityPredicate.Builder.create().type(EntityTypePredicate.create(END_ENTITIES))
                    ))
                .build(ENDER_SCOURGE.getValue()));

        registerable.register(PROSECUTE,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                    itemLookup.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                    2,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    4,
                    AttributeModifierSlot.MAINHAND
                ))
                .exclusiveSet(RegistryEntryList.of(enchantmentLookup.getOrThrow(EXECUTE)))
                .addEffect(
                    EnchantmentEffectComponentTypes.DAMAGE,
                    new MultiplyEnchantmentEffect(EnchantmentLevelBasedValue.constant(1.2f)),
                    EntityPropertiesLootCondition.builder(
                        LootContext.EntityTarget.THIS, EntityPredicate.Builder.create().typeSpecific(new LivingEntityHealthPredicate(1f))
                    ))
                .build(PROSECUTE.getValue()));

        registerable.register(EXECUTE,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                    2,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    4,
                    AttributeModifierSlot.MAINHAND
                ))
                .addNonListEffect(AffinityEnchantmentEffectComponents.KILL_TARGET_WHEN_LOW_ON_HEALTH, EnchantmentLevelBasedValue.constant(.1f))
                .exclusiveSet(RegistryEntryList.of(enchantmentLookup.getOrThrow(PROSECUTE)))
                .build(EXECUTE.getValue()));

        registerable.register(UPDOG,
            Enchantment.builder(Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                    2,
                    3,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(6, 10),
                    8,
                    AttributeModifierSlot.MAINHAND
                ))
                .addEffect(AffinityEnchantmentEffectComponents.UPDOG_DAMAGE)
                .build(UPDOG.getValue()));
    }

    private static RegistryKey<Enchantment> of(String id) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Affinity.id(id));
    }
}