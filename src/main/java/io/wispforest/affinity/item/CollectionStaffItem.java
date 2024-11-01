package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.misc.EntityReference;
import io.wispforest.affinity.misc.ServerTasks;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionStaffItem extends StaffItem {

    private static final InquirableOutlineProvider.Outline UP_AOE = new InquirableOutlineProvider.Outline(-5, 0, -5, 5, 4, 5);
    private static final InquirableOutlineProvider.Outline DOWN_AOE = new InquirableOutlineProvider.Outline(-5, -4, -5, 5, 0, 5);

    public CollectionStaffItem() {
        super(AffinityItems.settings().maxCount(1).trackUsageStat());
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean canBePlacedOnPedestal() {
        return true;
    }

    @Override
    public void pedestalTickServer(ServerWorld world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        if (pedestal.time() % 20 != 0) return;

        var storage = ItemStorage.SIDED.find(world, pos.add(0, pedestal.down(), 0), pedestal.facing().getOpposite());
        if (storage == null) return;

        final var items = world.getEntitiesByClass(ItemEntity.class, new Box(pos.add(0, pedestal.up() * 2, 0)).expand(5, 2, 5), Entity::isAlive);
        final var iter = items.iterator();

        while (iter.hasNext()) {
            var item = iter.next();
            var stack = item.getStack();

            try (var transaction = Transaction.openOuter()) {
                long transferred = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                if (transferred < 1 || !pedestal.hasFlux(transferred * 8L)) {
                    iter.remove();
                    continue;
                }

                if (transferred == stack.getCount()) {
                    item.discard();
                } else {
                    item.setStack(stack.copyWithCount(stack.getCount() - (int) transferred));
                }

                pedestal.consumeFlux(transferred * 8);
                transaction.commit();
            }
        }

        if (!items.isEmpty()) {
            AffinityNetwork.CHANNEL.serverHandle(world, pos).send(new BulkParticlesPacket(items, ParticleTypes.POOF, .25));
        }
    }

    @Override
    public void pedestalTickClient(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        if (pedestal.flux() < 8) return;
        if (ItemStorage.SIDED.find(world, pos.add(0, pedestal.down(), 0), pedestal.facing().getOpposite()) == null) {
            return;
        }

        for (var item : world.getEntitiesByClass(ItemEntity.class, new Box(pos.add(0, pedestal.up() * 2, 0)).expand(5, 2, 5), Entity::isAlive)) {
            ClientParticles.spawn(ParticleTypes.WITCH, world, item.getPos().add(0, .125, 0), .25);
        }
    }

    @Override
    public InquirableOutlineProvider.Outline getAreaOfEffect(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        return pedestal.facing() == Direction.UP ? UP_AOE : DOWN_AOE;
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock) {
        final var triggerPos = player.getBlockPos();

        player.getItemCooldownManager().set(stack.getItem(), 30);
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return TypedActionResult.success(stack);

        WorldOps.playSound(serverWorld, triggerPos, AffinitySoundEvents.ITEM_COLLECTION_STAFF_MARK_ITEMS, SoundCategory.PLAYERS, 1, 1);

        var ref = EntityReference.of(getItems(player));
        ServerTasks.doFor(serverWorld, 25, () -> {
            if (!ref.present()) return false;

            AffinityNetwork.CHANNEL.serverHandle(serverWorld, triggerPos)
                    .send(new BulkParticlesPacket(ref.get(), ParticleTypes.WITCH, .25));

            return true;
        }, () -> {
            ref.consume(itemEntities -> {
                WorldOps.playSound(world, player.getPos(), AffinitySoundEvents.ITEM_COLLECTION_STAFF_TELEPORT_ITEMS, SoundCategory.PLAYERS, 1, 1f);
                AffinityNetwork.CHANNEL.serverHandle(serverWorld, triggerPos)
                        .send(new BulkParticlesPacket(itemEntities, ParticleTypes.POOF, .25));

                for (var item : itemEntities) {
                    item.updatePosition(player.getX(), player.getY(), player.getZ());
                }
            });
        });

        return TypedActionResult.success(stack);
    }

    private static Collection<ItemEntity> getItems(LivingEntity entity) {
        var box = new Box(BlockPos.ofFloored(MathUtil.entityCenterPos(entity))).expand(9, 4, 9);
        return entity.getWorld().getEntitiesByClass(ItemEntity.class, box, itemEntity -> !itemEntity.cannotPickup());
    }

    static {
        AffinityNetwork.CHANNEL.registerClientbound(BulkParticlesPacket.class, (message, access) -> {
            for (var pos : message.positions()) {
                ClientParticles.spawn(message.particle(), access.player().getWorld(),
                        pos.add(0, .125, 0), message.deviation());
            }
        });
    }

    public record BulkParticlesPacket(ParticleEffect particle, double deviation, List<Vec3d> positions) {

        public <E extends Entity> BulkParticlesPacket(Collection<E> entities, ParticleEffect particle, double deviation) {
            this(particle, deviation, new ArrayList<>());
            for (var entity : entities) this.positions.add(entity.getPos());
        }

    }
}
