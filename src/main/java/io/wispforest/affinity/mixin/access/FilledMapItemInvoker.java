package io.wispforest.affinity.mixin.access;

import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FilledMapItem.class)
public interface FilledMapItemInvoker {

    @Invoker("allocateMapId")
    static MapIdComponent affinity$AllocateMapId(World world, int x, int z, int scale, boolean showIcons, boolean unlimitedTracking, RegistryKey<World> dimension) {
        throw new AssertionError("what");
    }

}
