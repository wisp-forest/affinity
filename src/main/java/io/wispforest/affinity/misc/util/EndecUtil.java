package io.wispforest.affinity.misc.util;

import io.wispforest.affinity.endec.BuiltInEndecs;
import io.wispforest.affinity.endec.nbt.NbtEndec;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec2f;

import java.util.List;

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

    public static final Endec<Vec2f> VEC2F_ENDEC = Endec.FLOAT.listOf().validate(ints -> {
        if (ints.size() != 2) {
            throw new IllegalStateException("Vec2f array must have two elements");
        }
    }).xmap(
        components -> new Vec2f(components.get(0), components.get(1)),
        vector -> List.of(vector.x, vector.y)
    );
}
