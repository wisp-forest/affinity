package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import io.wispforest.affinity.item.EchoShardExtension;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BanishmentComponent implements Component {

    public static final NbtKey<Identifier> DIMENSION = new NbtKey<>("Dimension", NbtKey.Type.IDENTIFIER);
    public static final NbtKey<BlockPos> POSITION = new NbtKey<>("Pos", EchoShardExtension.BLOCK_POS_TYPE);

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
