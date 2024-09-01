package io.wispforest.affinity.component;

import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;

public class BanishmentComponent implements Component {

    public static final KeyedEndec<Identifier> DIMENSION = MinecraftEndecs.IDENTIFIER.keyed("Dimension", (Identifier) null);
    public static final KeyedEndec<BlockPos> POSITION = MinecraftEndecs.BLOCK_POS.keyed("Pos", (BlockPos) null);

    public @Nullable Identifier dimension = null;
    public @Nullable BlockPos pos = null;

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        this.dimension = tag.get(DIMENSION);
        this.pos = tag.get(POSITION);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.putIfNotNull(SerializationContext.empty(), DIMENSION, this.dimension);
        tag.putIfNotNull(SerializationContext.empty(), POSITION, this.pos);
    }
}
