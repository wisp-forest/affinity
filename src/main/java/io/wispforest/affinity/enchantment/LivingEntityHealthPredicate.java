package io.wispforest.affinity.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.Affinity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record LivingEntityHealthPredicate(float threshold) implements EntitySubPredicate {

    public static final MapCodec<LivingEntityHealthPredicate> MAP_CODEC = Codec.FLOAT.fieldOf("threshold").xmap(LivingEntityHealthPredicate::new, LivingEntityHealthPredicate::threshold);

    @Override
    public MapCodec<? extends EntitySubPredicate> getCodec() {
        return MAP_CODEC;
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        return entity instanceof LivingEntity living && living.getHealth() >= living.getMaxHealth() * this.threshold;
    }

    public static void register() {
        Registry.register(Registries.ENTITY_SUB_PREDICATE_TYPE, Affinity.id("living_entity_health"), MAP_CODEC);
    }
}
