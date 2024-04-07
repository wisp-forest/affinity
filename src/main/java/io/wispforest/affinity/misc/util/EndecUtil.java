package io.wispforest.affinity.misc.util;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import io.wispforest.owo.serialization.format.nbt.NbtEndec;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.GlobalPos;

public class EndecUtil {

    public static final Endec<ItemStack> RECIPE_RESULT_ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.ofRegistry(Registries.ITEM).fieldOf("item", ItemStack::getItem),
            Endec.INT.optionalFieldOf("count", ItemStack::getCount, 1),
            NbtEndec.COMPOUND.optionalFieldOf("data", ItemStack::getNbt, (NbtCompound) null),
            (item, count, nbt) -> {
                var stack = new ItemStack(item, count);
                stack.setNbt(nbt);
                return stack;
            }
    );

    public static final Endec<GlobalPos> GLOBAL_POS_ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.IDENTIFIER.xmap(id -> RegistryKey.of(RegistryKeys.WORLD, id), RegistryKey::getValue).fieldOf("dimension", GlobalPos::getDimension),
            BuiltInEndecs.BLOCK_POS.fieldOf("pos", GlobalPos::getPos),
            GlobalPos::create
    );
}
