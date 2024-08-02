package io.wispforest.affinity.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityIngredients;
import io.wispforest.endec.Endec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.Objects;

public class EnchantedBookIngredient implements CustomIngredient {

    public static final Codec<EnchantmentLevelEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Registries.ENCHANTMENT.getCodec().fieldOf("id").forGetter(entry -> entry.enchantment),
                    Codecs.createStrictOptionalFieldCodec(Codec.INT, "level", 1).forGetter(entry -> entry.level)
            ).apply(instance, EnchantmentLevelEntry::new));

    public static final Endec<EnchantedBookIngredient> ENDEC = RecordCodecBuilder.create(instance -> instance
            .group(ENTRY_CODEC.listOf().fieldOf("enchantments").forGetter(o -> o.requiredEnchantments))
            .apply(instance, EnchantedBookIngredient::new));

    private final List<EnchantmentLevelEntry> requiredEnchantments;

    public EnchantedBookIngredient(List<EnchantmentLevelEntry> requiredEnchantments) {
        this.requiredEnchantments = requiredEnchantments;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!stack.isOf(Items.ENCHANTED_BOOK)) return false;

        var presentEnchantments = EnchantmentHelper.get(stack);
        for (var entry : this.requiredEnchantments) {
            if (!Objects.equals(presentEnchantments.get(entry.enchantment), entry.level)) {
                return false;
            }
        }

        return presentEnchantments.size() == this.requiredEnchantments.size();
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        var stack = Items.ENCHANTED_BOOK.getDefaultStack();
        for (var entry : this.requiredEnchantments) {
            EnchantedBookItem.addEnchantment(stack, entry);
        }

        return List.of(stack);
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return AffinityIngredients.ENCHANTMENTS;
    }

    public static class Serializer implements CustomIngredientSerializer<EnchantedBookIngredient> {

        @Override
        public Identifier getIdentifier() {
            return Affinity.id("enchantments");
        }

        @Override
        public Codec<EnchantedBookIngredient> getCodec(boolean allowEmpty) {
            return EnchantedBookIngredient.CODEC;
        }

        @Override
        public EnchantedBookIngredient read(PacketByteBuf buf) {
            return new EnchantedBookIngredient(buf.readList($ -> new EnchantmentLevelEntry(buf.readRegistryValue(Registries.ENCHANTMENT), buf.readShort())));
        }

        @Override
        public void write(PacketByteBuf buf, EnchantedBookIngredient ingredient) {
            buf.writeCollection(ingredient.requiredEnchantments, (packetByteBuf, entry) -> {
                buf.writeRegistryValue(Registries.ENCHANTMENT, entry.enchantment);
                buf.writeShort(entry.level);
            });
        }
    }
}
