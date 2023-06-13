package io.wispforest.affinity.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DamageTypeKey {

    private final RegistryKey<DamageType> key;
    private final Attribution attributionType;

    public DamageTypeKey(Identifier id, Attribution attributionType) {
        this.key = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id);
        this.attributionType = attributionType;
    }

    public DamageTypeKey(Identifier id) {
        this(id, Attribution.ATTACKER_THEN_ADVERSARY);
    }

    public DamageSource source(World world) {
        return new Source(this.get(world), null, null);
    }

    public DamageSource source(Entity source) {
        return new Source(this.get(source.getWorld()), source, source);
    }

    public DamageSource source(Entity source, Entity attacker) {
        return new Source(this.get(source.getWorld()), source, attacker);
    }

    public RegistryEntry<DamageType> get(World world) {
        return world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(this.key);
    }

    public class Source extends DamageSource {

        public Source(RegistryEntry<DamageType> type, @Nullable Entity source, @Nullable Entity attacker) {
            super(type, source, attacker);
        }

        @Override
        public Text getDeathMessage(LivingEntity killed) {
            var key = "death.attack." + this.getType().msgId();

            var killer = switch (DamageTypeKey.this.attributionType) {
                case NEVER_ATTRIBUTE -> null;
                case ATTACKER_THEN_ADVERSARY -> this.getAttacker() != null ? this.getAttacker() : killed.getPrimeAdversary();
            };

            if (killer != null) {
                return Text.translatable(key + ".player", killed.getDisplayName(), killer.getDisplayName());
            } else {
                return Text.translatable(key, killed.getDisplayName());
            }
        }
    }

    public enum Attribution {
        NEVER_ATTRIBUTE, ATTACKER_THEN_ADVERSARY;
    }
}
