package io.wispforest.affinity.mixin.access;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("dead")
    void affinity$setDead(boolean dead);

    @Invoker("getEquipmentChanges")
    Map<EquipmentSlot, ItemStack> affinity$getEquipmentChanges();

    @Invoker("sendEquipmentChanges")
    void affinity$sendEquipmentChanges();

    @Accessor("lastAttackedTicks")
    int affinity$getLastAttackedTicks();

    @Accessor("lastAttackedTicks")
    void affinity$setLastAttackedTicks(int lastAttackedTicks);
}
