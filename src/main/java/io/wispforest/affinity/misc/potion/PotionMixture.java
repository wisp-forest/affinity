package io.wispforest.affinity.misc.potion;

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
 * or simply a list of {@link net.minecraft.entity.effect.StatusEffectInstance}s
 */
public class PotionMixture {

    public static final PotionMixture EMPTY = new PotionMixture(Potions.EMPTY, ImmutableList.of(), true);
    public static final Potion DUBIOUS_POTION = new Potion("dubious");

    private final Potion basePotion;
    private final List<StatusEffectInstance> effects;
    private final boolean pure;
    private final int color;

    public PotionMixture(Potion basePotion) {
        this.basePotion = basePotion;
        this.effects = ImmutableList.of();
        this.pure = true;

        final var colorEffects = new ArrayList<>(effects);
        if (basePotion != Potions.EMPTY) colorEffects.addAll(basePotion.getEffects());

        this.color = PotionUtil.getColor(colorEffects);
    }

    public PotionMixture(Potion basePotion, List<StatusEffectInstance> effects, boolean pure) {
        this.basePotion = basePotion;
        this.effects = ImmutableList.copyOf(effects);
        this.pure = pure;

        final var colorEffects = new ArrayList<>(effects);
        if (basePotion != Potions.EMPTY) colorEffects.addAll(basePotion.getEffects());

        this.color = PotionUtil.getColor(colorEffects);
    }

    public PotionMixture mix(PotionMixture other) {

        if (this.equals(other)) return this;

        final var effects = new ArrayList<>(this.effects);
        effects.addAll(other.effects);
        effects.addAll(basePotion.getEffects());
        effects.addAll(other.basePotion.getEffects());

        return new PotionMixture(Potions.EMPTY, effects, false);
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

        if (!effects.isEmpty()) {
            final var effectsNbt = new NbtList();
            for (var effect : effects) {
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
            if (!effects.isEmpty()) PotionUtil.setCustomPotionEffects(stack, effects);
        } else {
            PotionUtil.setPotion(stack, DUBIOUS_POTION);
        }

        return stack;
    }

    public boolean isEmpty() {
        return this == EMPTY || (basePotion == Potions.EMPTY && effects.isEmpty());
    }

    public int color() {
        return color;
    }

    public List<StatusEffectInstance> effects() {
        return effects;
    }

    public Potion basePotion() {
        return basePotion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotionMixture that = (PotionMixture) o;
        return basePotion.equals(that.basePotion) && effects.equals(that.effects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePotion, effects);
    }
}
