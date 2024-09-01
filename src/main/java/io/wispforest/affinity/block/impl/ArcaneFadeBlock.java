package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.misc.ArcaneFadeFluid;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArcaneFadeBlock extends FluidBlock {

    private static final Map<Block, TagKey<Block>> BLOCK_GROUPS = new HashMap<>();
    private static final Map<Item, TagKey<Item>> ITEM_GROUPS = new HashMap<>();

    public ArcaneFadeBlock() {
        super(AffinityBlocks.Fluids.ARCANE_FADE, FabricBlockSettings.copyOf(Blocks.WATER));
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);

        for (var direction : Direction.values()) {
            var neighborPos = pos.offset(direction);
            var neighborState = world.getBlockState(neighborPos);

            this.bleachNeighbor(world, neighborState, neighborPos);
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        this.bleachNeighbor(world, neighborState, neighborPos);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public static void forEachGroup(GroupConsumer consumer) {
        ITEM_GROUPS.forEach((item, tag) -> {
            var entries = new ArrayList<ItemConvertible>();
            for (var mappedItem : Registries.ITEM.iterateEntries(tag)) {
                entries.add(mappedItem.value());
            }

            consumer.accept(tag.id(), item, entries);
        });

        BLOCK_GROUPS.forEach((block, tag) -> {
            var entries = new ArrayList<ItemConvertible>();
            for (var mappedItem : Registries.BLOCK.iterateEntries(tag)) {
                entries.add(mappedItem.value());
            }

            consumer.accept(tag.id(), block, entries);
        });

    }

    private void bleachNeighbor(WorldAccess world, BlockState neighborState, BlockPos neighborPos) {
        BLOCK_GROUPS.forEach((block, tag) -> {
            if (!neighborState.isIn(tag)) return;

            world.breakBlock(neighborPos, false);
            world.setBlockState(neighborPos, block.getDefaultState(), Block.NOTIFY_LISTENERS);
        });
    }

    private static @Nullable Item mapColoredItem(Item in) {
        for (var entry : ITEM_GROUPS.entrySet()) {
            if (!in.getRegistryEntry().isIn(entry.getValue())) continue;
            return entry.getKey();
        }

        if (!(in instanceof BlockItem blockItem)) return null;
        var block = blockItem.getBlock();

        for (var entry : BLOCK_GROUPS.entrySet()) {
            if (!block.getRegistryEntry().isIn(entry.getValue())) continue;
            return entry.getKey().asItem();
        }

        return null;
    }

    private static <T> void reloadGroups(Registry<T> registry, Map<T, TagKey<T>> storage) {
        storage.clear();

        registry.streamTags().forEach(tag -> {
            if (!tag.id().getPath().startsWith("arcane_fade_groups/")) return;

            registry.getOrEmpty(new Identifier(tag.id().getNamespace(), tag.id().getPath().replaceFirst("arcane_fade_groups/", "")))
                    .ifPresent(block -> storage.put(block, tag));
        });
    }

    public interface GroupConsumer {
        void accept(Identifier id, ItemConvertible fadedVariant, List<ItemConvertible> inputVariants);
    }

    static {
        ArcaneFadeFluid.ENTITY_TOUCH_EVENT.register(entity -> {
            if (!(entity instanceof ItemEntity item)) return;

            var mappedItem = mapColoredItem(item.getStack().getItem());
            if (mappedItem != null) {
                var newStack = mappedItem.getDefaultStack();
                newStack.setCount(item.getStack().getCount());
                newStack.setNbt(item.getStack().getNbt());

                item.setStack(newStack);
            } else if (item.getStack().getItem() instanceof DyeableItem dyeable) {
                var newStack = item.getStack().copy();
                dyeable.removeColor(newStack);

                item.setStack(newStack);
            } else {
                return;
            }

            AffinityParticleSystems.ARCANE_FADE_BLEACH_SHEEP.spawn(item.getWorld(), item.getPos().add(0, .5f, 0), .25f);
        });

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (client) return;

            reloadGroups(Registries.BLOCK, BLOCK_GROUPS);
            reloadGroups(Registries.ITEM, ITEM_GROUPS);
        });
    }
}
