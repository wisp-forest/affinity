package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TransportationComponent implements Component {
    private Identifier world = World.OVERWORLD.getValue();
    private Vec3d pos = Vec3d.ZERO;

    public TransportationComponent() {

    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        world = new Identifier(tag.getString("World"));
        pos = new Vec3d(
            tag.getDouble("X"),
            tag.getDouble("Y"),
            tag.getDouble("Z")
        );
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putString("World", world.toString());
        tag.putDouble("X", pos.getX());
        tag.putDouble("Y", pos.getY());
        tag.putDouble("Z", pos.getZ());
    }

    public Identifier getWorld() {
        return world;
    }

    public void setWorld(Identifier world) {
        this.world = world;
    }

    public Vec3d getPos() {
        return pos;
    }

    public void setPos(Vec3d pos) {
        this.pos = pos;
    }
}
