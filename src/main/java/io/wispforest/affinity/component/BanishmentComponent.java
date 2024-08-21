package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BanishmentComponent implements Component {

    public static final KeyedEndec<Identifier> DIMENSION = BuiltInEndecs.IDENTIFIER.keyed("Dimension", (Identifier) null);
    public static final KeyedEndec<BlockPos> POSITION = BuiltInEndecs.BLOCK_POS.keyed("Pos", (BlockPos) null);

    public @Nullable Identifier dimension = null;
    public @Nullable BlockPos pos = null;

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.dimension = tag.get(DIMENSION);
        this.pos = tag.get(POSITION);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        tag.putIfNotNull(DIMENSION, this.dimension);
        tag.putIfNotNull(POSITION, this.pos);
    }
}
