package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.components.AffinityComponents;
import io.wispforest.affinity.mixin.access.FilledMapItemInvoker;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.minecraft.block.MapColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AethumMapItem extends FilledMapItem {

    private static final byte[] COLORS = {
            MapColor.BLUE.getRenderColorByte(MapColor.Brightness.LOWEST),
            MapColor.BLUE.getRenderColorByte(MapColor.Brightness.LOW),
            MapColor.BLUE.getRenderColorByte(MapColor.Brightness.NORMAL),
            MapColor.BLUE.getRenderColorByte(MapColor.Brightness.HIGH),
            MapColor.LIGHT_BLUE.getRenderColorByte(MapColor.Brightness.LOWEST),
            MapColor.LIGHT_BLUE.getRenderColorByte(MapColor.Brightness.LOW),
            MapColor.LIGHT_BLUE.getRenderColorByte(MapColor.Brightness.NORMAL),
            MapColor.LIGHT_BLUE.getRenderColorByte(MapColor.Brightness.HIGH)
    };

    public AethumMapItem() {
        super(new OwoItemSettings());
    }

    @Override
    public void updateColors(World world, Entity entity, MapState state) {
        if (world.getTime() % 200 != 0) return;

        final byte[] COLORS = {
                MapColor.BLACK.getRenderColorByte(MapColor.Brightness.LOW),
                MapColor.BLACK.getRenderColorByte(MapColor.Brightness.NORMAL),
                MapColor.BLACK.getRenderColorByte(MapColor.Brightness.HIGH),
                MapColor.GRAY.getRenderColorByte(MapColor.Brightness.LOWEST),
                MapColor.GRAY.getRenderColorByte(MapColor.Brightness.LOW),
                MapColor.GRAY.getRenderColorByte(MapColor.Brightness.NORMAL),
                MapColor.LIGHT_GRAY.getRenderColorByte(MapColor.Brightness.LOWEST),
                MapColor.LIGHT_GRAY.getRenderColorByte(MapColor.Brightness.LOW),
                MapColor.LIGHT_GRAY.getRenderColorByte(MapColor.Brightness.NORMAL),
                MapColor.LIGHT_GRAY.getRenderColorByte(MapColor.Brightness.HIGH),
                MapColor.WHITE.getRenderColorByte(MapColor.Brightness.LOW),
                MapColor.WHITE.getRenderColorByte(MapColor.Brightness.NORMAL),
                MapColor.WHITE.getRenderColorByte(MapColor.Brightness.HIGH)
        };

//        int idx = 0;
//        for (var color : COLORS) {
//            for (int i = 0; i < 16; i++) {
//                state.putColor(state.centerX + idx, state.centerZ + i, color);
//            }
//            idx++;
//        }

        for (int x = 0; x < 128; x += 2) {
            for (int z = 0; z < 128; z += 2) {
                final var pos = new BlockPos(state.centerX - 64 + x, 0, state.centerZ - 64 + z);
                var aethum = AffinityComponents.CHUNK_AETHUM.get(world.getChunk(pos)).aethumAt(pos);

                for (int i = 0; i < 4; i++) {
                    state.putColor(x + i % 2, z + i / 2, COLORS[(int) Math.round((aethum - 60) / 25 * 12)]);
                }
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        final var playerStack = user.getStackInHand(hand);

        var stateId = getMapId(playerStack);
        if (stateId != null) return TypedActionResult.pass(playerStack);

        FilledMapItemInvoker.affinity$createMapState(playerStack, world, user.getBlockX(), user.getBlockZ(), 0,
                true, false, world.getRegistryKey());

        return TypedActionResult.success(playerStack);
    }
}
