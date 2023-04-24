package io.wispforest.affinity.recipe.ingredient;

import com.google.gson.JsonObject;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityIngredients;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnchantedBookIngredient implements CustomIngredient {

    private final Map<Enchantment, Integer> requiredEnchantments;

    public EnchantedBookIngredient(Map<Enchantment, Integer> requiredEnchantments) {
        this.requiredEnchantments = requiredEnchantments;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!stack.isOf(Items.ENCHANTED_BOOK)) return false;

        var presentEnchantments = EnchantmentHelper.get(stack);
        for (var entry : this.requiredEnchantments.entrySet()) {
            if (!Objects.equals(presentEnchantments.get(entry.getKey()), entry.getValue())) {
                return false;
            }
        }

        return presentEnchantments.size() == this.requiredEnchantments.size();
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        var stack = Items.ENCHANTED_BOOK.getDefaultStack();
        this.requiredEnchantments.forEach((enchantment, integer) -> {
            EnchantedBookItem.addEnchantment(stack, new EnchantmentLevelEntry(enchantment, integer));
        });

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
        public EnchantedBookIngredient read(JsonObject json) {
            var requiredEnchantments = new HashMap<Enchantment, Integer>();

            for (var enchantmentElement : JsonHelper.getArray(json, "enchantments")) {
                if (!enchantmentElement.isJsonObject()) continue;

                var enchantmentObject = enchantmentElement.getAsJsonObject();
                requiredEnchantments.put(
                        Registries.ENCHANTMENT.getOrEmpty(new Identifier(JsonHelper.getString(enchantmentObject, "id"))).orElseThrow(),
                        JsonHelper.getInt(enchantmentObject, "level", 1)
                );
            }

            return new EnchantedBookIngredient(requiredEnchantments);
        }

        @Override
        public EnchantedBookIngredient read(PacketByteBuf buf) {
            return new EnchantedBookIngredient(buf.readMap(HashMap::new, $ -> $.readRegistryValue(Registries.ENCHANTMENT), PacketByteBuf::readVarInt));
        }

        @Override
        public void write(PacketByteBuf buf, EnchantedBookIngredient ingredient) {
            buf.writeMap(ingredient.requiredEnchantments, (buffer, enchantment) -> buffer.writeRegistryValue(Registries.ENCHANTMENT, enchantment), PacketByteBuf::writeVarInt);
        }

        @Override
        public void write(JsonObject json, EnchantedBookIngredient ingredient) {}
    }
}
