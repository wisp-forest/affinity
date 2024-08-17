package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.blockentity.impl.AzaleaChestBlockEntity;
import io.wispforest.affinity.misc.screenhandler.LargeAzaleaChestScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class AzaleaChestBlock extends ChestBlock {

    private static final DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<NamedScreenHandlerFactory>> SCREEN_RETRIEVER = new DoubleBlockProperties.PropertyRetriever<>() {
        public Optional<NamedScreenHandlerFactory> getFromBoth(ChestBlockEntity chest, ChestBlockEntity otherChest) {
            final Inventory inventory = new DoubleInventory(chest, otherChest);
            return Optional.of(new NamedScreenHandlerFactory() {
                @Nullable
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                    if (chest.checkUnlocked(playerEntity) && otherChest.checkUnlocked(playerEntity)) {
                        chest.generateLoot(playerInventory.player);
                        otherChest.generateLoot(playerInventory.player);
                        return new LargeAzaleaChestScreenHandler(syncId, playerInventory, inventory);
                    } else {
                        return null;
                    }
                }

                @Override
                public Text getDisplayName() {
                    if (chest.hasCustomName()) {
                        return chest.getDisplayName();
                    } else {
                        return otherChest.hasCustomName() ? otherChest.getDisplayName() : Text.translatable("container.affinity.azalea_chest_double");
                    }
                }
            });
        }

        public Optional<NamedScreenHandlerFactory> getFrom(ChestBlockEntity chestBlockEntity) {
            return Optional.of(chestBlockEntity);
        }

        public Optional<NamedScreenHandlerFactory> getFallback() {
            return Optional.empty();
        }
    };

    public AzaleaChestBlock(Settings settings, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier) {
        super(settings, supplier);
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return this.getBlockEntitySource(state, world, pos, false).apply(SCREEN_RETRIEVER).orElse(null);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AzaleaChestBlockEntity(pos, state);
    }
}
