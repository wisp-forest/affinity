package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.MangroveBasketBlockEntity;
import io.wispforest.affinity.misc.PreMangroveBasketCallback;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MangroveBasketItem extends BlockItem {
    public static final TagKey<Block> MANGROVE_BASKET_BLACKLIST = TagKey.of(RegistryKeys.BLOCK, Affinity.id("mangrove_basket_blacklist"));

    public MangroveBasketItem(Block block, OwoItemSettings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (BlockItem.getBlockEntityNbt(context.getStack()) != null) {
            return this.place(new ItemPlacementContext(context));
        } else {
            return this.place(new BasketPlacementContext(context));
        }
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(ItemPlacementContext context) {
        if (BlockItem.getBlockEntityNbt(context.getStack()) == null) {
            var currentState = context.getWorld().getBlockState(context.getBlockPos());

            if (currentState.isIn(MANGROVE_BASKET_BLACKLIST))
                return null;

            if (context.getWorld().getBlockEntity(context.getBlockPos()) == null)
                return null;
        }

        return super.getPlacementState(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        var nbt = BlockItem.getBlockEntityNbt(stack);

        if (nbt != null) {
            var state = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), nbt.getCompound("ContainedState"));

            tooltip.add(Text.literal("• ")
                    .formatted(Formatting.DARK_GRAY)
                    .append(state.getBlock().getName()));
        }
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        var nbt = BlockItem.getBlockEntityNbt(context.getStack());

        if (nbt != null) {
            return super.place(context, state);
        }

        BlockState old = context.getWorld().getBlockState(context.getBlockPos());
        BlockEntity oldBlockEntity = context.getWorld().getBlockEntity(context.getBlockPos());

        if (!PreMangroveBasketCallback.EVENT.invoker().preMangroveBasket(context.getWorld(), context.getBlockPos(), old, oldBlockEntity)) {
            return false;
        }

        context.getWorld().removeBlockEntity(context.getBlockPos());

        if (!context.getWorld().setBlockState(context.getBlockPos(), state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD))
            return false;

        if (context.getWorld().getBlockEntity(context.getBlockPos()) instanceof MangroveBasketBlockEntity newBlockEntity) {
            newBlockEntity.init(old, oldBlockEntity);
            return true;
        } else {
            return false;
        }
    }

    private static class BasketPlacementContext extends ItemPlacementContext {
        public BasketPlacementContext(ItemUsageContext context) {
            super(context);

            this.canReplaceExisting = true;
        }
    }
}
