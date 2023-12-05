package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import net.minecraft.nbt.NbtCompound;

public class ExtraArrowDamageComponent implements Component {

    private static final KeyedEndec<Integer> DAMAGE_KEY = Endec.INT.keyed("Damage", 0);
    public int extraDamage = 0;

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.extraDamage = tag.get(DAMAGE_KEY);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.put(DAMAGE_KEY, this.extraDamage);
    }
}
