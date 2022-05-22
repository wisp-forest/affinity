package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityEnchantments;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.apache.commons.lang3.mutable.MutableInt;

public class AffineInfuserBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    private static final int REPAIR_COST_PER_ITEM = 100;

    private static final MutableInt currentRepairCost = new MutableInt();

    public AffineInfuserBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AFFINE_INFUSER, pos, state);

        this.fluxStorage.setFluxCapacity(64000);
        this.fluxStorage.setMaxInsert(256);
    }

    @Override
    public void tickServer() {
        if (this.world.getTime() % 10 != 0) return;

        final var searchArea = new Box(this.pos).expand(32);
        currentRepairCost.setValue(0);

        for (var entity : this.world.getNonSpectatingEntities(Entity.class, searchArea)) {
            if (currentRepairCost.getValue() > this.flux() - REPAIR_COST_PER_ITEM) break;

            if (entity instanceof PlayerEntity) {
                entity.getItemsEquipped().forEach(AffineInfuserBlockEntity::repairIfEnchanted);
            } else if (entity instanceof ItemFrameEntity frame) {
                repairIfEnchanted(frame.getHeldItemStack());
            } else if (entity instanceof ItemEntity item) {
                repairIfEnchanted(item.getStack());
            }
        }

        this.updateFlux(this.flux() - currentRepairCost.getValue());
    }

    private static void repairIfEnchanted(ItemStack stack) {
        if (EnchantmentHelper.getLevel(AffinityEnchantments.AFFINE, stack) < 1) return;
        if (stack.getDamage() < 1) return;

        stack.setDamage(stack.getDamage() - 1);
        currentRepairCost.add(REPAIR_COST_PER_ITEM);
    }
}
