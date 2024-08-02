package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.affinity.recipe.AspenInfusionRecipe;
import io.wispforest.owo.ops.WorldOps;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AspRiteCoreBlockEntity extends RitualCoreBlockEntity {

    @Nullable private AspenInfusionRecipe cachedRecipe = null;

    public AspRiteCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ASP_RITE_CORE, pos, state);

        this.fluxStorage.setFluxCapacity(16000);
        this.fluxStorage.setMaxInsert(200);
    }

    @Override
    protected boolean onRitualStart(RitualSetup setup) {
        final var inventory = new AspenInfusionRecipeInput(setup.resolveSocles(this.world), this.item);
        final var recipeOptional = this.world.getRecipeManager().getFirstMatch(AffinityRecipeTypes.ASPEN_INFUSION, inventory, this.world);

        if (recipeOptional.isEmpty()) return false;
        this.cachedRecipe = recipeOptional.get().value();

        setup.configureLength(this.cachedRecipe.duration);

        return true;
    }

    @Override
    protected void doRitualTick() {
        if (this.cachedRecipe.fluxCostPerTick > 0) {
            if (this.flux() < this.cachedRecipe.fluxCostPerTick) {
                this.endRitual(this::onRitualInterrupted, false);
                return;
            } else {
                this.updateFlux(this.flux() - this.cachedRecipe.fluxCostPerTick);
            }
        }

        if (this.ritualTick % 10 == 0) {
            AffinityParticleSystems.ASPEN_INFUSION_ACTIVE.spawn(this.world, Vec3d.ofCenter(this.pos, .85f));
        }

        if (this.ritualTick % 80 == 0) {
            WorldOps.playSound(this.world, this.pos, AffinitySoundEvents.BLOCK_ASP_RITE_CORE_ACTIVE, SoundCategory.BLOCKS);
        }
    }

    @Override
    protected boolean onRitualCompleted() {
        this.item = this.cachedRecipe.getResult(null);

        AffinityParticleSystems.ASPEN_INFUSION_CRAFT.spawn(this.world, Vec3d.ofCenter(this.pos, 1f));
        WorldOps.playSound(this.world, this.pos, AffinitySoundEvents.BLOCK_ASP_RITE_CORE_CRAFT, SoundCategory.BLOCKS);

        return true;
    }

    @Override
    protected boolean onRitualInterrupted() {
        AffinityParticleSystems.ASPEN_INFUSION_FAILS.spawn(this.world, Vec3d.ofCenter(this.pos, .85), this.item);
        return false;
    }

    @Override
    protected Vec3d modulatorStreamTargetPos() {
        return Vec3d.ofCenter(this.pos, .85);
    }

    public static class AspenInfusionRecipeInput extends SocleRecipeInput {

        private final ItemStack primaryInput;

        public AspenInfusionRecipeInput(List<RitualSocleBlockEntity> socles, ItemStack primaryInput) {
            super(socles);
            this.primaryInput = primaryInput.copy();
        }

        public ItemStack primaryInput() {
            return primaryInput;
        }
    }
}
