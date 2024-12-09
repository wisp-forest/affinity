package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.MangroveBasketBlockEntity;
import io.wispforest.affinity.misc.callback.BeforeMangroveBasketCaptureCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MangroveBasketItem extends BlockItem {
    public static final TagKey<Block> MANGROVE_BASKET_BLACKLIST = TagKey.of(RegistryKeys.BLOCK, Affinity.id("mangrove_basket_blacklist"));

    public MangroveBasketItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getStack().get(DataComponentTypes.BLOCK_ENTITY_DATA) != null) {
            return this.place(new ItemPlacementContext(context));
        } else {
            return this.place(new BasketPlacementContext(context));
        }
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(ItemPlacementContext context) {
        if (context.getStack().get(DataComponentTypes.BLOCK_ENTITY_DATA) == null) {
            var currentState = context.getWorld().getBlockState(context.getBlockPos());

            if (currentState.isIn(MANGROVE_BASKET_BLACKLIST)) return null;
            if (context.getWorld().getBlockEntity(context.getBlockPos()) == null) return null;
        }

        return super.getPlacementState(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        var nbt = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        if (nbt == null) return;

        tooltip.add(nbt.getNbt().get(MangroveBasketBlockEntity.CONTAINED_STATE_KEY).getBlock().getName().formatted(Formatting.GRAY));
    } 

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        var nbt = context.getStack().get(DataComponentTypes.BLOCK_ENTITY_DATA);

        if (nbt != null) {
            return super.place(context, state);
        }

        var world = context.getWorld();
        var pos = context.getBlockPos();

        var oldState = world.getBlockState(pos);
        var oldBlockEntity = world.getBlockEntity(pos);

        var stateRef = new MutableObject<>(oldState);
        if (!BeforeMangroveBasketCaptureCallback.EVENT.invoker().beforeMangroveBasketCapture(world, pos, stateRef, oldBlockEntity)) {
            return false;
        }

        oldState = stateRef.getValue();
        world.removeBlockEntity(pos);

        if (!world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD)) {
            return false;
        }

        if (world.getBlockEntity(pos) instanceof MangroveBasketBlockEntity newBlockEntity) {
            newBlockEntity.init(oldState, oldBlockEntity);
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
