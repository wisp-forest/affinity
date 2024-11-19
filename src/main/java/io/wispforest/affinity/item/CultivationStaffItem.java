package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.affinity.worldgen.AffinityWorldgen;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CultivationStaffItem extends StaffItem {

    public static final ComponentType<Unit> SUPER_FORAGING_MODE = Affinity.unitComponent("cultivation_staff_super_foraging_mode");

    private static final TagKey<Block> BLOCKS_TO_CULTIVATE = TagKey.of(RegistryKeys.BLOCK, Affinity.id("blocks_to_cultivate"));
    private static final InquirableOutlineProvider.Outline AOE = InquirableOutlineProvider.Outline.symmetrical(4, 0, 4);

    public CultivationStaffItem() {
        super(AffinityItems.settings().maxCount(1));
    }

    @Override
    public boolean canBePlacedOnPedestal() {
        return true;
    }

    @Override
    public void pedestalTickServer(ServerWorld world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        var inventory = ItemStorage.SIDED.find(world, pos.add(0, pedestal.down(), 0), pedestal.facing().getOpposite());
        if (inventory == null) return;

        if (pedestal.getItem().contains(SUPER_FORAGING_MODE) && pedestal.hasFlux(150)) {
            pedestal.consumeFlux(150);

            for (var cropPos : BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 0, 4))) {
                if (world.random.nextFloat() > .005f) continue;

                var state = world.getBlockState(cropPos);
                if (!(state.getBlock() instanceof Fertilizable crop)) continue;

                if (crop.isFertilizable(world, pos, state) && crop.canGrow(world, world.random, cropPos, state)) {
                    crop.grow(world, world.random, cropPos, state);
                }

                world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, cropPos, 0);
            }
        }

        final int sideLength = 9;
        final int index = pedestal.time() % MathHelper.square(sideLength);

        var cropPos = pos.add(-4 + index / sideLength, 0, -4 + index % sideLength);
        var state = world.getBlockState(cropPos);

        var isCrop = state.getBlock() instanceof CropBlock;
        if ((!isCrop || !((CropBlock) state.getBlock()).isMature(state)) && !state.isIn(BLOCKS_TO_CULTIVATE)) return;

        if (!pedestal.hasFlux(50)) return;
        pedestal.consumeFlux(50);

        var drops = Block.getDroppedStacks(state, world, cropPos, world.getBlockEntity(cropPos));
        boolean decrementSeeds = isCrop;

        try (var transaction = Transaction.openOuter()) {
            for (var drop : drops) {
                if (decrementSeeds && drop.getItem() == state.getBlock().asItem()) {
                    drop.decrement(1);
                    decrementSeeds = false;
                }

                if (drop.isEmpty()) continue;

                long inserted = inventory.insert(ItemVariant.of(drop), drop.getCount(), transaction);
                if (inserted != drop.getCount()) {
                    drop.setCount((int) (drop.getCount() - inserted));
                    ItemScatterer.spawn(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, drop);
                }
            }

            transaction.commit();
        }

        world.breakBlock(cropPos, false);
        if (state.getBlock() instanceof CropBlock crop) world.setBlockState(cropPos, crop.withAge(0), Block.NOTIFY_LISTENERS);
    }

    @Override
    public void appendTooltipEntries(World world, BlockPos pos, StaffPedestalBlockEntity pedestal, List<InWorldTooltipProvider.Entry> entries) {
        entries.add(InWorldTooltipProvider.Entry.text(
                pedestal.getItem().contains(SUPER_FORAGING_MODE) ? TextOps.withColor("✔", 0x28FFBF) : TextOps.withColor("❌ ", 0xEB1D36),
                Text.translatable("item.affinity.cultivation_staff.super_foraging_mode")
        ));
    }

    @Override
    public ActionResult onPedestalScrolled(World world, BlockPos pos, StaffPedestalBlockEntity pedestal, boolean direction) {
        if (pedestal.getItem().contains(SUPER_FORAGING_MODE)) {
            pedestal.getItem().remove(SUPER_FORAGING_MODE);
        } else {
            pedestal.getItem().set(SUPER_FORAGING_MODE, Unit.INSTANCE);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public InquirableOutlineProvider.Outline getAreaOfEffect(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        return AOE;
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

            WorldOps.playSound(world, clickedBlock.up(), AffinitySoundEvents.ITEM_CULTIVATION_STAFF_CULTIVATE, SoundCategory.PLAYERS, 1, 1.1f + world.random.nextFloat() * .4f);
        } else {
            spawnParticles(world, clickedBlock.up());
        }

        player.getItemCooldownManager().set(this, 20);
        return TypedActionResult.success(stack);
    }

    @Environment(EnvType.CLIENT)
    protected void spawnParticles(World world, BlockPos pos) {
        ClientParticles.setParticleCount(30);
        ClientParticles.spawnPrecise(ParticleTypes.HAPPY_VILLAGER, world, Vec3d.ofCenter(pos, .75f), 7.5, 1.5, 7.5);

        ClientParticles.randomizeVelocity(.15);
        ClientParticles.setParticleCount(20);
        ClientParticles.spawnPrecise(ParticleTypes.FIREWORK, world, Vec3d.ofCenter(pos, .75f), 7.5, 1.5, 7.5);
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return 1f;
    }
}
