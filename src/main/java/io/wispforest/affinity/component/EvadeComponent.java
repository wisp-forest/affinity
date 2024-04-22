package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvadeComponent implements Component, CommonTickingComponent {

    private final PlayerEntity holder;

    private @Nullable Vec3d velocity;
    private int evadeTicks;

    public EvadeComponent(PlayerEntity holder) {
        this.holder = holder;
    }

    @Override
    public void tick() {
        if (this.evadeTicks < 1) return;

        if (--this.evadeTicks == 0) {
            this.velocity = null;

            this.holder.fallDistance = 0;
            this.holder.setVelocity(Vec3d.ZERO);
        }
    }

    public void evade(Vec3d direction) {
        this.velocity = direction.normalize();
        this.evadeTicks = 3;

        this.holder.fallDistance = 0;
        this.holder.setVelocity(Vec3d.ZERO);
    }

    public boolean isActive() {
        return this.velocity != null;
    }

    public Vec3d velocity() {
        return this.velocity;
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {}

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {}
}
