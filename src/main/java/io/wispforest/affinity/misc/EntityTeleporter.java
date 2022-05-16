package io.wispforest.affinity.misc;

import io.wispforest.owo.ops.WorldOps;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public final class EntityTeleporter {
    private EntityTeleporter() {

    }

    @SuppressWarnings("unchecked")
    public static <E extends Entity> E teleport(E entity, ServerWorld to, Vec3d pos, float yaw, float pitch) {
        if (entity instanceof ServerPlayerEntity player) {
            WorldOps.teleportToWorld(player, to, pos, yaw, pitch);
        }

        if (entity.getEntityWorld() != to) {
            entity.detach();
            E newEntity = (E) entity.getType().create(to);

            newEntity.copyFrom(entity);
            newEntity.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
            newEntity.setHeadYaw(yaw);
            to.onDimensionChanged(newEntity);
            entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);

            return newEntity;
        } else {
            entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
            entity.setHeadYaw(yaw);

            return entity;
        }
    }
}
