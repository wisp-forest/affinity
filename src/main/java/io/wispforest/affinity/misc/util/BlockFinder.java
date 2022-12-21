package io.wispforest.affinity.misc.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

public class BlockFinder {

    public static Result findUncapped(World world, BlockPos origin, BiPredicate<BlockPos, BlockState> shouldExplore) {
        return findCapped(world, origin, shouldExplore, -1);
    }

    public static Result findCapped(World world, BlockPos origin, BiPredicate<BlockPos, BlockState> shouldExplore, int maxCount) {
        final var searchQueue = new ArrayDeque<BlockPos>();
        final var foundBlocks = new HashMap<BlockPos, BlockState>();

        searchQueue.add(origin);
        while (!searchQueue.isEmpty()) {
            final var next = searchQueue.poll();

            final var nextState = world.getBlockState(next);
            if (!shouldExplore.test(next, nextState)) continue;

            foundBlocks.put(next, nextState);
            if (maxCount > -1 && foundBlocks.size() >= maxCount) break;

            final var neighbors = BlockPos.iterate(
                    next.getX() - 1,
                    next.getY() - 1,
                    next.getZ() - 1,
                    next.getX() + 1,
                    next.getY() + 1,
                    next.getZ() + 1);

            for (var neighbor : neighbors) {
                if (searchQueue.contains(neighbor) || foundBlocks.containsKey(neighbor)) continue;

                if (shouldExplore.test(neighbor, world.getBlockState(neighbor))) {
                    searchQueue.add(neighbor.toImmutable());
                }
            }
        }

        return new Result(foundBlocks);
    }

    public record Result(Map<BlockPos, BlockState> results) implements Iterable<BlockPos> {
        public Map<Block, Integer> byCount() {
            final var map = new HashMap<Block, Integer>();

            results.forEach((blockPos, state) -> {
                map.put(state.getBlock(), map.getOrDefault(state.getBlock(), 0) + 1);
            });

            return map;
        }

        public boolean isEmpty() {
            return this.results.isEmpty();
        }

        @NotNull
        @Override
        public Iterator<BlockPos> iterator() {
            return this.results.keySet().iterator();
        }
    }

}
