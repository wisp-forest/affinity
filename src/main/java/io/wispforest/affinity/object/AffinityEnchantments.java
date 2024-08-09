package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

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

    public static void bootstrap(Registerable<Enchantment> registerable) {
        RegistryEntryLookup<Item> items = registerable.getRegistryLookup(RegistryKeys.ITEM);

        registerable.register(CURSE_OF_ILLITERACY,
            Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE),
                2,
                1,
                Enchantment.leveledCost(1, 10),
                Enchantment.leveledCost(6, 10),
                4,
                AttributeModifierSlot.HEAD
            ))
            .addEffect(AffinityEnchantmentEffectComponents.CAUSES_ILLITERACY)
            .build(CURSE_OF_ILLITERACY.getValue()));

        registerable.register(GRAVECALLER,
            Enchantment.builder(Enchantment.definition(
                items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                1,
                1,
                Enchantment.leveledCost(1, 10),
                Enchantment.leveledCost(6, 10),
                8,
                AttributeModifierSlot.HEAD,
                AttributeModifierSlot.CHEST,
                AttributeModifierSlot.LEGS,
                AttributeModifierSlot.FEET
            ))
            .addNonListEffect(AffinityEnchantmentEffectComponents.ABSOLUTE_NAME_HUE, 205)
            .build(GRAVECALLER.getValue()));
    }

    private static RegistryKey<Enchantment> of(String id) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Affinity.id(id));
    }
}

//public class AffinityEnchantments implements AutoRegistryContainer<Enchantment> {
//
//    public static final IlliteracyCurseEnchantment CURSE_OF_ILLITERACY = new IlliteracyCurseEnchantment();
//    public static final HealthCurseEnchantment CURSE_OF_HEALTH = new HealthCurseEnchantment();
//    public static final AffineEnchantment AFFINE = new AffineEnchantment();
//    public static final EnderScourgeEnchantment ENDER_SCOURGE = new EnderScourgeEnchantment();
//    public static final BerserkerEnchantment BERSERKER = new BerserkerEnchantment();
//    public static final GravecallerEnchantment GRAVECALLER = new GravecallerEnchantment();
//    public static final BastionEnchantment BASTION = new BastionEnchantment();
//    public static final WoundingEnchantment WOUNDING = new WoundingEnchantment();
//    public static final CriticalGambleEnchantment CRITICAL_GAMBLE = new CriticalGambleEnchantment();
//    public static final ExecuteEnchantment EXECUTE = new ExecuteEnchantment();
//    public static final ProsecuteEnchantment PROSECUTE = new ProsecuteEnchantment();
//    public static final UpdogEnchantment UPDOG = new UpdogEnchantment();
//
//    @Override
//    public Registry<Enchantment> getRegistry() {
//        return Registries.ENCHANTMENT;
//    }
//
//    @Override
//    public Class<Enchantment> getTargetFieldType() {
//        return Enchantment.class;
//    }
//}
