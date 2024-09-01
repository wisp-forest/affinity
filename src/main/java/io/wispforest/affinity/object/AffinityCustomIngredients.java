package io.wispforest.affinity.object;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.util.EndecUtil;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

import java.util.List;

public class AffinityCustomIngredients {

    public static void initialize() {
        CustomIngredientSerializer.register(PassthroughIngredient.SERIALIZER);
    }

    private record PassthroughIngredient(Ingredient base, List<ItemStack> displayStacks) implements CustomIngredient {

        private static final StructEndec<PassthroughIngredient> ENDEC = StructEndecBuilder.of(
            EndecUtil.INGREDIENT_ENDEC.fieldOf("base", PassthroughIngredient::base),
            MinecraftEndecs.ITEM_STACK.listOf().fieldOf("display_stacks", PassthroughIngredient::getMatchingStacks),
            PassthroughIngredient::new
        );

        private static final CustomIngredientSerializer<PassthroughIngredient> SERIALIZER = new CustomIngredientSerializer<PassthroughIngredient>() {
            @Override
            public Identifier getIdentifier() {
                return Affinity.id("passthrough");
            }

            @Override
            public MapCodec<PassthroughIngredient> getCodec(boolean allowEmpty) {
                return CodecUtils.toMapCodec(ENDEC);
            }

            @Override
            public PacketCodec<RegistryByteBuf, PassthroughIngredient> getPacketCodec() {
                return CodecUtils.toPacketCodec(ENDEC);
            }
        };

        @Override
        public boolean test(ItemStack stack) {
            return this.base.test(stack);
        }

        @Override
        public List<ItemStack> getMatchingStacks() {
            return this.displayStacks;
        }

        @Override
        public boolean requiresTesting() {
            return true;
        }

        @Override
        public CustomIngredientSerializer<?> getSerializer() {
            return SERIALIZER;
        }
    }

}
