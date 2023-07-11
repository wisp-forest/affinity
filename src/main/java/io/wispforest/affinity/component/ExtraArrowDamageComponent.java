package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.nbt.NbtCompound;

public class ExtraArrowDamageComponent implements Component {

    private static final NbtKey<Integer> DAMAGE_KEY = new NbtKey<>("Damage", NbtKey.Type.INT);
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
