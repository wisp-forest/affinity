package io.wispforest.affinity.misc.components;

import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import io.wispforest.affinity.util.NbtKey;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PlayerAethumComponent extends AethumComponent<PlayerEntity> implements CommonTickingComponent {

    public static final NbtKey<Double> MAX_AETHUM_KEY = new NbtKey<>("MaxAethum", NbtKey.Type.DOUBLE);
    public static final NbtKey<Double> NATURAL_REGEN_SPEED_KEY = new NbtKey<>("NaturalRegenSpeed", NbtKey.Type.DOUBLE);

    private double maxAethum = 15;
    private double naturalRegenSpeed = 0.025;

    public PlayerAethumComponent(PlayerEntity holder) {
        super(AffinityComponents.PLAYER_AETHUM, holder);
    }

    @Override
    public void tick() {
        if (this.aethum >= this.maxAethum) return;
        this.aethum = Math.min(this.aethum + this.naturalRegenSpeed, this.maxAethum);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        super.readFromNbt(tag);
        this.maxAethum = MAX_AETHUM_KEY.readOr(tag, this.maxAethum);
        this.naturalRegenSpeed = NATURAL_REGEN_SPEED_KEY.readOr(tag, this.naturalRegenSpeed);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        super.writeToNbt(tag);
        MAX_AETHUM_KEY.write(tag, this.maxAethum);
        NATURAL_REGEN_SPEED_KEY.write(tag, this.naturalRegenSpeed);
    }

    public void setMaxAethum(double maxAethum) {
        this.maxAethum = maxAethum;
    }

    public double getMaxAethum() {
        return maxAethum;
    }

    public void setNaturalRegenSpeed(double naturalRegenSpeed) {
        this.naturalRegenSpeed = naturalRegenSpeed;
    }

    public double getNaturalRegenSpeed() {
        return naturalRegenSpeed;
    }

    @Override
    double initialValue() {
        return 10;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.holder;
    }
}
