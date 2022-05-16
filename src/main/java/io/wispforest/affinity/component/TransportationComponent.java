package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import io.wispforest.affinity.item.EchoShardItem;
import io.wispforest.owo.util.NbtKey;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TransportationComponent implements Component {

    public static final NbtKey<Identifier> DIMENSION = new NbtKey<>("Dimension", EchoShardItem.IDENTIFIER_TYPE);
    public static final NbtKey<Vec3d> POSITION = new NbtKey<>("Pos", EchoShardItem.VEC_3D_TYPE);

    public Identifier dimension = World.OVERWORLD.getValue();
    public Vec3d pos = Vec3d.ZERO;

    @Override
    public void readFromNbt(NbtCompound tag) {
        dimension = DIMENSION.get(tag);
        pos = POSITION.get(tag);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        DIMENSION.put(tag, this.dimension);
        POSITION.put(tag, this.pos);
    }
}
