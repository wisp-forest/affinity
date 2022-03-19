package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.misc.NbtKey;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.affinity.particle.ColoredFlameParticleEffect;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RitualCoreBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity {

    private final NbtKey<ItemStack> ITEM_KEY = new NbtKey<>("item", NbtKey.Type.ITEM_STACK);

    @NotNull private ItemStack item = ItemStack.EMPTY;
    @Nullable private RitualConfiguration cachedConfiguration = null;
    private int ritualTick = -1;
    private int lastActivatedSocle = -1;

    public RitualCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.RITUAL_CORE, pos, state);
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking()) {
            return this.tryStartRitual();
        } else {
            if (player.getStackInHand(hand).isOf(AffinityItems.WAND_OF_INQUIRY)) return ActionResult.PASS;
            if (this.world.isClient()) return ActionResult.SUCCESS;

            return InteractionUtil.handleSingleItemContainer(this.world, this.pos, player, hand,
                    () -> this.item, stack -> this.item = stack, this::markDirty);
        }
    }

    public ActionResult tryStartRitual() {
        if (this.item.isEmpty()) return ActionResult.PASS;
        if (this.world.isClient()) return ActionResult.SUCCESS;

        var configuration = this.examineConfiguration();
        if (configuration.isEmpty()) return ActionResult.PASS;

        this.cachedConfiguration = configuration;
        Collections.shuffle(this.cachedConfiguration.socles(), this.world.random);

        this.ritualTick = 0;
        return ActionResult.SUCCESS;
    }

    @Override
    public void tickServer() {
        if (this.ritualTick < 0) return;

        if (this.ritualTick % 5 == 0 && ++this.lastActivatedSocle < this.cachedConfiguration.socles().size()) {
            var entity = this.world.getBlockEntity(this.cachedConfiguration.socles().get(this.lastActivatedSocle).position());
            if (entity instanceof RitualSocleBlockEntity socle) socle.beginExtraction(this.pos);
        }

        if (this.ritualTick++ >= this.cachedConfiguration.length()) {
            this.ritualTick = -1;
            this.lastActivatedSocle = -1;
            this.cachedConfiguration = null;

            this.item = ItemStack.EMPTY;
            this.markDirty();
        }
    }

    public RitualConfiguration examineConfiguration() {
        var soclePOIs = ((ServerWorld) this.world).getPointOfInterestStorage().getInCircle(type -> type == AffinityPoiTypes.RITUAL_SOCLE,
                this.pos, 10, PointOfInterestStorage.OccupationStatus.ANY).filter(poi -> poi.getPos().getY() == this.pos.getY()).toList();

        double stability = 75;

        List<Double> allDistances = new ArrayList<>((soclePOIs.size() - 1) * soclePOIs.size());

        var socles = new ArrayList<RitualSocleEntry>();
        for (var soclePOI : soclePOIs) {
            double meanDistance = 0;
            double minDistance = Double.MAX_VALUE;

            final var soclePos = soclePOI.getPos();
            for (var other : soclePOIs) {
                if (other == soclePOI) continue;

                final var distance = MathUtil.distance(soclePos, other.getPos());

                meanDistance += distance;
                allDistances.add(distance);
                if (distance < minDistance) minDistance = distance;
            }

            meanDistance /= (soclePOIs.size() - 1d);
            socles.add(new RitualSocleEntry(soclePos, meanDistance, minDistance, MathUtil.distance(soclePos, this.pos)));
        }

        if (allDistances.isEmpty()) allDistances.add(0d);

        final double mean = MathUtil.mean(allDistances);
        final double standardDeviation = MathUtil.standardDeviation(mean, allDistances);
        final var distancePenalty = mean > 4.5 ? mean - 4.5 : 0;

        stability -= distancePenalty * 15;
        stability *= Math.min((mean / 75) + 1.5 / standardDeviation, 1.25);

        for (var socle : socles) {
            if (socle.coreDistance() < 1.5) {
                stability *= .5;
            } else if (socle.minDistance() < 1.25) {
                stability *= .975;
            } else {
                final var socleType = RitualSocleType.forBlockState(this.world.getBlockState(socle.position()));
                stability += (100 - stability) * (socleType == null ? 0 : socleType.stabilityModifier());
            }
        }

        return new RitualConfiguration(stability, socles.size() * 5 + 40, socles);
    }

    @Override
    public void onBroken() {
        super.onBroken();
        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.getItem());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.item = ITEM_KEY.get(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        ITEM_KEY.put(nbt, this.item);
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    private void sendDebugParticles(BlockPos standPos, PlayerEntity player, DyeColor color) {
        var packet = new ParticleS2CPacket(new ColoredFlameParticleEffect(color),
                false, standPos.getX() + .5, standPos.getY() + 1.2, standPos.getZ() + .5,
                .1f, .1f, .1f, 0, 10);

        ((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
    }

    public record RitualConfiguration(double stability, int length, List<RitualSocleEntry> socles) {
        public boolean isEmpty() {
            return socles.isEmpty();
        }
    }

    public record RitualSocleEntry(BlockPos position, double meanDistance, double minDistance, double coreDistance) {}
}
