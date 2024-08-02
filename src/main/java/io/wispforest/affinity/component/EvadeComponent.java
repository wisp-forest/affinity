package io.wispforest.affinity.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.TransientComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public class EvadeComponent implements TransientComponent, CommonTickingComponent {

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
}
