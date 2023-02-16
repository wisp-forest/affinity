package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerAethumComponent extends AethumComponent<PlayerEntity> implements CommonTickingComponent {

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

        if (!this.holder.world.isClient && this.maxAethum != this.maxAethum() || this.naturalRegenSpeed != this.naturalRegenSpeed()) {
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

    public double maxAethum() {
        return this.holder.world.isClient
                ? this.maxAethum
                : this.holder.getAttributeValue(AffinityEntityAttributes.MAX_AETHUM);
    }

    public double naturalRegenSpeed() {
        return this.holder.world.isClient
                ? this.naturalRegenSpeed
                : this.holder.getAttributeValue(AffinityEntityAttributes.NATURAL_AETHUM_REGEN_SPEED);
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
