package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityEnchantments;
import io.wispforest.affinity.object.AffinityStatusEffects;
import io.wispforest.affinity.particle.BezierPathEmitterParticleEffect;
import io.wispforest.affinity.particle.ColoredFallingDustParticleEffect;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.util.VectorRandomUtils;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public class AffineInfuserBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity, InquirableOutlineProvider {

    private static final MutableInt currentRepairCost = new MutableInt();

    public AffineInfuserBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AFFINE_INFUSER, pos, state);

        this.fluxStorage.setFluxCapacity(64000);
        this.fluxStorage.setMaxInsert(256);
    }

    @Override
    public void tickClient() {
        if (this.shouldBeDisabled() || this.flux() < repairCostPerItem() || this.world.random.nextFloat() > .15f) {
            return;
        }

        ClientParticles.spawn(
                new BezierPathEmitterParticleEffect(
                        new ColoredFallingDustParticleEffect(MathUtil.rgbToVec3f(Affinity.AETHUM_FLUX_COLOR.rgb())),
                        VectorRandomUtils.getRandomCenteredOnBlock(this.world, this.pos, 10),
                        40, 5, true
                ), this.world, Vec3d.ofCenter(pos), 0
        );
    }

    @Override
    public void tickServer() {
        if (this.shouldBeDisabled() || this.world.getTime() % 10 != 0) return;

        final var searchArea = new Box(this.pos).expand(32);
        currentRepairCost.setValue(0);

        for (var entity : this.world.getNonSpectatingEntities(Entity.class, searchArea)) {
            if (currentRepairCost.getValue() > this.flux() - repairCostPerItem()) break;

            if (entity instanceof LivingEntity living && living.hasStatusEffect(AffinityStatusEffects.AFFINE) && living.getMaxHealth() > living.getHealth()) {
                ((LivingEntity) entity).heal(1);
                currentRepairCost.add(repairCostPerItem());
            }

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

    private boolean shouldBeDisabled() {
        return this.world.getReceivedRedstonePower(this.pos) > 0;
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        return CuboidRenderer.Cuboid.symmetrical(32, 32, 32);
    }

    private static void repairIfEnchanted(ItemStack stack) {
        if (EnchantmentHelper.getLevel(AffinityEnchantments.AFFINE, stack) < 1) return;
        if (stack.getDamage() < 1) return;

        stack.setDamage(stack.getDamage() - 1);
        currentRepairCost.add(repairCostPerItem());
    }

    private static int repairCostPerItem() {
        return Affinity.CONFIG.affineInfuserCostPerDurabilityPoint();
    }
}
