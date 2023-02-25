package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.AethumAcquisitionCache;
import io.wispforest.affinity.mixin.access.MapStateAccessor;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.block.MapColor;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RealizedAethumMapItem extends FilledMapItem {

    private static final NbtKey<Boolean> LOCKED = new NbtKey<>("Locked", NbtKey.Type.BOOLEAN);

    private static final byte[] COLORS = {
            MapColor.BLACK.getRenderColorByte(MapColor.Brightness.LOWEST),
            MapColor.BLACK.getRenderColorByte(MapColor.Brightness.LOW),
            MapColor.BLACK.getRenderColorByte(MapColor.Brightness.NORMAL),
            MapColor.BLACK.getRenderColorByte(MapColor.Brightness.HIGH),
            MapColor.GRAY.getRenderColorByte(MapColor.Brightness.LOWEST),
            MapColor.GRAY.getRenderColorByte(MapColor.Brightness.LOW),
            MapColor.GRAY.getRenderColorByte(MapColor.Brightness.NORMAL),
            MapColor.STONE_GRAY.getRenderColorByte(MapColor.Brightness.LOW),
            MapColor.STONE_GRAY.getRenderColorByte(MapColor.Brightness.NORMAL),
            MapColor.STONE_GRAY.getRenderColorByte(MapColor.Brightness.HIGH),
            MapColor.LIGHT_GRAY.getRenderColorByte(MapColor.Brightness.NORMAL),
            MapColor.IRON_GRAY.getRenderColorByte(MapColor.Brightness.NORMAL),
            MapColor.LIGHT_GRAY.getRenderColorByte(MapColor.Brightness.HIGH),
            MapColor.WHITE.getRenderColorByte(MapColor.Brightness.LOW),
            MapColor.WHITE.getRenderColorByte(MapColor.Brightness.NORMAL),
            MapColor.WHITE.getRenderColorByte(MapColor.Brightness.HIGH)
    };

    public RealizedAethumMapItem() {
        super(new OwoItemSettings().maxCount(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (stack.get(LOCKED)) return;
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public void updateColors(World world, Entity entity, MapState state) {
        final double entityX = entity.getX();
        final double entityZ = entity.getZ();
        final var withinMapArea = entityX >= state.centerX - 64
                && entityX <= state.centerX + 64
                && entityZ >= state.centerZ - 64
                && entityZ <= state.centerZ + 64;

        if (withinMapArea && world.getTime() % 50 != 0) return;
        if (!withinMapArea) realign(state, (int) entityX, (int) entityZ);

        final var cache = AethumAcquisitionCache.create(world,
                (state.centerX - 64) >> 4, (state.centerZ - 64) >> 4, 8 + 1);

        for (int x = 0; x < 128; x += 2) {
            for (int z = 0; z < 128; z += 2) {
                final var pos = new BlockPos(state.centerX - 64 + x, 0, state.centerZ - 64 + z);
                final var component = cache.getComponentFrom(pos.getX() >> 4, pos.getZ() >> 4);

                final byte color = component == null ? COLORS[0] :
                        COLORS[MathHelper.clamp(
                                (int) Math.round((component.fastAethumAt(cache, pos.getX(), pos.getZ()) - 30) / 50 * 15),
                                0,
                                15
                        )];

                for (int i = 0; i < 4; i++) {
                    state.putColor(x + i % 2, z + i / 2, color);
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (stack.get(LOCKED)) {
            tooltip.add(Text.translatable(this.getTranslationKey() + ".tooltip.locked"));
        } else {
            tooltip.add(Text.translatable(this.getTranslationKey() + ".tooltip.unlocked"));
        }
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        stack.mutate(LOCKED, locked -> !locked);
        return true;
    }

    public static void realign(MapState state, int x, int z) {
        final var stateAccess = (MapStateAccessor) state;
        stateAccess.affinity$setCenterX(makeCenter(x));
        stateAccess.affinity$setCenterZ(makeCenter(z));
    }

    public static int makeCenter(int coordinate) {
        int sideLength = 128;
        int magic = MathHelper.floor((coordinate + 64d) / sideLength);
        return magic * sideLength + sideLength / 2 - 64;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }
}
