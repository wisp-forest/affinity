package io.wispforest.affinity.misc.potion;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.wispforest.affinity.Affinity;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.serialization.format.nbt.NbtEndec;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Container for a potion. This could be a {@link net.minecraft.potion.Potion} contained in {@link net.minecraft.registry.Registries#POTION}
 * or simply a list of {@link net.minecraft.entity.effect.StatusEffectInstance}s
 */
public class PotionMixture {

    public static final Endec<PotionMixture> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.ofRegistry(Registries.POTION).fieldOf("potion", PotionMixture::basePotion),
            CodecUtils.toEndec(StatusEffectInstance.CODEC).listOf().optionalFieldOf("effects", PotionMixture::effects, List.of()),
            Endec.BOOLEAN.fieldOf("pure", mixture -> mixture.pure),
            CodecUtils.toEndec(ComponentMap.CODEC).optionalFieldOf("extra_potion_components", PotionMixture::extraComponents, ComponentMap.EMPTY),
            PotionMixture::new
    );

    public static final ComponentType<Float> EXTEND_DURATION_BY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Affinity.id("extend_potion_duration_by"),
            ComponentType.<Float>builder().codec(Codec.FLOAT).packetCodec(PacketCodecs.FLOAT).build()
    );

    public static final PotionMixture EMPTY = new PotionMixture(Potions.WATER.value(), ImmutableList.of(), true, ComponentMap.EMPTY);
    public static final Potion DUBIOUS_POTION = new Potion("dubious");

    private final Potion basePotion;
    private final List<StatusEffectInstance> effects;
    private final boolean pure;
    private final int color;
    private final ComponentMap extraComponents;

    public PotionMixture(Potion basePotion, ComponentMap extraComponents) {
        this.basePotion = basePotion;
        this.effects = ImmutableList.of();
        this.pure = true;
        this.extraComponents = extraComponents;

        final var colorEffects = new ArrayList<>(effects);
        if (basePotion != Potions.WATER.value()) colorEffects.addAll(basePotion.getEffects());

        this.color = PotionContentsComponent.getColor(colorEffects);
    }

    public PotionMixture(Potion basePotion, List<StatusEffectInstance> effects, boolean pure, ComponentMap extraComponents) {
        this.basePotion = basePotion;
        this.effects = ImmutableList.copyOf(effects);
        this.pure = pure;
        this.extraComponents = extraComponents;

        final var colorEffects = new ArrayList<>(effects);
        if (basePotion != Potions.WATER.value()) colorEffects.addAll(basePotion.getEffects());

        this.color = PotionContentsComponent.getColor(colorEffects);
    }

    public PotionMixture mix(PotionMixture other) {

        if (this.equals(other)) return this;

        final var effects = new ArrayList<>(this.effects);
        effects.addAll(other.effects);
        effects.addAll(basePotion.getEffects());
        effects.addAll(other.basePotion.getEffects());

        return new PotionMixture(Potions.WATER.value(), effects, false, null);
    }

    public static PotionMixture fromStack(ItemStack stack) {
        final var component = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        return new PotionMixture(component.potion().orElse(Potions.WATER).value(), component.customEffects(), true, stack.get(EXTRA_DATA));
    }

    public ItemStack toStack() {
        final var stack = new ItemStack(Items.POTION);
        var component = PotionContentsComponent.DEFAULT;

        if (pure) {
            if (basePotion != Potions.WATER.value()) component = component.with(Registries.POTION.getEntry(basePotion));
            for (var effect : effects) {
                component = component.with(effect);
            }
        } else {
            component = component.with(Registries.POTION.getEntry(DUBIOUS_POTION));
        }

        stack.set(DataComponentTypes.POTION_CONTENTS, component);
        stack.applyComponentsFrom(this.extraComponents);

        return stack;
    }

    public boolean isEmpty() {
        return this == EMPTY || (basePotion == Potions.WATER && effects.isEmpty());
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

    public ComponentMap extraComponents() {
        return extraComponents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotionMixture that = (PotionMixture) o;

        return pure == that.pure && color == that.color
                && Objects.equals(basePotion, that.basePotion)
                && Objects.equals(effects, that.effects)
                && Objects.equals(extraComponents, that.extraComponents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePotion, effects, pure, color, extraComponents);
    }
}
