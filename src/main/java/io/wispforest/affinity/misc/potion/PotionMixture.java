package io.wispforest.affinity.misc.potion;

import com.google.common.collect.ImmutableList;
import io.wispforest.affinity.endec.BuiltInEndecs;
import io.wispforest.affinity.endec.nbt.NbtEndec;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Container for a potion. This could be a {@link net.minecraft.potion.Potion} contained in {@link net.minecraft.registry.Registries#POTION}
 * or simply a list of {@link net.minecraft.entity.effect.StatusEffectInstance}s
 */
public class PotionMixture {

    public static final Endec<PotionMixture> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.ofRegistry(Registries.POTION).fieldOf("Potion", PotionMixture::basePotion),
            NbtEndec.COMPOUND.xmap(StatusEffectInstance::fromNbt, instance -> instance.writeNbt(new NbtCompound())).listOf().optionalFieldOf("Effects", PotionMixture::effects, List.of()),
            Endec.BOOLEAN.fieldOf("Pure", mixture -> mixture.pure),
            NbtEndec.COMPOUND.optionalFieldOf("ExtraPotionData", PotionMixture::extraNbt, (NbtCompound) null),
            PotionMixture::new
    );

    public static final KeyedEndec<NbtCompound> EXTRA_DATA = NbtEndec.COMPOUND.keyed("ExtraPotionData", (NbtCompound) null);
    public static final KeyedEndec<Float> EXTEND_DURATION_BY = Endec.FLOAT.keyed("ExtendDurationBy", 1f);

    public static final PotionMixture EMPTY = new PotionMixture(Potions.EMPTY, ImmutableList.of(), true, null);
    public static final Potion DUBIOUS_POTION = new Potion("dubious");

    private final Potion basePotion;
    private final List<StatusEffectInstance> effects;
    private final boolean pure;
    private final int color;
    private NbtCompound extraNbt;

    public PotionMixture(Potion basePotion, NbtCompound extraNbt) {
        this.basePotion = basePotion;
        this.effects = ImmutableList.of();
        this.pure = true;
        this.extraNbt = extraNbt;

        final var colorEffects = new ArrayList<>(effects);
        if (basePotion != Potions.EMPTY) colorEffects.addAll(basePotion.getEffects());

        this.color = PotionUtil.getColor(colorEffects);
    }

    public PotionMixture(Potion basePotion, List<StatusEffectInstance> effects, boolean pure, NbtCompound extraNbt) {
        this.basePotion = basePotion;
        this.effects = ImmutableList.copyOf(effects);
        this.pure = pure;
        this.extraNbt = extraNbt;

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

        return new PotionMixture(Potions.EMPTY, effects, false, null);
    }

    public static PotionMixture fromStack(ItemStack stack) {
        final var potion = PotionUtil.getPotion(stack);
        final var effects = PotionUtil.getCustomPotionEffects(stack);

        return new PotionMixture(potion, effects, true, stack.get(EXTRA_DATA));
    }

    public ItemStack toStack() {
        final var stack = new ItemStack(Items.POTION);

        if (pure) {
            if (basePotion != Potions.EMPTY) PotionUtil.setPotion(stack, basePotion);
            if (!effects.isEmpty()) PotionUtil.setCustomPotionEffects(stack, effects);
        } else {
            PotionUtil.setPotion(stack, DUBIOUS_POTION);
        }

        stack.putIfNotNull(EXTRA_DATA, extraNbt);

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

    public NbtCompound extraNbt() {
        return extraNbt;
    }

    public NbtCompound getOrCreateExtraNbt() {
        if (extraNbt == null) {
            extraNbt = new NbtCompound();
        }

        return extraNbt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotionMixture that = (PotionMixture) o;

        return pure == that.pure && color == that.color
                && Objects.equals(basePotion, that.basePotion)
                && Objects.equals(effects, that.effects)
                && Objects.equals(extraNbt, that.extraNbt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePotion, effects, pure, color, extraNbt);
    }
}
