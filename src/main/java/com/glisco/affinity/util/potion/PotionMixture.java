package com.glisco.affinity.util.potion;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Container for a potion. This could be a {@link net.minecraft.potion.Potion} contained in {@link net.minecraft.util.registry.Registry#POTION}
 * or simply a list of {@link net.minecraft.entity.effect.StatusEffectInstance}
 */
public class PotionMixture {

    public static final Potion DUBIOUS_POTION = new Potion("dubious");

    private final Potion basePotion;
    private final List<StatusEffectInstance> customEffects;
    private final int color;
    private final boolean pure;

    public static final PotionMixture EMPTY = new PotionMixture(Potions.EMPTY, ImmutableList.of(), true);

    public PotionMixture(Potion basePotion, List<StatusEffectInstance> customEffects, boolean pure) {
        this.basePotion = basePotion;
        this.customEffects = ImmutableList.copyOf(customEffects);
        this.pure = pure;

        final var colorEffects = new ArrayList<>(customEffects);
        if (basePotion != Potions.EMPTY) colorEffects.addAll(basePotion.getEffects());

        this.color = PotionUtil.getColor(colorEffects);
    }

    public static PotionMixture fromStack(ItemStack stack) {
        final var potion = PotionUtil.getPotion(stack);
        final var effects = PotionUtil.getCustomPotionEffects(stack);

        return new PotionMixture(potion, effects, true);
    }

    public static PotionMixture fromNbt(NbtCompound nbt) {

        var potion = Potions.EMPTY;
        var effects = new ArrayList<StatusEffectInstance>();

        if (nbt.contains("Potion", NbtElement.COMPOUND_TYPE)) {
            final var potionNbt = nbt.getCompound("Potion");
            potion = Registry.POTION.get(Identifier.tryParse(potionNbt.getString("id")));
        }

        if (nbt.contains("Effects", NbtElement.LIST_TYPE)) {
            final var effectsNbt = nbt.getList("Effects", NbtElement.COMPOUND_TYPE);
            for (var effect : effectsNbt) {
                effects.add(StatusEffectInstance.fromNbt((NbtCompound) effect));
            }
        }

        return new PotionMixture(potion, effects, nbt.getBoolean("Pure"));

    }

    public NbtCompound toNbt() {
        final var nbt = new NbtCompound();

        if (basePotion != Potions.EMPTY) {
            final var potionNbt = new NbtCompound();
            potionNbt.putString("id", Registry.POTION.getId(basePotion).toString());

            nbt.put("Potion", potionNbt);
        }

        if (!customEffects.isEmpty()) {
            final var effectsNbt = new NbtList();
            for (var effect : customEffects) {
                effectsNbt.add(effect.writeNbt(new NbtCompound()));
            }

            nbt.put("Effects", effectsNbt);
        }

        nbt.putBoolean("Pure", pure);

        return nbt;
    }

    public ItemStack toStack() {
        final var stack = new ItemStack(Items.POTION);

        if (pure) {
            if (basePotion != Potions.EMPTY) PotionUtil.setPotion(stack, basePotion);
            if (!customEffects.isEmpty()) PotionUtil.setCustomPotionEffects(stack, customEffects);
        } else {
            PotionUtil.setPotion(stack, DUBIOUS_POTION);
        }

        return stack;
    }

    public PotionMixture mix(PotionMixture other) {

        if (this.equals(other)) return this;

        final var effects = new ArrayList<>(customEffects);
        effects.addAll(other.customEffects);
        effects.addAll(basePotion.getEffects());
        effects.addAll(other.basePotion.getEffects());

        return new PotionMixture(Potions.EMPTY, effects, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotionMixture that = (PotionMixture) o;
        return basePotion.equals(that.basePotion) && customEffects.equals(that.customEffects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePotion, customEffects);
    }

    public boolean isEmpty() {
        return this == EMPTY || (basePotion == Potions.EMPTY && customEffects.isEmpty());
    }

    public int getColor() {
        return color;
    }

    public List<StatusEffectInstance> getCustomEffects() {
        return customEffects;
    }

    public Potion getBasePotion() {
        return basePotion;
    }
}
