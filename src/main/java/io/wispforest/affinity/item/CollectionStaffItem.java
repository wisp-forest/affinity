package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.EntityReferenceTracker;
import io.wispforest.affinity.misc.ServerExecutionScheduler;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.network.annotations.ElementType;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionStaffItem extends Item {

    public CollectionStaffItem() {
        super(AffinityItems.settings(0).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        final var playerStack = user.getStackInHand(hand);
        final var triggerPos = user.getBlockPos();

        user.getItemCooldownManager().set(playerStack.getItem(), 30);
        if (!(user.world instanceof ServerWorld serverWorld)) return TypedActionResult.success(playerStack);

        WorldOps.playSound(serverWorld, triggerPos, SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 1, 0);

        var ref = EntityReferenceTracker.tracked(getItems(user));
        ServerExecutionScheduler.runFor(serverWorld, 25, () -> {
            if (!ref.present()) return false;

            AffinityNetwork.CHANNEL.serverHandle(serverWorld, triggerPos)
                    .send(new BulkParticlesPacket(ref.get(), ParticleTypes.WITCH, .25));

            return true;
        }, () -> {
            if (!ref.present()) return;

            WorldOps.playSound(world, user.getPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1.5f);
            AffinityNetwork.CHANNEL.serverHandle(serverWorld, triggerPos)
                    .send(new BulkParticlesPacket(ref.get(), ParticleTypes.POOF, .25));

            for (var item : ref.get()) {
                item.updatePosition(user.getX(), user.getY(), user.getZ());
            }
        });

        return TypedActionResult.success(playerStack);
    }

    private static Collection<ItemEntity> getItems(LivingEntity entity) {
        var box = new Box(entity.getBlockPos()).expand(5, 3, 5);
        return entity.world.getEntitiesByClass(ItemEntity.class, box, itemEntity -> !itemEntity.cannotPickup());
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 25;
    }

    static {
        AffinityNetwork.CHANNEL.registerClientbound(BulkParticlesPacket.class, (message, access) -> {
            for (var pos : message.positions()) {
                ClientParticles.spawn(message.particle(), access.runtime().world,
                        pos.add(0, .125, 0), message.deviation());
            }
        });
    }

    public record BulkParticlesPacket(ParticleEffect particle, double deviation, @ElementType(Vec3d.class) List<Vec3d> positions) {

        public <E extends Entity> BulkParticlesPacket(Collection<E> entities, ParticleEffect particle, double deviation) {
            this(particle, deviation, new ArrayList<>());
            for (var entity : entities) this.positions.add(entity.getPos());
        }

    }
}
