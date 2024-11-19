package io.wispforest.affinity.item;

import com.google.common.base.Suppliers;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class EmeraldArmorItem extends ArmorItem {

    private final Supplier<AttributeModifiersComponent> emeraldModifiers;

    public EmeraldArmorItem(Type type) {
        super(MATERIAL, type, AffinityItems.settings().maxCount(1).rarity(Rarity.UNCOMMON));

        this.emeraldModifiers = Suppliers.memoize(() -> {
            return super.getAttributeModifiers().with(
                    Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.DAMAGE_TAKEN),
                    new EntityAttributeModifier(Affinity.id("emerald_armor_damage_taken"), 1, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.forEquipmentSlot(this.getSlotType())
            ).with(
                    Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.KNOCKBACK_SUSCEPTIBILITY),
                    new EntityAttributeModifier(Affinity.id("emerald_armor_knockback_susceptibility"), 1, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.forEquipmentSlot(this.getSlotType())
            );
        });
    }

    @Override
    public AttributeModifiersComponent getAttributeModifiers() {
        return this.emeraldModifiers.get();
    }

    public static final RegistryEntry<ArmorMaterial> MATERIAL = Registry.registerReference(Registries.ARMOR_MATERIAL, Affinity.id("emerald"), new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, -1);
                map.put(ArmorItem.Type.LEGGINGS, -2);
                map.put(ArmorItem.Type.CHESTPLATE, -3);
                map.put(ArmorItem.Type.HELMET, -1);
            }),
            15,
            Registries.SOUND_EVENT.getEntry(AffinitySoundEvents.ITEM_EMERALD_ARMOR_EQUIP),
            () -> Ingredient.ofItems(Items.EMERALD),
            List.of(new ArmorMaterial.Layer(Affinity.id("emerald"))),
            -2,
            -5
    ));
}
