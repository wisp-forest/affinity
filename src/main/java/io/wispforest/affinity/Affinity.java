package io.wispforest.affinity;

import com.google.common.collect.ImmutableSet;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.item.AffinityItemGroup;
import io.wispforest.affinity.item.EchoShardExtension;
import io.wispforest.affinity.misc.AffinityDebugCommands;
import io.wispforest.affinity.misc.ClumpDirectionLootCondition;
import io.wispforest.affinity.mixin.access.BlockEntityTypeAccessor;
import io.wispforest.affinity.mixin.access.SignTypeInvoker;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.*;
import io.wispforest.affinity.worldgen.AffinityStructures;
import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Affinity implements ModInitializer {

    public static final String MOD_ID = "affinity";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final OwoItemGroup AFFINITY_GROUP = AffinityItemGroup.GROUP;

    public static final Color AETHUM_FLUX_COLOR = Color.ofRgb(0x6A67CE);

    public static final BlockApiLookup<AethumNetworkMember, Void> AETHUM_MEMBER = BlockApiLookup.get(id("aethum_member"), AethumNetworkMember.class, Void.class);
    public static final BlockApiLookup<AethumNetworkNode, Void> AETHUM_NODE = BlockApiLookup.get(id("aethum_node"), AethumNetworkNode.class, Void.class);

    @Override
    public void onInitialize() {
        FieldRegistrationHandler.register(AffinityBlocks.class, MOD_ID, false);
        FieldRegistrationHandler.register(AffinityBlocks.Entities.class, MOD_ID, false);

        FieldRegistrationHandler.register(AffinityItems.class, MOD_ID, false);
        FieldRegistrationHandler.register(AffinityEnchantments.class, MOD_ID, false);

        FieldRegistrationHandler.register(AffinityEntities.class, MOD_ID, false);
        FieldRegistrationHandler.register(AffinityParticleTypes.class, MOD_ID, false);
        FieldRegistrationHandler.register(AffinitySoundEvents.class, MOD_ID, false);

        FieldRegistrationHandler.register(AffinityRecipeTypes.class, MOD_ID, false);
        FieldRegistrationHandler.register(AffinityRecipeTypes.Serializers.class, MOD_ID, false);
        FieldRegistrationHandler.register(AffinityScreenHandlerTypes.class, MOD_ID, false);

        AffinityStatusEffects.register();
        AffinityNetwork.initialize();
        AffinityParticleSystems.initialize();
        AffinityPoiTypes.initialize();

        EchoShardExtension.apply();

        Registry.register(Registries.LOOT_CONDITION_TYPE, Affinity.id("clump_direction"), ClumpDirectionLootCondition.TYPE);

        AffinityStructures.register();

        // Inject our sign block into the BE type
        var signBlocks = ((BlockEntityTypeAccessor) BlockEntityType.SIGN).affinity$getBlocks();
        signBlocks = ImmutableSet.<Block>builder().addAll(signBlocks).add(AffinityBlocks.AZALEA_SIGN, AffinityBlocks.AZALEA_WALL_SIGN).build();
        ((BlockEntityTypeAccessor) BlockEntityType.SIGN).affinity$setBlocks(signBlocks);

        SignTypeInvoker.affinity$invokeRegister(AffinityBlocks.AZALEA_SIGN_TYPE);

        AFFINITY_GROUP.initialize();

        if (!Owo.DEBUG) return;
        AffinityDebugCommands.register();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static String idPlain(String path) {
        return id(path).toString();
    }

}
