package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import io.wispforest.affinity.endec.BuiltInEndecs;
import io.wispforest.endec.impl.KeyedEndec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BanishmentComponent implements Component {

    public static final KeyedEndec<Identifier> DIMENSION = BuiltInEndecs.IDENTIFIER.keyed("Dimension", World.OVERWORLD.getValue());
    public static final KeyedEndec<BlockPos> POSITION = BuiltInEndecs.BLOCK_POS.keyed("Pos", BlockPos.ORIGIN);

    public Identifier dimension = World.OVERWORLD.getValue();
    public BlockPos pos = BlockPos.ORIGIN;

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.dimension = tag.get(DIMENSION);
        this.pos = tag.get(POSITION);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        tag.put(DIMENSION, this.dimension);
        tag.put(POSITION, this.pos);
    }
}
