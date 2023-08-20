package io.wispforest.affinity.blockentity.template;

import io.wispforest.affinity.blockentity.impl.FieldCoherenceModulatorBlockEntity;
import io.wispforest.affinity.blockentity.impl.RitualSocleBlockEntity;
import io.wispforest.affinity.client.particle.BezierPathEmitterParticle;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.entity.WiseWispEntity;
import io.wispforest.affinity.misc.ReadOnlyInventory;
import io.wispforest.affinity.misc.SingleStackStorageProvider;
import io.wispforest.affinity.misc.util.BlockFinder;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.owo.nbt.NbtKey;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class RitualCoreBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity, InquirableOutlineProvider {

    protected static final NbtKey<ItemStack> ITEM_KEY = new NbtKey<>("Item", NbtKey.Type.ITEM_STACK);

    @Nullable
    protected RitualSetup cachedSetup = null;

    @NotNull
    protected ItemStack item = ItemStack.EMPTY;
    protected final SingleStackStorageProvider storageProvider = new SingleStackStorageProvider(() -> this.item, stack -> this.item = stack, this::markDirty)
            .capacity(1)
            .active(() -> this.ritualTick < 0);

    protected int ritualTick = -1;
    protected int ritualFailureTick = -1;
    protected int lastActivatedSocle = -1;

    public RitualCoreBlockEntity(BlockEntityType<? extends RitualCoreBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Called when a ritual is about to start
     *
     * @return {@code true} if the ritual can
     * start given the current conditions
     */
    protected abstract boolean onRitualStart(RitualSetup setup);

    /**
     * Called every tick during a ritual, may well do nothing
     */
    protected abstract void doRitualTick();

    /**
     * Called when a ritual has finished
     *
     * @return {@code true} if state was modified
     * and {@link #markDirty()} should be called
     */
    protected abstract boolean onRitualCompleted();

    /**
     * Called when a ritual is interrupted, usually
     * because of a socle being removed
     *
     * @return {@code true} if state was modified
     * and {@link #markDirty()} should be called
     */
    protected abstract boolean onRitualInterrupted();

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking()) {
            return this.tryStartRitual();
        } else {
            if (this.world.isClient()) return ActionResult.SUCCESS;
            if (this.ritualTick >= 0) return ActionResult.PASS;

            return InteractionUtil.handleSingleItemContainer(this.world, this.pos, player, hand,
                    () -> this.item, stack -> this.item = stack, this::markDirty);
        }
    }

    @Override
    public void onBroken() {
        super.onBroken();
        this.endRitual(this::onRitualInterrupted, false);
        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.getItem());
    }

    protected void activateSocle(@NotNull RitualSocleBlockEntity socle) {
        socle.beginExtraction(this.pos, this.cachedSetup.durationPerSocle());
    }

    public ActionResult tryStartRitual() {
        if (this.world.isClient()) return ActionResult.SUCCESS;
        if (this.ritualTick >= 0 || this.item.isEmpty()) return ActionResult.PASS;

        var setup = examineSetup(this, false);
        if (setup.isEmpty()) return ActionResult.PASS;

        if (!this.onRitualStart(setup)) return ActionResult.PASS;
        if (setup.duration() < 0) {
            throw new IllegalStateException("No ritual length was configured. If you're a player, report this issue");
        }

        this.cachedSetup = setup;
        Collections.shuffle(this.cachedSetup.socles, ThreadLocalRandom.current());

        this.cachedSetup.forEachSocle(world, socle -> socle.ritualLock.acquire(this));
        this.cachedSetup.forEachModulator(world, modulator -> modulator.updateFlux(modulator.flux() - 32000));

        if (this.cachedSetup.stability / 100d < this.world.random.nextDouble()) {
            this.ritualFailureTick = 20 + this.world.random.nextInt(this.cachedSetup.duration() - 20);
        }

        this.ritualTick = 0;
        return ActionResult.SUCCESS;
    }

    @Override
    public void tickServer() {
        if (this.ritualTick < 0) return;

        if (this.cachedSetup.isSocleActivationTick(this.ritualTick) && ++this.lastActivatedSocle < this.cachedSetup.socles.size()) {
            var socle = this.cachedSetup.resolveSocle(world, this.lastActivatedSocle);
            if (socle != null) this.activateSocle(socle);
        }

        this.doRitualTick();
        if (this.ritualTick < 0) return;

        if (++this.ritualTick >= this.cachedSetup.duration()) {
            this.endRitual(this::onRitualCompleted, true);
        } else if (this.ritualTick == this.ritualFailureTick) {
            this.endRitual(this::onRitualInterrupted, false);
        }
    }

    public void onSocleDestroyed(BlockPos pos) {
        if (this.cachedSetup == null) return;
        if (!this.cachedSetup.hasSocleAt(pos)) return;

        this.onRitualInterrupted();

        this.endRitual(this::onRitualInterrupted, false);
    }

    protected void endRitual(Supplier<Boolean> handlerImpl, boolean clearItems) {
        if (this.ritualTick < 0) return;

        this.ritualTick = -1;
        this.ritualFailureTick = -1;
        this.lastActivatedSocle = -1;

        if (this.cachedSetup != null) {
            AffinityNetwork.CHANNEL.serverHandle(this).send(new RemoveBezierEmitterParticlesPacket(
                    this.cachedSetup.socles.stream().map(RitualSocleEntry::position).toList(),
                    RitualSocleBlockEntity.PARTICLE_OFFSET
            ));
            this.cachedSetup.forEachSocle(this.world, socle -> {
                socle.ritualLock.release();
                socle.stopExtraction(clearItems);
            });
        }
        this.cachedSetup = null;

        if (handlerImpl.get()) {
            this.markDirty();
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.item = nbt.get(ITEM_KEY);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put(ITEM_KEY, this.item);
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    public BlockPos ritualCenterPos() {
        return this.pos;
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        return CuboidRenderer.Cuboid.symmetrical(8, 0, 8);
    }

    public static RitualSetup examineSetup(RitualCoreBlockEntity core, boolean includeEmptySocles) {
        return examineSetup((ServerWorld) core.world, core.ritualCenterPos(), includeEmptySocles);
    }

    @SuppressWarnings("ConstantConditions")
    public static RitualSetup examineSetup(ServerWorld world, BlockPos pos, boolean includeEmptySocles) {
        var soclePOIs = BlockFinder.findPoi(world, AffinityPoiTypes.RITUAL_SOCLE, pos, 8)
                .filter(poi -> poi.getPos().getY() == pos.getY()).toList();

        double stability = world.getChunk(pos).getComponent(AffinityComponents.CHUNK_AETHUM).aethumAt(pos.getX(), pos.getZ());

        List<Double> allDistances = new ArrayList<>((soclePOIs.size() - 1) * soclePOIs.size());

        var socles = new ArrayList<RitualSocleEntry>();
        for (var soclePOI : soclePOIs) {

            if (!(world.getBlockEntity(soclePOI.getPos()) instanceof RitualSocleBlockEntity socle)) continue;
            if (socle.ritualLock.isActive()) continue;

            if (!includeEmptySocles) {
                if (socle.getItem().isEmpty()) continue;
            }

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
            socles.add(new RitualSocleEntry(soclePos, meanDistance, minDistance, MathUtil.distance(soclePos, pos)));
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
                final var socleType = RitualSocleType.forBlockState(world.getBlockState(socle.position()));
                stability += (100 - stability) * (socleType == null ? 0 : socleType.stabilityModifier());
            }
        }

        stability += (100 - stability) * 0.05 * world.getEntitiesByClass(WiseWispEntity.class, new Box(pos).expand(5, 3, 5), Entity::isAlive).size();

        var modulators = BlockFinder.findPoi(world, AffinityPoiTypes.FIELD_COHERENCE_MODULATOR, pos, 6)
                .map(PointOfInterest::getPos)
                .filter(modulatorPos -> {
                    if (modulatorPos.getY() - pos.getY() != 1) return false;
                    if (!(world.getBlockEntity(modulatorPos) instanceof FieldCoherenceModulatorBlockEntity modulator)) return false;
                    return modulator.flux() >= 32000;
                }).toList();

        stability = Math.min(100, stability + 5 * modulators.size());

        if (world.getBlockState(pos.down()).isOf(AffinityBlocks.INVERSION_STONE)) {
            stability = 100 - stability;
        }

        return new RitualSetup(MathHelper.clamp(stability, 0, 100), socles, modulators);
    }

    public static final class RitualSetup {
        public final double stability;
        public final List<RitualSocleEntry> socles;
        public final List<BlockPos> modulators;

        private int duration = -1;
        private int durationPerSocle = -1;
        private int socleDelay = -1;

        public RitualSetup(double stability, List<RitualSocleEntry> socles, List<BlockPos> modulators) {
            this.stability = stability;
            this.socles = socles;
            this.modulators = modulators;
        }

        public boolean isEmpty() {
            return socles.isEmpty();
        }

        public int duration() {
            return this.duration;
        }

        public int durationPerSocle() {
            return this.durationPerSocle;
        }

        public boolean isSocleActivationTick(int tick) {
            return tick % this.socleDelay == 0;
        }

        public boolean hasSocleAt(BlockPos pos) {
            return this.socles.stream().anyMatch(ritualSocleEntry -> ritualSocleEntry.position().equals(pos));
        }

        public void configureLength(int length) {
            this.duration = length;

            this.socleDelay = (int) Math.floor(length * (1 / (this.socles.size() + 2f)));
            this.durationPerSocle = length - this.socleDelay * (this.socles.size() - 1);
        }

        public List<RitualSocleBlockEntity> resolveSocles(World world) {
            return this.socles.stream()
                    .map(RitualSocleEntry::position)
                    .map(world::getBlockEntity)
                    .filter(RitualSocleBlockEntity.class::isInstance)
                    .map(RitualSocleBlockEntity.class::cast)
                    .toList();
        }

        public RitualSocleBlockEntity resolveSocle(World world, int index) {
            final var entity = world.getBlockEntity(this.socles.get(index).position());
            return entity instanceof RitualSocleBlockEntity socle ? socle : null;
        }

        public List<FieldCoherenceModulatorBlockEntity> resolveModulators(World world) {
            return this.modulators.stream()
                    .map(world::getBlockEntity)
                    .filter(FieldCoherenceModulatorBlockEntity.class::isInstance)
                    .map(FieldCoherenceModulatorBlockEntity.class::cast)
                    .toList();
        }

        public void forEachSocle(World world, Consumer<RitualSocleBlockEntity> action) {
            for (var socle : this.resolveSocles(world)) action.accept(socle);
        }

        public void forEachModulator(World world, Consumer<FieldCoherenceModulatorBlockEntity> action) {
            for (var socle : this.resolveModulators(world)) action.accept(socle);
        }
    }

    public record RitualSocleEntry(BlockPos position, double meanDistance, double minDistance, double coreDistance) {}

    static {
        AffinityNetwork.CHANNEL.registerClientbound(RemoveBezierEmitterParticlesPacket.class, (message, access) -> {
            for (var pos : message.soclePositions()) {
                BezierPathEmitterParticle.removeParticleAt(Vec3d.of(pos).add(message.offset()));
            }
        });

        //noinspection UnstableApiUsage
        ItemStorage.SIDED.registerForBlockEntities((entity, direction) -> {
            var core = (RitualCoreBlockEntity) entity;
            return core.storageProvider;
        }, AffinityBlocks.Entities.ASP_RITE_CORE, AffinityBlocks.Entities.SPIRIT_INTEGRATION_APPARATUS);
    }

    public record RemoveBezierEmitterParticlesPacket(List<BlockPos> soclePositions, Vec3d offset) {}

    public static class SocleInventory implements ReadOnlyInventory.ListBacked {

        protected final DefaultedList<ItemStack> items;

        public SocleInventory(List<RitualSocleBlockEntity> socles) {
            this.items = DefaultedList.ofSize(socles.size(), ItemStack.EMPTY);

            for (int i = 0; i < socles.size(); i++) {
                this.items.set(i, socles.get(i).getItem());
            }
        }

        @Override
        public List<ItemStack> delegate() {
            return this.items;
        }
    }

}