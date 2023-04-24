package io.wispforest.affinity.recipe.ingredient;

import com.google.gson.JsonObject;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityIngredients;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.List;

public class PotionIngredient implements CustomIngredient {

    private final Potion requiredPotion;

    public PotionIngredient(Potion requiredPotion) {
        this.requiredPotion = requiredPotion;
    }

    @Override
    public boolean test(ItemStack stack) {
        return (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) && PotionUtil.getPotion(stack) == requiredPotion;
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        return List.of(
                PotionUtil.setPotion(Items.POTION.getDefaultStack(), this.requiredPotion),
                PotionUtil.setPotion(Items.SPLASH_POTION.getDefaultStack(), this.requiredPotion),
                PotionUtil.setPotion(Items.LINGERING_POTION.getDefaultStack(), this.requiredPotion)
        );
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return AffinityIngredients.POTION;
    }

    public static final class Serializer implements CustomIngredientSerializer<PotionIngredient> {

        @Override
        public Identifier getIdentifier() {
            return Affinity.id("potion");
        }

        @Override
        public PotionIngredient read(JsonObject json) {
            return new PotionIngredient(Registries.POTION.getOrEmpty(new Identifier(JsonHelper.getString(json, "potion"))).orElseThrow());
        }

        @Override
        public PotionIngredient read(PacketByteBuf buf) {
            return new PotionIngredient(buf.readRegistryValue(Registries.POTION));
        }

        @Override
        public void write(PacketByteBuf buf, PotionIngredient ingredient) {
            buf.writeRegistryValue(Registries.POTION, ingredient.requiredPotion);
        }

        @Override
        public void write(JsonObject json, PotionIngredient ingredient) {}
    }
}
