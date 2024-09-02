package io.wispforest.affinity.mixin;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {

    @Unique private static final Set<String> STAFFS = Set.of(
        "affinity:collection_staff",
        "affinity:nimble_staff",
        "affinity:time_staff",
        "affinity:kinesis_staff",
        "affinity:astrokinesis_staff",
        "affinity:cultivation_staff",
        "affinity:salvo_staff",
        "affinity:swivel_staff",
        "affinity:aethum_fire_extinguisher"
    );

    @Unique private static final Set<String> SHARDS = Set.of(
        "minecraft:amethyst_shard",
        "affinity:mildly_attuned_amethyst_shard",
        "affinity:fairly_attuned_amethyst_shard",
        "affinity:greatly_attuned_amethyst_shard"
    );

    @Unique private static final Set<String> POTIONS = Set.of(
        "minecraft:potion",
        "minecraft:splash_potion",
        "minecraft:lingering_potion",
        "minecraft:tipped_arrow"
    );

    @Inject(method = "fixStack", at = @At("TAIL"))
    private static void fixStaffBundles(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, CallbackInfo ci) {
        if (data.itemMatches(STAFFS)) {
            data.moveToComponent("bundled_staffs", "affinity:bundled_staffs");
        }

        if (data.itemMatches(SHARDS)) {
            data.moveToComponent("Health", "affinity:attuned_shard_health");
        }

        if (data.itemEquals("affinity:carbon_copy")) {
            var recipe = data.getAndRemove("Recipe");
            var result = data.getAndRemove("Result");

            if (recipe.result().isPresent() && result.result().isPresent()) {
                data.setComponent(
                    "affinity:carbon_copy_recipe",
                    dynamic.createMap(Map.of(
                        dynamic.createString("recipe_id"), recipe.result().get(),
                        dynamic.createString("result"), result.result().get()
                    ))
                );
            }
        }

        if (data.itemEquals("affinity:nimble_staff")) {
            data.moveToComponent("Direction", "affinity:nimble_staff_direction");

            var shardTarget = data.getAndRemove("EchoShardTarget");
            if (shardTarget.result().isPresent()) {
                data.setComponent("affinity:nimble_staff_echo_shard_target", blockPosToLong(shardTarget.result().get()));
            }
        }

        if (data.itemEquals("minecraft:echo_shard")) {
            moveBoundLocation(
                data,
                data.getAndRemove("Bound"),
                data.getAndRemove("World"),
                data.getAndRemove("Pos")
            );
        }

        if (data.itemMatches(POTIONS)) {
            var extraData = data.getAndRemove("ExtraPotionData").result();
            extraData.ifPresent(value -> {
                moveBoundLocation(
                    data,
                    value.get("Bound"),
                    value.get("World"),
                    value.get("Pos")
                );

                value.get("ExtendDurationBy").result().ifPresent(durationExtension -> {
                    data.setComponent("affinity:extend_potion_duration_by", dynamic.createFloat(durationExtension.asFloat(0)));
                });
            });
        }
    }

    @Unique
    private static void moveBoundLocation(ItemStackComponentizationFix.StackData data, OptionalDynamic<?> bound, OptionalDynamic<?> world, OptionalDynamic<?> pos) {
        var boundResult = bound.result();
        var worldResult = world.result();
        var posResult = pos.result();

        if (boundResult.isPresent() && boundResult.get().asBoolean(false) && worldResult.isPresent() && posResult.isPresent()) {
            var dynamic = boundResult.get();

            data.setComponent(
                "affinity:bound_location",
                dynamic.createMap(Map.of(
                    dynamic.createString("pos"), blockPosToLong(posResult.get()),
                    dynamic.createString("world"), worldResult.get()
                ))
            );
        }
    }

    @Unique
    private static <T> Dynamic<T> blockPosToLong(Dynamic<T> blockPosArray) {
        var coordinates = blockPosArray.asIntStream().toArray();
        var blockPos = new BlockPos(coordinates[0], coordinates[1], coordinates[2]);

        return blockPosArray.createLong(blockPos.asLong());
    }
}
