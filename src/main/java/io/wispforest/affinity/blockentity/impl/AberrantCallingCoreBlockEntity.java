package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.AberrantCallingCoreBlock;
import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.misc.EntityReference;
import io.wispforest.affinity.misc.ServerTasks;
import io.wispforest.affinity.recipe.AberrantCallingRecipe;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ops.WorldOps;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class AberrantCallingCoreBlockEntity extends RitualCoreBlockEntity {

    public static final Vec3d PARTICLE_OFFSET = new Vec3d(.5, 0.85, .5);

    private static final NbtKey<ItemStack> ITEM_KEY = new NbtKey<>("Item", NbtKey.Type.ITEM_STACK);
    private @NotNull ItemStack item = ItemStack.EMPTY;

    @Nullable private AberrantCallingRecipe cachedRecipe = null;
    @Nullable private AberrantCallingCoreBlock.CoreSet neighborPositions = null;
    @Nullable private AberrantCallingCoreBlockEntity[] cachedNeighbors = null;

    public final RitualLock<AberrantCallingCoreBlockEntity> ritualLock = new RitualLock<>();

    public AberrantCallingCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ABERRANT_CALLING_CORE, pos, state);
    }

    @Override
    protected ActionResult handleNormalUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (this.world.isClient()) return ActionResult.SUCCESS;
        if (this.ritualLock.isActive()) return ActionResult.PASS;

        return InteractionUtil.handleSingleItemContainer(this.world, this.pos, player, hand,
                () -> this.item, stack -> this.item = stack, this::markDirty);
    }

    @Override
    protected boolean onRitualStart(RitualSetup setup) {
        if (this.ritualLock.isActive()) return false;
        if (this.item.isEmpty()) return false;

        this.neighborPositions = AberrantCallingCoreBlock.findValidCoreSet(this.world, this.pos);
        if (this.neighborPositions == null) return false;

        final var coreItems = new ItemStack[4];
        coreItems[0] = this.item.copy();

        this.cachedNeighbors = this.neighborPositions.resolve(this.world);
        for (int i = 0; i < this.cachedNeighbors.length; i++) {
            coreItems[i + 1] = this.cachedNeighbors[i].item.copy();
        }

        final var inventory = new AberrantCallingInventory(setup.resolveSocles(this.world), coreItems);
        final var recipeOptional = this.world.getRecipeManager()
                .getFirstMatch(AffinityRecipeTypes.ABERRANT_CALLING, inventory, this.world);

        if (recipeOptional.isEmpty()) return false;
        this.cachedRecipe = recipeOptional.get();

        setup.configureLength(this.cachedRecipe.getDuration());
        this.ritualLock.acquire(this);
        this.createDissolveParticle(this.item, this.pos, setup.duration());

        for (var neighbor : this.cachedNeighbors) {
            neighbor.ritualLock.acquire(this);
            this.createDissolveParticle(neighbor.item, neighbor.pos, setup.duration());
        }

        return true;
    }

    @Override
    protected void doRitualTick() {
        if (this.ritualTick % 3 == 0) {
            AffinityParticleSystems.ABERRANT_CALLING_ACTIVE.spawn(this.world, Vec3d.ofCenter(this.pos), this.neighborPositions);
        }

        if (this.ritualTick == this.cachedSetup.duration() - 10) {
            this.item = ItemStack.EMPTY;
            this.markDirty();

            for (var neighbor : this.cachedNeighbors) {
                neighbor.ritualLock.release();

                neighbor.item = ItemStack.EMPTY;
                neighbor.markDirty();
            }
        }
    }

    @Override
    protected boolean onRitualCompleted() {
        final var pos = Vec3d.ofCenter(this.ritualCenterPos());

        final var entity = this.cachedRecipe.getEntityType().create(world);
        if (this.cachedRecipe.getEntityNbt() != null) entity.readNbt(this.cachedRecipe.getEntityNbt());

        entity.setPos(pos.x, pos.y + 1.5, pos.z);
        entity.addVelocity(0, 1, 0);

        final var ref = EntityReference.of(entity);
        entity.setInvulnerable(true);
        ServerTasks.doDelayed((ServerWorld) this.world, 30, () -> {
            ref.consume(e -> e.setInvulnerable(false));
        });

        this.world.spawnEntity(entity);
        if (entity instanceof MobEntity mob) mob.playSpawnEffects();

        this.ritualLock.release();

        this.cachedNeighbors = null;
        this.neighborPositions = null;

        WorldOps.playSound(world, pos, AffinitySoundEvents.BLOCK_ABERRANT_CALLING_CORE_RITUAL_SUCCESS, SoundCategory.BLOCKS);
        AffinityParticleSystems.ABERRANT_CALLING_SUCCESS.spawn(world, pos.add(0, 2, 0));

        return true;
    }

    @Override
    protected boolean onRitualInterrupted() {
        if (this.cachedNeighbors != null) {
            for (var neighbor : this.cachedNeighbors) {
                neighbor.ritualLock.release();
            }

            this.ritualLock.release();
        }

        return false;
    }

    @Override
    protected void finishRitual(Supplier<Boolean> handlerImpl, boolean clearItems) {
        if (this.ritualTick > 0) {
            final var positions = new ArrayList<>(Arrays.asList(this.neighborPositions.cores()));
            positions.add(this.pos);
            AffinityNetwork.server(this).send(new RemoveBezierEmitterParticlesPacket(positions, PARTICLE_OFFSET));
        }

        super.finishRitual(handlerImpl, clearItems);
    }

    @Override
    protected void activateSocle(@NotNull RitualSocleBlockEntity socle) {
        double shortestDistance = this.pos.getSquaredDistance(socle.getPos());
        BlockPos closestCore = this.getPos();

        for (var neighbor : this.cachedNeighbors) {
            final var neighborPos = neighbor.getPos();
            final var distance = neighborPos.getSquaredDistance(socle.getPos());

            if (distance < shortestDistance) {
                shortestDistance = distance;
                closestCore = neighborPos;
            }
        }

        socle.beginExtraction(closestCore, this.cachedSetup.durationPerSocle());
    }

    @Override
    public void onBroken() {
        super.onBroken();

        if (!this.ritualLock.isActive()) return;
        this.ritualLock.holder().finishRitual(this.ritualLock.holder()::onRitualInterrupted, false);
    }

    @Override
    public BlockPos ritualCenterPos() {
        final var set = neighborPositions != null ? neighborPositions : AberrantCallingCoreBlock.findValidCoreSet(this.world, this.pos);
        return set != null ? set.center() : this.pos;
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);

        var possibleCoreSets = AberrantCallingCoreBlock.possibleValidCoreSets(this.pos);

        var mostCompleteSet = possibleCoreSets.get(0);
        for (var set : possibleCoreSets) {
            if (set.validCoreCount(this.world) > mostCompleteSet.validCoreCount(this.world)) {
                mostCompleteSet = set;
            }
        }

        int missingCoreCount = 3 - mostCompleteSet.validCoreCount(this.world);
        if (missingCoreCount != 0) {
            entries.add(Entry.text(
                    TextOps.withColor("❌ ", 0xEB1D36),
                    Text.translatable(
                            missingCoreCount == 1
                                    ? "block.affinity.aberrant_calling_core.tooltip.incomplete.singular"
                                    : "block.affinity.aberrant_calling_core.tooltip.incomplete.plural",
                            missingCoreCount
                    )
            ));
        } else {
            entries.add(Entry.text(
                    TextOps.withColor("✔", 0x28FFBF),
                    Text.translatable("block.affinity.aberrant_calling_core.tooltip.complete")
            ));
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

    private void createDissolveParticle(ItemStack item, BlockPos from, int duration) {
        AffinityParticleSystems.DISSOLVE_ITEM.spawn(this.world, Vec3d.of(from).add(PARTICLE_OFFSET),
                new AffinityParticleSystems.DissolveData(item, Vec3d.ofCenter(this.ritualCenterPos().up(2)), duration - 10, 16));
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    public static class AberrantCallingInventory extends SocleInventory {

        private final ItemStack[] coreInputs;

        public AberrantCallingInventory(List<RitualSocleBlockEntity> socles, ItemStack[] coreInputs) {
            super(socles);

            this.coreInputs = new ItemStack[4];
            for (int i = 0; i < coreInputs.length; i++) {
                this.coreInputs[i] = coreInputs[i].copy();
            }
        }

        public ItemStack[] coreInputs() {
            return coreInputs;
        }
    }
}
