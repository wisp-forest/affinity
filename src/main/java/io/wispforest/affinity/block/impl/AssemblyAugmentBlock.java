package io.wispforest.affinity.block.impl;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.block.template.BlockItemProvider;
import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.item.DirectInteractionHandler;
import io.wispforest.affinity.misc.screenhandler.AssemblyAugmentScreenHandler;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class AssemblyAugmentBlock extends BlockWithEntity implements BlockItemProvider {

    private static final VoxelShape SHAPE = Stream.of(
        Block.createCuboidShape(-1, 0, 3, 3, 3, 13),
        Block.createCuboidShape(-1, 0, -1, 17, 3, 3),
        Block.createCuboidShape(4, 2, -2, 12, 4, 0),
        Block.createCuboidShape(4, 2, 16, 12, 4, 18),
        Block.createCuboidShape(16, 2, 4, 18, 4, 12),
        Block.createCuboidShape(-2, 2, 4, 0, 4, 12),
        Block.createCuboidShape(13, -3, -1, 17, 0, 3),
        Block.createCuboidShape(13, -3, 13, 17, 0, 17),
        Block.createCuboidShape(-1, -3, 13, 3, 0, 17),
        Block.createCuboidShape(-1, -3, -1, 3, 0, 3),
        Block.createCuboidShape(-1, 0, 13, 17, 3, 17),
        Block.createCuboidShape(13, 0, 3, 17, 3, 13)
    ).reduce(VoxelShapes::union).get();

    public AssemblyAugmentBlock() {
        super(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).nonOpaque());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof AssemblyAugmentBlockEntity augment) {
            openScreen(player, augment);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isIn(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient || !world.getBlockState(pos.down()).isIn(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)) return;
        this.displayPlaceParticles(world, pos);
    }

    @Environment(EnvType.CLIENT)
    private void displayPlaceParticles(World world, BlockPos pos) {
        ClientParticles.setParticleCount(15);
        ClientParticles.spawnCenteredOnBlock(ParticleTypes.END_ROD, world, pos.down(), 1.5);

        ClientParticles.setParticleCount(15);
        ClientParticles.spawn(ArcaneTreetapBlock.PARTICLE, world, Vec3d.ofCenter(pos.down(), 1.15), 1.25);

        ClientParticles.setParticleCount(10);
        ClientParticles.randomizeVelocity(.15);
        ClientParticles.spawn(ParticleTypes.FIREWORK, world, Vec3d.ofCenter(pos.down(), 1.15), 1.25);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction != Direction.DOWN) return state;
        return world.getBlockState(neighborPos).isIn(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES) ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AssemblyAugmentBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, AffinityBlocks.Entities.ASSEMBLY_AUGMENT, TickedBlockEntity.ticker());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public Item createBlockItem(Block block, Item.Settings settings) {
        return new AssemblyAugmentItem(block, settings);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof AssemblyAugmentBlockEntity augment) {
            ItemScatterer.spawn(world, pos, augment.inventory());
            ItemScatterer.spawn(world, pos, augment.templateInventory());
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    private static void openScreen(PlayerEntity player, AssemblyAugmentBlockEntity augment) {
        player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.translatable("gui.affinity.augmented_crafting_table.title");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return AssemblyAugmentScreenHandler.server(syncId, inv, augment);
            }
        });
    }

    static {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSneaking() || !world.getBlockState(hitResult.getBlockPos()).isIn(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)) {
                return ActionResult.PASS;
            }
            if (!(world.getBlockEntity(hitResult.getBlockPos().up()) instanceof AssemblyAugmentBlockEntity augment)) {
                return ActionResult.PASS;
            }

            if (!world.isClient) {
                openScreen(player, augment);
            }

            return ActionResult.SUCCESS;
        });
    }

    private static class AssemblyAugmentItem extends BlockItem implements DirectInteractionHandler {

        public AssemblyAugmentItem(Block block, Settings settings) {
            super(block, settings);
        }

        @Override
        public Collection<Block> interactionOverrideCandidates(World world) {
            return Registries.BLOCK.getEntryList(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
                .map(entries -> entries.stream().map(RegistryEntry::value).toList())
                .orElse(List.of());
        }
    }
}
