package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.worldgen.AffinityWorldgen;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CultivationStaffItem extends StaffItem {

    public CultivationStaffItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN));
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock) {
        if (clickedBlock == null) return TypedActionResult.pass(stack);

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.getRegistryManager().get(RegistryKeys.PLACED_FEATURE).getOrEmpty(AffinityWorldgen.CULTIVATION_STAFF_FLOWER_PATCH).ifPresent(feature -> {
                feature.generateUnregistered(serverWorld, serverWorld.getChunkManager().getChunkGenerator(), world.random, clickedBlock);
            });

            serverWorld.getRegistryManager().get(RegistryKeys.PLACED_FEATURE).getOrEmpty(AffinityWorldgen.CULTIVATION_STAFF_GRASS_PATCH).ifPresent(feature -> {
                feature.generateUnregistered(serverWorld, serverWorld.getChunkManager().getChunkGenerator(), world.random, clickedBlock);
            });
        } else {
            spawnParticles(world, clickedBlock);
        }

        player.getItemCooldownManager().set(this, 20);
        return TypedActionResult.success(stack);
    }

    @Environment(EnvType.CLIENT)
    protected void spawnParticles(World world, BlockPos pos) {
        ClientParticles.setParticleCount(30);
        ClientParticles.spawnPrecise(ParticleTypes.HAPPY_VILLAGER, world, Vec3d.ofCenter(pos.up()), 5, 1, 5);

        ClientParticles.randomizeVelocity(.15);
        ClientParticles.setParticleCount(10);
        ClientParticles.spawnPrecise(ParticleTypes.FIREWORK, world, Vec3d.ofCenter(pos.up()), 5, 1, 5);
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return 1f;
    }
}
