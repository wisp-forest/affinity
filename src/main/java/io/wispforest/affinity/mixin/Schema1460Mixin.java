package io.wispforest.affinity.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.Schema1460;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(Schema1460.class)
public abstract class Schema1460Mixin {

    @Shadow
    protected static void method_5273(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {}

    @Inject(method = "registerBlockEntities", at = @At("TAIL"))
    private void injectAffinityBlockEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
        var map = cir.getReturnValue();
        method_5273(schema, map, "affinity:brewing_cauldron");
        method_5273(schema, map, "affinity:azalea_chest");

        schema.register(
            map,
            "affinity:aethum_flux_node",
            () -> DSL.optionalFields(
                "Shard", TypeReferences.ITEM_STACK.in(schema),
                "OuterShards", DSL.list(TypeReferences.ITEM_STACK.in(schema))
            )
        );

        schema.register(map, "affinity:aethum_flux_cache", () -> DSL.optionalFields("Shard", TypeReferences.ITEM_STACK.in(schema)));

        schema.register(map, "affinity:ethereal_aethum_flux_node", () -> DSL.optionalFields("shard", TypeReferences.ITEM_STACK.in(schema)));

        schema.register(map, "affinity:staff_pedestal", () -> DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema)));

        schema.register(map, "affinity:graviton_transducer", () -> DSL.optionalFields("Shard", TypeReferences.ITEM_STACK.in(schema)));

        schema.register(map, "affinity:mangrove_basket", () -> DSL.optionalFields("ContainedBlockEntity", TypeReferences.BLOCK_ENTITY.in(schema)));

        schema.register(
            map,
            "affinity:item_transfer_node",
            () -> DSL.optionalFields(
                "FilterStack", TypeReferences.ITEM_STACK.in(schema),
                "Entries", DSL.list(DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema)))
            )
        );
    }

}
