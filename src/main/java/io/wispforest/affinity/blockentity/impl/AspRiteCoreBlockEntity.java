package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.affinity.recipe.AspenInfusionRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AspRiteCoreBlockEntity extends RitualCoreBlockEntity {

    @Nullable private AspenInfusionRecipe cachedRecipe = null;

    public AspRiteCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ASP_RITE_CORE, pos, state);
    }

    @Override
    protected boolean onRitualStart(RitualSetup setup) {
        final var inventory = new AspenInfusionInventory(setup.resolveSocles(this.world), this.item);
        final var recipeOptional = this.world.getRecipeManager().getFirstMatch(AffinityRecipeTypes.ASPEN_INFUSION, inventory, this.world);

        if (recipeOptional.isEmpty()) return false;
        this.cachedRecipe = recipeOptional.get();

        setup.configureLength(this.cachedRecipe.getDuration());

        return true;
    }

    @Override
    protected void doRitualTick() {
        if (this.ritualTick % 10 != 0) return;
        AffinityParticleSystems.ASPEN_INFUSION_ACTIVE.spawn(this.world, Vec3d.ofCenter(this.pos, .85f));
    }

    @Override
    protected boolean onRitualCompleted() {
        this.item = this.cachedRecipe.getOutput();
        AffinityParticleSystems.ASPEN_INFUSION_CRAFT.spawn(this.world, Vec3d.ofCenter(this.pos, 1f));

        return true;
    }

    @Override
    protected boolean onRitualInterrupted() {
        return false;
    }

    public static class AspenInfusionInventory extends SocleInventory {

        private final ItemStack primaryInput;

        public AspenInfusionInventory(List<RitualSocleBlockEntity> socles, ItemStack primaryInput) {
            super(socles);
            this.primaryInput = primaryInput.copy();
        }

        public ItemStack primaryInput() {
            return primaryInput;
        }
    }
}
