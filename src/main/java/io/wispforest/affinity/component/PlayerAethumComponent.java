package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.DamageTypeKey;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerAethumComponent extends AethumComponent<PlayerEntity> implements CommonTickingComponent {

    public static final DamageTypeKey AETHUM_DRAIN_DAMAGE = new DamageTypeKey(Affinity.id("aethum_drain"), DamageTypeKey.Attribution.NEVER_ATTRIBUTE);

    private double maxAethum = 15;
    private double naturalRegenSpeed = 0.025;

    public PlayerAethumComponent(PlayerEntity holder) {
        super(AffinityComponents.PLAYER_AETHUM, holder);
    }

    @Override
    public void tick() {
        if (this.aethum < 3 && this.holder.getWorld().getTime() % 20 == 0) {
            this.holder.damage(AETHUM_DRAIN_DAMAGE.source(this.holder.getWorld()), (float) (5d - this.aethum));
        }

        if (!this.holder.getWorld().isClient && this.maxAethum != this.maxAethum() || this.naturalRegenSpeed != this.naturalRegenSpeed()) {
            this.maxAethum = this.maxAethum();
            this.naturalRegenSpeed = this.naturalRegenSpeed();

            this.key.sync(this.holder);
        }

        if (this.aethum >= this.maxAethum) {
            this.aethum = this.maxAethum;
            return;
        }

        this.aethum = Math.min(this.aethum + this.naturalRegenSpeed, this.maxAethum);
    }

    @Override
    public double maxAethum() {
        return this.holder.getWorld().isClient
                ? this.maxAethum
                : this.holder.getAttributeValue(AffinityEntityAttributes.MAX_AETHUM);
    }

    public double naturalRegenSpeed() {
        return this.holder.getWorld().isClient
                ? this.naturalRegenSpeed
                : this.holder.getAttributeValue(AffinityEntityAttributes.NATURAL_AETHUM_REGEN_SPEED);
    }

    public boolean hasAethum(double amount) {
        return this.holder.isCreative() || this.aethum >= aethum;
    }

    @Override
    public boolean tryConsumeAethum(double amount) {
        var result = super.tryConsumeAethum(amount);
        return result || this.holder.isCreative();
    }

    @Override
    protected double initialValue() {
        return 10;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.holder;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        super.writeSyncPacket(buf, recipient);
        buf.writeDouble(this.maxAethum);
        buf.writeDouble(this.naturalRegenSpeed);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        super.applySyncPacket(buf);
        this.maxAethum = buf.readDouble();
        this.naturalRegenSpeed = buf.readDouble();
    }
}
