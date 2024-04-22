package io.wispforest.affinity.mixin.endec;

import io.wispforest.affinity.endec.nbt.NbtDeserializer;
import io.wispforest.affinity.endec.nbt.NbtSerializer;
import io.wispforest.endec.DataToken;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.util.MapCarrier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements MapCarrier {

    @Shadow
    private @Nullable NbtCompound nbt;

    @Shadow public abstract NbtCompound getOrCreateNbt();

    @Override
    public <T> T getWithErrors(@NotNull KeyedEndec<T> key) {
        if (!this.has(key)) return key.defaultValue();
        return key.endec().decodeFully(e -> NbtDeserializer.of(e).withTokens(DataToken.HUMAN_READABLE), this.nbt .get(key.key()));
    }

    @Override
    public <T> void put(@NotNull KeyedEndec<T> key, @NotNull T value) {
        this.getOrCreateNbt().put(key.key(), key.endec().encodeFully(() -> NbtSerializer.of().withTokens(DataToken.HUMAN_READABLE), value));
    }

    @Override
    public <T> void delete(@NotNull KeyedEndec<T> key) {
        if (this.nbt == null) return;
        this.nbt.remove(key.key());
    }

    @Override
    public <T> boolean has(@NotNull KeyedEndec<T> key) {
        return this.nbt != null && this.nbt.contains(key.key());
    }
}
