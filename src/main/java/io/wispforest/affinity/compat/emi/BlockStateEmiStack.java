package io.wispforest.affinity.compat.emi;

import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiStack;
import io.wispforest.owo.ui.component.BlockComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public class BlockStateEmiStack extends EmiStack {

    public static boolean renderLarge = false;

    private final BlockState state;
    private final Item item;

    private BlockComponent renderComponent;

    public BlockStateEmiStack(BlockState state) {
        this.state = state;
        this.item = state.getBlock().asItem();
    }

    @Override
    public EmiStack copy() {
        return new BlockStateEmiStack(this.state);
    }

    @Override
    public void render(DrawContext draw, int x, int y, float delta, int flags) {
        if (this.renderComponent == null) {
            this.renderComponent = Components.block(this.state);
        }

        var size = renderLarge ? 32 : 16;

        this.renderComponent.sizing(Sizing.fixed(size));
        this.renderComponent.inflate(Size.of(size, size));
        this.renderComponent.mount(null, x, y);
        this.renderComponent.draw(OwoUIDrawContext.of(draw), -69, -69, delta, delta);
    }

    @Override
    public boolean isEmpty() {
        return this.state.isAir();
    }

    @Override
    public ComponentChanges getComponentChanges() {
        return ComponentChanges.EMPTY;
    }

    @Override
    public Object getKey() {
        return this.item;
    }

    @Override
    public Identifier getId() {
        return Registries.ITEM.getId(this.item);
    }

    @Override
    public List<Text> getTooltipText() {
        return List.of(this.state.getBlock().getName());
    }

    @Override
    public List<TooltipComponent> getTooltip() {
        var tooltip = getTooltipText().stream().map(EmiTooltipComponents::of).collect(Collectors.toList());
        if (this.amount > 1) {
            tooltip.add(EmiTooltipComponents.getAmount(this));
        }

        EmiTooltipComponents.appendModName(
                tooltip,
                Registries.BLOCK.getId(this.state.getBlock()).getNamespace()
        );

        tooltip.addAll(super.getTooltip());
        return tooltip;
    }

    @Override
    public Text getName() {
        return this.state.getBlock().getName();
    }
}
