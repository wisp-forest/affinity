package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import io.wispforest.affinity.misc.quack.AffinityWorldExtension;
import io.wispforest.affinity.mixin.access.LivingEntityAccessor;
import io.wispforest.affinity.object.AffinityStatusEffects;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;

public class InnerCreeperComponent implements Component, CommonTickingComponent, AutoSyncedComponent {

    private static final KeyedEndec<Boolean> IGNITED = Endec.BOOLEAN.keyed("Ignited", false);
    private static final KeyedEndec<Boolean> ACTIVE = Endec.BOOLEAN.keyed("Active", false);
    private static final KeyedEndec<Integer> FUSE_DIRECTION = Endec.INT.keyed("FuseDirection", -1);
    private static final KeyedEndec<Integer> MAX_FUSE_TIME = Endec.INT.keyed("MaxFuseTime", 30);
    private static final KeyedEndec<Integer> EXPLOSION_RADIUS = Endec.INT.keyed("ExplosionRadius", 3);

    private final LivingEntity holder;

    private boolean ignited = false;
    private boolean active = false;
    private int fuseDirection = -1;
    private int maxFuseTime = 30;
    private int explosionRadius = 3;

    private int lastFuseTime = 0;
    private int fuseTime = 0;

    public InnerCreeperComponent(LivingEntity holder) {
        this.holder = holder;
    }

    public boolean active() {
        return this.active;
    }

    public float fuseProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastFuseTime, this.fuseTime) / (this.maxFuseTime - 2f);
    }

    public int fuseDirection() {
        return this.fuseDirection;
    }

    public void fuseDirection(int fuseDirection) {
        this.fuseDirection = fuseDirection;
        AffinityComponents.INNER_CREEPER.sync(this.holder);
    }

    @Override
    public void tick() {
        if (!this.holder.isAlive()) return;

        this.lastFuseTime = this.fuseTime;
        if (!this.holder.getWorld().isClient && this.active != this.holder.hasStatusEffect(AffinityStatusEffects.CAT_ANXIETY)) {
            this.active = this.holder.hasStatusEffect(AffinityStatusEffects.CAT_ANXIETY);
            AffinityComponents.INNER_CREEPER.sync(this.holder);
        }

        if (this.active
                && this.holder instanceof ServerPlayerEntity player
                && !player.getWorld().getOtherEntities(this.holder, player.getBoundingBox().expand(3, 1, 3), entity -> !entity.isSpectator()).isEmpty()) {
            this.fuseDirection(1);
        }

        if (this.ignited) {
            this.fuseDirection = 1;
        } else if (!this.active) {
            this.fuseDirection = -1;
        }

        if (this.fuseDirection > 0 && this.fuseTime == 0) {
            this.holder.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1f, .5f);
            this.holder.emitGameEvent(GameEvent.PRIME_FUSE);
        }

        this.fuseTime += this.fuseDirection;
        if (this.fuseTime < 0) {
            this.fuseTime = 0;
        }

        if (this.fuseTime >= this.maxFuseTime) {
            this.fuseTime = this.maxFuseTime;
            this.explode();
        }
    }

    private void explode() {
        if (this.holder.getWorld().isClient) return;

        ((LivingEntityAccessor) this.holder).affinity$setDead(true);

        ((AffinityWorldExtension) this.holder.getWorld()).affinity$markNextExplosionNoDrops();
        this.holder.getWorld().createExplosion(this.holder, this.holder.getX(), this.holder.getY(), this.holder.getZ(), this.explosionRadius, World.ExplosionSourceType.MOB);

        if (this.holder instanceof PlayerEntity player) {
            player.kill();
        } else {
            this.holder.discard();
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.ignited = tag.get(IGNITED);
        this.active = tag.get(ACTIVE);
        this.fuseDirection = tag.get(FUSE_DIRECTION);
        this.explosionRadius = tag.get(EXPLOSION_RADIUS);

        if (!this.holder.getWorld().isClient) {
            this.maxFuseTime = tag.get(MAX_FUSE_TIME);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        tag.put(IGNITED, this.ignited);
        tag.put(ACTIVE, this.active);
        tag.put(FUSE_DIRECTION, this.fuseDirection);
        tag.put(MAX_FUSE_TIME, this.maxFuseTime);
        tag.put(EXPLOSION_RADIUS, this.explosionRadius);
    }

    static {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(entity instanceof LivingEntity living) || !living.getComponent(AffinityComponents.INNER_CREEPER).active()) {
                return ActionResult.PASS;
            }

            var playerStack = player.getStackInHand(hand);
            if (!playerStack.isIn(ItemTags.CREEPER_IGNITERS)) return ActionResult.PASS;

            if (!world.isClient) {
                living.getComponent(AffinityComponents.INNER_CREEPER).ignited = true;
                AffinityComponents.INNER_CREEPER.sync(living);

                if (!playerStack.isDamageable()) {
                    playerStack.decrement(1);
                } else {
                    playerStack.damage(1, player, $ -> $.sendToolBreakStatus(hand));
                }
            }

            return ActionResult.success(world.isClient);
        });
    }
}
