package io.wispforest.affinity.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityIngredients;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.List;

public class PotionIngredient implements CustomIngredient {

    public static final StructEndec<PotionIngredient> ENDEC = StructEndecBuilder.of(
            CodecUtils.toEndec(Registries.POTION.getEntryCodec()).fieldOf("potion", ingredient -> ingredient.requiredPotion),
            PotionIngredient::new
    );

    private final RegistryEntry<Potion> requiredPotion;

    public PotionIngredient(RegistryEntry<Potion> requiredPotion) {
        this.requiredPotion = requiredPotion;
    }

    @Override
    public boolean test(ItemStack stack) {
        return (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION))
                && stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).matches(requiredPotion);
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        return List.of(
                PotionContentsComponent.createStack(Items.POTION, this.requiredPotion),
                PotionContentsComponent.createStack(Items.SPLASH_POTION, this.requiredPotion),
                PotionContentsComponent.createStack(Items.LINGERING_POTION, this.requiredPotion)
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
        public MapCodec<PotionIngredient> getCodec(boolean allowEmpty) {
            return CodecUtils.toMapCodec(ENDEC);
        }

        @Override
        public PacketCodec<RegistryByteBuf, PotionIngredient> getPacketCodec() {
            return CodecUtils.toPacketCodec(ENDEC);
        }
    }
}
