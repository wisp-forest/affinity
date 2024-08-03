package io.wispforest.affinity.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityIngredients;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.Objects;

public class EnchantedBookIngredient implements CustomIngredient {

    public static final Codec<EnchantmentLevelEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Enchantment.ENTRY_CODEC.fieldOf("id").forGetter(entry -> entry.enchantment),
                    Codec.INT.optionalFieldOf("level", 1).forGetter(entry -> entry.level)
            ).apply(instance, EnchantmentLevelEntry::new));

    public static final MapCodec<EnchantedBookIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(ENTRY_CODEC.listOf().fieldOf("enchantments").forGetter(o -> o.requiredEnchantments))
            .apply(instance, EnchantedBookIngredient::new));

    private final List<EnchantmentLevelEntry> requiredEnchantments;

    public EnchantedBookIngredient(List<EnchantmentLevelEntry> requiredEnchantments) {
        this.requiredEnchantments = requiredEnchantments;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!stack.isOf(Items.ENCHANTED_BOOK)) return false;

        var presentEnchantments = EnchantmentHelper.getEnchantments(stack);
        for (var entry : this.requiredEnchantments) {
            if (!Objects.equals(presentEnchantments.getLevel(entry.enchantment), entry.level)) {
                return false;
            }
        }

        return presentEnchantments.getSize() == this.requiredEnchantments.size();
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        var stack = Items.ENCHANTED_BOOK.getDefaultStack();
        for (var entry : this.requiredEnchantments) {
            stack.addEnchantment(entry.enchantment, entry.level);
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
        public MapCodec<EnchantedBookIngredient> getCodec(boolean allowEmpty) {
            return EnchantedBookIngredient.CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, EnchantedBookIngredient> getPacketCodec() {
            // TODO: move this to endecs.
            return PacketCodecs.unlimitedRegistryCodec(EnchantedBookIngredient.CODEC.codec());
        }
    }
}
