package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.SpiritIntegrationApparatusBlock;
import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.misc.DamageTypeKey;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.*;
import io.wispforest.affinity.recipe.SpiritAssimilationRecipe;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ops.WorldOps;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpiritIntegrationApparatusBlockEntity extends RitualCoreBlockEntity {

    public static final Vec3d PARTICLE_OFFSET = new Vec3d(.5, .85, .5);
    private static final DamageTypeKey DAMAGE_TYPE = new DamageTypeKey(Affinity.id("ritual_sacrifice"));

    @Nullable private SpiritAssimilationRecipe cachedRecipe = null;
    @Nullable private ItemStack cachedResult = null;
    @Nullable private SpiritIntegrationApparatusBlock.ApparatusSet neighborPositions = null;
    @Nullable private SpiritIntegrationApparatusBlockEntity[] cachedNeighbors = null;

    public final RitualLock<SpiritIntegrationApparatusBlockEntity> ritualLock = new RitualLock<>();

    public SpiritIntegrationApparatusBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.SPIRIT_INTEGRATION_APPARATUS, pos, state);

        this.fluxStorage.setFluxCapacity(24000);
        this.fluxStorage.setMaxInsert(500);

        this.storageProvider.active(() -> !this.ritualLock.isHeld() && this.storageProvider.active().getAsBoolean());
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (this.ritualLock.isHeld()) return ActionResult.PASS;
        return super.onUse(player, hand, hit);
    }

    @Override
    protected boolean onRitualStart(RitualSetup setup) {
        if (this.ritualLock.isHeld()) return false;

        this.neighborPositions = SpiritIntegrationApparatusBlock.findValidApparatusSet(this.world, this.pos);
        if (this.neighborPositions == null) return false;

        final var sacrifices = this.world.getNonSpectatingEntities(LivingEntity.class, new Box(this.ritualCenterPos().up()).expand(1));
        if (sacrifices.isEmpty()) return false;

        final var coreItems = new ItemStack[4];
        coreItems[0] = this.item.copy();

        this.cachedNeighbors = this.neighborPositions.resolve(this.world);
        for (int i = 0; i < this.cachedNeighbors.length; i++) {
            coreItems[i + 1] = this.cachedNeighbors[i].item.copy();
        }

        final var sacrifice = sacrifices.get(0);
        final var inventory = new SpiritAssimilationRecipeInput(setup.resolveSocles(this.world), coreItems, sacrifice);
        final var recipeOptional = this.world.getRecipeManager().getFirstMatch(AffinityRecipeTypes.SPIRIT_ASSIMILATION, inventory, this.world);

        if (recipeOptional.isEmpty()) return false;
        this.cachedRecipe = recipeOptional.get().value();
        this.cachedResult = this.cachedRecipe.craft(inventory, this.world.getRegistryManager());

        setup.configureLength(this.cachedRecipe.duration);
        this.ritualLock.acquire(this);
        this.createDissolveParticle(this.item, this.pos, setup.duration());

        for (var neighbor : this.cachedNeighbors) {
            neighbor.ritualLock.acquire(this);
            this.createDissolveParticle(neighbor.item, neighbor.pos, setup.duration());
        }

        AffinityParticleSystems.LAVA_ERUPTION.spawn(this.world, MathUtil.entityCenterPos(sacrifice));
        WorldOps.playSound(this.world, this.pos, AffinitySoundEvents.BLOCK_SPIRIT_INTEGRATION_APPARATUS_RITUAL_START, SoundCategory.BLOCKS);

        if (sacrifice instanceof ServerPlayerEntity serverPlayer) {
            AffinityCriteria.SACRIFICED_TO_RITUAL.trigger(serverPlayer);
        }

        if (sacrifice instanceof Tameable tameable && tameable.getOwnerUuid() != null) {
            var players = world.getNonSpectatingEntities(ServerPlayerEntity.class, new Box(this.pos).expand(7, 3, 7));
            for (var player : players) {
                AffinityCriteria.SACRIFICE_PET.trigger(player, sacrifice);
            }
        }

        sacrifice.getComponent(AffinityComponents.ENTITY_FLAGS).setFlag(EntityFlagComponent.NO_DROPS);
        sacrifice.damage(DAMAGE_TYPE.source(this.world), Float.MAX_VALUE);

        return true;
    }

    @Override
    protected void doRitualTick() {
        if (this.cachedRecipe.fluxCostPerTick > 0) {
            int fluxCostPerCore = this.cachedRecipe.fluxCostPerTick / 4;

            if (this.testFluxSupply(fluxCostPerCore)) {
                this.updateFlux(this.flux() - fluxCostPerCore);

                for (var neighbor : this.cachedNeighbors) {
                    neighbor.updateFlux(neighbor.flux() - fluxCostPerCore);
                }
            } else {
                this.endRitual(this::onRitualInterrupted, false);
                return;
            }
        }

        if (this.ritualTick % 3 == 0) {
            AffinityParticleSystems.SPIRIT_ASSIMILATION_ACTIVE.spawn(this.world, Vec3d.ofCenter(this.pos), this.neighborPositions);
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

    private boolean testFluxSupply(int requiredFlux) {
        if (this.flux() < requiredFlux) return false;

        for (var neighbor : this.cachedNeighbors) {
            if (neighbor.flux() < requiredFlux) return false;
        }

        return true;
    }

    @Override
    protected boolean onRitualCompleted() {
        final var pos = Vec3d.ofCenter(this.ritualCenterPos(), 2.5);

        var item = new ItemEntity(this.world, pos.x, pos.y - .25, pos.z, this.cachedResult);
        item.setVelocity(
                this.world.random.nextTriangular(0.0, 0.115),
                this.world.random.nextTriangular(0.2, 0.115),
                this.world.random.nextTriangular(0.0, 0.115)
        );
        item.getComponent(AffinityComponents.ENTITY_FLAGS).setFlag(EntityFlagComponent.ITEM_GLOW);
        AffinityComponents.ENTITY_FLAGS.sync(item);
        this.world.spawnEntity(item);
        AffinityParticleSystems.ARCANE_FADE_CRAFT.spawn(this.world, pos);

        this.ritualLock.release();

        this.cachedRecipe = null;
        this.cachedResult = null;
        this.cachedNeighbors = null;
        this.neighborPositions = null;

        WorldOps.playSound(world, pos, AffinitySoundEvents.BLOCK_SPIRIT_INTEGRATION_APPARATUS_DROP_ITEM, SoundCategory.BLOCKS);

        return true;
    }

    @Override
    protected boolean onRitualInterrupted() {
        if (this.cachedNeighbors != null) {
            for (var neighbor : this.cachedNeighbors) {
                neighbor.ritualLock.release();
            }

            this.ritualLock.release();

            var items = Arrays.stream(this.cachedNeighbors).map(SpiritIntegrationApparatusBlockEntity::getItem).collect(Collectors.toList());
            items.add(this.item);

            AffinityParticleSystems.SPIRIT_ASSIMILATION_FAILS.spawn(
                    this.world,
                    Vec3d.ofCenter(this.ritualCenterPos().up(2)),
                    new AffinityParticleSystems.SpiritAssimilationStacksData(items)
            );
        }

        return false;
    }

    @Override
    protected Vec3d modulatorStreamTargetPos() {
        return Vec3d.ofCenter(this.ritualCenterPos(), 2.5);
    }

    @Override
    protected void endRitual(Supplier<Boolean> handlerImpl, boolean clearItems) {
        if (this.ritualTick > 0) {
            final var positions = new ArrayList<>(Arrays.asList(this.neighborPositions.apparatuses()));
            positions.add(this.pos);
            AffinityNetwork.server(this).send(new RemoveBezierEmitterParticlesPacket(positions, PARTICLE_OFFSET));
        }

        super.endRitual(handlerImpl, clearItems);
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

        if (!this.ritualLock.isHeld()) return;
        this.ritualLock.holder().endRitual(this.ritualLock.holder()::onRitualInterrupted, false);
    }

    @Override
    public BlockPos ritualCenterPos() {
        final var set = neighborPositions != null ? neighborPositions : SpiritIntegrationApparatusBlock.findValidApparatusSet(this.world, this.pos);
        return set != null ? set.center() : this.pos;
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        var offset = this.ritualCenterPos().subtract(this.pos);
        return CuboidRenderer.Cuboid.of(new BlockPos(-8, 0, -8).add(offset), new BlockPos(9, 1, 9).add(offset));
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);

        var possibleCoreSets = SpiritIntegrationApparatusBlock.possibleValidApparatusSets(this.pos);

        var mostCompleteSet = possibleCoreSets.get(0);
        for (var set : possibleCoreSets) {
            if (set.validApparatusCount(this.world) > mostCompleteSet.validApparatusCount(this.world)) {
                mostCompleteSet = set;
            }
        }

        int missingCoreCount = 3 - mostCompleteSet.validApparatusCount(this.world);
        if (missingCoreCount != 0) {
            entries.add(Entry.text(
                    TextOps.withColor("❌ ", 0xEB1D36),
                    Text.translatable(
                            missingCoreCount == 1
                                    ? "block.affinity.spirit_integration_apparatus.tooltip.incomplete.singular"
                                    : "block.affinity.spirit_integration_apparatus.tooltip.incomplete.plural",
                            missingCoreCount
                    )
            ));
        } else {
            entries.add(Entry.text(
                    TextOps.withColor("✔", 0x28FFBF),
                    Text.translatable("block.affinity.spirit_integration_apparatus.tooltip.complete")
            ));
        }
    }

    private void createDissolveParticle(ItemStack item, BlockPos from, int duration) {
        AffinityParticleSystems.DISSOLVE_ITEM.spawn(this.world, Vec3d.of(from).add(PARTICLE_OFFSET),
                new AffinityParticleSystems.DissolveData(item, Vec3d.ofCenter(this.ritualCenterPos().up(2)), duration - 10, 16));
    }

    public static class SpiritAssimilationRecipeInput extends SocleRecipeInput {

        private final ItemStack[] coreInputs;
        private final Entity sacrifice;

        public SpiritAssimilationRecipeInput(List<RitualSocleBlockEntity> socles, ItemStack[] coreInputs, Entity sacrifice) {
            super(socles);

            this.coreInputs = new ItemStack[4];
            for (int i = 0; i < coreInputs.length; i++) {
                this.coreInputs[i] = coreInputs[i].copy();
            }

            this.sacrifice = sacrifice;
        }

        public ItemStack[] coreInputs() {
            return coreInputs;
        }

        public Entity sacrifice() {
            return this.sacrifice;
        }
    }
}
