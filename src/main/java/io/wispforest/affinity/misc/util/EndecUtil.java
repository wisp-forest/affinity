package io.wispforest.affinity.misc.util;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.GlobalPos;

public class EndecUtil {

    public static final Endec<Ingredient> INGREDIENT_ENDEC = CodecUtils.toEndec(Ingredient.DISALLOW_EMPTY_CODEC);
    public static final Endec<ItemStack> RECIPE_RESULT_ENDEC = CodecUtils.toEndec(ItemStack.VALIDATED_CODEC);

    public static final Endec<GlobalPos> GLOBAL_POS_ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.IDENTIFIER.xmap(id -> RegistryKey.of(RegistryKeys.WORLD, id), RegistryKey::getValue).fieldOf("dimension", GlobalPos::dimension),
            MinecraftEndecs.BLOCK_POS.fieldOf("pos", GlobalPos::pos),
            GlobalPos::create
    );
}
