package io.wispforest.affinity.block.template;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AethumNetworkMemberBlock extends BlockWithEntity {

    public static final Text CONSUMER_TOOLTIP = Text.translatable("text.affinity.aethum_flux_consumer");
    public static final Text GENERATOR_TOOLTIP = Text.translatable("text.affinity.aethum_flux_generator");
    public static final Text NODE_TOOLTIP = Text.translatable("text.affinity.aethum_flux_node");
    public static final Text STORAGE_TOOLTIP = Text.translatable("text.affinity.aethum_flux_storage");

    private final Text tooltipText;

    protected AethumNetworkMemberBlock(Settings settings, Text tooltipText) {
        super(settings);
        this.tooltipText = tooltipText;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof AethumNetworkMemberBlockEntity member) member.onBroken();
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        tooltip.add(this.tooltipText);
    }
}
