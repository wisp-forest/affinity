package io.wispforest.affinity.component;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.Component;

public class ExtraArrowDamageComponent implements Component {

    private static final KeyedEndec<Integer> DAMAGE_KEY = Endec.INT.keyed("Damage", 0);
    public int extraDamage = 0;

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        this.extraDamage = tag.get(DAMAGE_KEY);
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.put(DAMAGE_KEY, this.extraDamage);
    }
}
