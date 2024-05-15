package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.ServerTasks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

public class AethumFireExtinguisherItem extends StaffItem {

    public AethumFireExtinguisherItem() {
        super(AffinityItems.settings().maxCount(1));
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock) {
        final var lookDirection = player.getRotationVec(0f);
        final var streamOrigin = player.getEyePos().subtract(0, .5f, 0);

        if (!(world instanceof ServerWorld serverWorld)) {
            ClientParticles.setParticleCount(3);
            ClientParticles.setVelocity(lookDirection);
            ClientParticles.spawn(ParticleTypes.CLOUD, world, streamOrigin.add(lookDirection), 1.25f);

            if (world.getTime() % 5 == 0) {
                world.playSound(player.getX(), player.getY(), player.getZ(), SoundEvent.of(Affinity.id("item.aethum_fire_extinguisher.spray")), SoundCategory.PLAYERS, 1f, 1f, false);
            }
        } else {
            var iterationPos = streamOrigin;
            var entitiesInPath = new HashSet<Entity>();
            for (int i = 0; i < 10; i++) {
                if (!world.getBlockState(BlockPos.ofFloored(iterationPos)).isAir()) break;

                entitiesInPath.addAll(world.getNonSpectatingEntities(Entity.class, new Box(iterationPos.subtract(1, 1, 1), iterationPos.add(1, 1, 1))));
                iterationPos = iterationPos.add(lookDirection);
            }

            entitiesInPath.remove(player);
            entitiesInPath.forEach(entity -> {
                entity.extinguish();
                entity.addVelocity(lookDirection.multiply(.075f));
                entity.velocityDirty = true;
            });

            var targetPos = player.raycast(10, 0f, false);
            if (targetPos instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
                ServerTasks.doDelayed(serverWorld, (int) (Math.sqrt(blockHit.squaredDistanceTo(player))), () -> {
                    for (var pos : BlockPos.iterate(blockHit.getBlockPos().add(-1, -1, -1), blockHit.getBlockPos().add(1, 1, 1))) {
                        if (!world.getBlockState(pos).isOf(Blocks.FIRE)) continue;
                        world.breakBlock(pos, false, player);
                    }
                });
            }
        }

        var aethum = player.getComponent(AffinityComponents.PLAYER_AETHUM);
        if (player.isFallFlying() && (aethum.tryConsumeAethum(this.getAethumConsumption(stack)) || player.isCreative())) {
            player.addVelocity(lookDirection.multiply(-.2));
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return 1.5f / 20f;
    }

    @Override
    protected boolean isContinuous(ItemStack stack) {
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void applyUseActionTransform(ItemStack stack, AbstractClientPlayerEntity player, MatrixStack matrices, float tickDelta, float swingProgress) {
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20));
        matrices.translate(0, -.3, 0);
    }
}
