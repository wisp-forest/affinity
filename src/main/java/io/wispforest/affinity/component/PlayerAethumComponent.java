package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PlayerAethumComponent extends AethumComponent<PlayerEntity> implements CommonTickingComponent, CopyableComponent<PlayerAethumComponent> {

    public static final NbtKey<Double> MAX_AETHUM_KEY = new NbtKey<>("MaxAethum", NbtKey.Type.DOUBLE);
    public static final NbtKey<Double> NATURAL_REGEN_SPEED_KEY = new NbtKey<>("NaturalRegenSpeed", NbtKey.Type.DOUBLE);

    public static final DamageSource AETHUM_DRAIN_DAMAGE = new DamageSource("aethum_drain").setUsesMagic().setBypassesArmor();

    private double maxAethum = 15;
    private double naturalRegenSpeed = 0.025;

    public PlayerAethumComponent(PlayerEntity holder) {
        super(AffinityComponents.PLAYER_AETHUM, holder);
    }

    @Override
    public void tick() {
        if (this.aethum < 3 && this.holder.world.getTime() % 20 == 0) {
            this.holder.damage(AETHUM_DRAIN_DAMAGE, (float) (5d - this.aethum));
        }

        if (this.aethum >= this.maxAethum) return;
        this.aethum = Math.min(this.aethum + this.naturalRegenSpeed, this.maxAethum);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        super.readFromNbt(tag);
        this.maxAethum = tag.getOr(MAX_AETHUM_KEY, this.maxAethum);
        this.naturalRegenSpeed = tag.getOr(NATURAL_REGEN_SPEED_KEY, this.naturalRegenSpeed);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        super.writeToNbt(tag);
        tag.put(MAX_AETHUM_KEY, this.maxAethum);
        tag.put(NATURAL_REGEN_SPEED_KEY, this.naturalRegenSpeed);
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

    @Override
    public void copyFrom(PlayerAethumComponent other) {
        this.maxAethum = other.maxAethum;
        this.naturalRegenSpeed = other.naturalRegenSpeed;
    }
}
