package io.wispforest.affinity.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.ServerTasks;
import io.wispforest.affinity.misc.callback.ClientDoItemUseCallback;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class SwivelStaffItem extends StaffItem implements DirectInteractionHandler {

    private static final Map<Block, SwivelProperties> SWIVEL_PROPERTIES = new HashMap<>();
    private static final float AETHUM_PER_ENTITY_SPIN = 1.5f;

    public static final ComponentType<String> SELECTED_PROPERTY = Affinity.component("selected_property", Endec.STRING);

    public SwivelStaffItem() {
        super(AffinityItems.settings().maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer().shouldCancelInteraction()) {
            var world = context.getWorld();
            var stack = context.getStack();

            var state = world.getBlockState(context.getBlockPos());
            if (!SWIVEL_PROPERTIES.containsKey(state.getBlock())) return ActionResult.PASS;

            var swivelProperties = SWIVEL_PROPERTIES.get(state.getBlock());
            var candidates = state.getProperties().stream()
                    .filter(swivelProperties::hasCycleFor)
                    .filter(property -> swivelProperties.nextValueFor(property, state).isPresent())
                    .toList();

            if (candidates.isEmpty()) return ActionResult.PASS;
            if (world.isClient) return ActionResult.SUCCESS;

            var selectedPropName = stack.getOrDefault(SELECTED_PROPERTY, "");
            var selectedProp = candidates.stream()
                    .filter(property -> property.getName().equals(selectedPropName))
                    .findFirst()
                    .orElse(candidates.get(0));

            stack.set(SELECTED_PROPERTY, Util.next(candidates, selectedProp).getName());
            world.playSound(null, context.getPlayer().getX(), context.getPlayer().getY(), context.getPlayer().getZ(), AffinitySoundEvents.ITEM_SWIVEL_STAFF_SELECT_PROPERTY, SoundCategory.PLAYERS);

            return ActionResult.SUCCESS;
        } else {
            return super.useOnBlock(context);
        }
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock) {
        if (clickedBlock == null) return TypedActionResult.pass(stack);

        var state = world.getBlockState(clickedBlock);
        if (!SWIVEL_PROPERTIES.containsKey(state.getBlock())) return TypedActionResult.pass(stack);

        var swivelProperties = SWIVEL_PROPERTIES.get(state.getBlock());
        var selectedPropName = stack.get(SELECTED_PROPERTY);

        var swivelProperty = state.getProperties().stream()
                .filter(swivelProperties::hasCycleFor)
                .filter(property -> swivelProperties.nextValueFor(property, state).isPresent())
                .min(Comparator.comparing(property -> !property.getName().equals(selectedPropName)));

        if (swivelProperty.isEmpty()) return TypedActionResult.pass(stack);
        if (world.isClient) return TypedActionResult.success(stack);

        var newState = nextState(state, swivelProperty.get());
        world.setBlockState(clickedBlock, newState);

        if (!newState.getFluidState().isEmpty()) {
            world.scheduleFluidTick(clickedBlock, newState.getFluidState().getFluid(), newState.getFluidState().getFluid().getTickRate(world));
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), AffinitySoundEvents.ITEM_SWIVEL_STAFF_SWIVEL, SoundCategory.PLAYERS);
        return TypedActionResult.success(stack);
    }

    @Override
    public boolean shouldHandleInteraction(ItemStack stack, World world, BlockPos pos, BlockState state) {
        return SWIVEL_PROPERTIES.containsKey(state.getBlock()) && SWIVEL_PROPERTIES.get(state.getBlock()).hasPermissibleCycleFor(state);
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return .25f;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable(this.getTranslationKey() + ".tooltip.consumption_per_spin", AETHUM_PER_ENTITY_SPIN));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static @Nullable BlockState nextState(BlockState state, Property<?> property) {
        var swivelProperties = SWIVEL_PROPERTIES.get(state.getBlock());
        if (swivelProperties == null) return null;

        return swivelProperties.nextValueFor(property, state)
                .map(comparable -> state.with((Property) property, (Comparable) comparable))
                .orElse(null);
    }

    public static List<Pair<Property<?>, String>> swivelProperties(BlockState state) {
        if (!SWIVEL_PROPERTIES.containsKey(state.getBlock())) return List.of();

        var result = new ArrayList<Pair<Property<?>, String>>();

        var properties = SWIVEL_PROPERTIES.get(state.getBlock());
        for (var property : state.getProperties()) {
            if (!properties.hasCycleFor(property)) continue;
            if (properties.nextValueFor(property, state).isEmpty()) continue;

            var cycle = properties.cyclesByProperty.get(property.getName());
            result.add(new Pair<>(property, cycle.translationKey));
        }

        return result;
    }

    private static void reloadProperties() {
        SWIVEL_PROPERTIES.clear();

        PropertyLoader.CACHED_DATA.forEach((identifier, json) -> {
            try {
                SwivelPropertiesDefinition.ENDEC.decodeFully(GsonDeserializer::of, json).store(SWIVEL_PROPERTIES);
            } catch (Exception e) {
                Affinity.LOGGER.warn("Error while loading swivel property definition '{}'", identifier, e);
            }
        });
        PropertyLoader.CACHED_DATA.clear();
    }

    private record SwivelPropertiesDefinition(TagKey<Block> tag, Map<String, PropertyCycle> cyclesByProperty) {
        public static final StructEndec<SwivelPropertiesDefinition> ENDEC = StructEndecBuilder.of(
                CodecUtils.toEndec(TagKey.codec(RegistryKeys.BLOCK)).fieldOf("tag", SwivelPropertiesDefinition::tag),
                PropertyCycle.ENDEC.mapOf().fieldOf("properties", SwivelPropertiesDefinition::cyclesByProperty),
                SwivelPropertiesDefinition::new
        );

        public void store(Map<Block, SwivelProperties> storage) {
            var properties = new SwivelProperties(this.cyclesByProperty);

            for (var entry : Registries.BLOCK.iterateEntries(this.tag)) {
                if (storage.containsKey(entry.value())) {
                    var newProperties = new SwivelProperties(new HashMap<>(storage.get(entry.value()).cyclesByProperty));
                    newProperties.cyclesByProperty.putAll(properties.cyclesByProperty);

                    storage.put(entry.value(), newProperties);
                } else {
                    storage.put(entry.value(), properties);
                }
            }
        }
    }

    private record PropertyCycle(String translationKey, Map<String, String> cycle, @Nullable StatePredicate predicate) {
        public static final StructEndec<PropertyCycle> ENDEC = StructEndecBuilder.of(
                Endec.STRING.fieldOf("translation_key", PropertyCycle::translationKey),
                Endec.STRING.mapOf().fieldOf("cycle", PropertyCycle::cycle),
                StatePredicate.ENDEC.optionalFieldOf("predicate", PropertyCycle::predicate, (StatePredicate) null),
                PropertyCycle::new
        );
    }

    private record SwivelProperties(Map<String, PropertyCycle> cyclesByProperty) {

        public boolean hasCycleFor(Property<?> property) {
            return this.cyclesByProperty.containsKey(property.getName());
        }

        public boolean hasPermissibleCycleFor(BlockState state) {
            for (var property : state.getProperties()) {
                var cycle = this.cyclesByProperty.get(property.getName());
                if (cycle == null) continue;

                if (nextValueFor(property, state).isPresent()) return true;
            }

            return false;
        }

        public <T extends Comparable<T>> Optional<T> nextValueFor(Property<T> property, BlockState state) {
            var cycle = this.cyclesByProperty.get(property.getName());
            if (cycle.predicate != null && !cycle.predicate.test(state)) return Optional.empty();

            var currentValueName = property.name(state.get(property));
            if (!cycle.cycle.containsKey(currentValueName)) return Optional.empty();

            return property.parse(cycle.cycle.get(currentValueName));
        }
    }

    public record SyncSwivelProperties(Map<Block, SwivelProperties> propertiesMap) {
        public static final StructEndec<SyncSwivelProperties> ENDEC = StructEndecBuilder.of(
                Endec.map(
                        MinecraftEndecs.ofRegistry(Registries.BLOCK),
                        StructEndecBuilder.of(
                                PropertyCycle.ENDEC.mapOf().fieldOf("cycles_by_property", SwivelProperties::cyclesByProperty),
                                SwivelProperties::new
                        )
                ).fieldOf("properties_map", SyncSwivelProperties::propertiesMap),
                SyncSwivelProperties::new
        );
    }

    static {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PropertyLoader());

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (client) return;
            reloadProperties();
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            AffinityNetwork.CHANNEL.serverHandle(player).send(new SyncSwivelProperties(SWIVEL_PROPERTIES));
        });

        StatePredicate.TYPES.put("has_property", HasPropertyPredicate.ENDEC);
        StatePredicate.TYPES.put("all_of", AllOfPredicate.ENDEC);
        StatePredicate.TYPES.put("any_of", AnyOfPredicate.ENDEC);
        StatePredicate.TYPES.put("none_of", NoneOfPredicate.ENDEC);

        AffinityNetwork.CHANNEL.registerClientbound(SyncSwivelProperties.class, SyncSwivelProperties.ENDEC, (message, access) -> {
            SWIVEL_PROPERTIES.clear();
            SWIVEL_PROPERTIES.putAll(message.propertiesMap);
        });
    }

    static {
        ClientDoItemUseCallback.EVENT.register((player, hand) -> {
            var playerStack = player.getStackInHand(hand);
            if (!(playerStack.getItem() instanceof SwivelStaffItem)) {
                return ActionResult.PASS;
            }

            var entity = InteractionUtil.raycastEntities(player, 1f, 7, .1f, $ -> true);
            if (entity == null) return ActionResult.PASS;

            var blockRaycast = player.raycast(7, 1f, false);
            if (blockRaycast.getType() != HitResult.Type.MISS && blockRaycast.squaredDistanceTo(player) < entity.squaredDistanceTo(player)) return ActionResult.PASS;

            var aethum = player.getComponent(AffinityComponents.PLAYER_AETHUM);
            if (!aethum.hasAethum(AETHUM_PER_ENTITY_SPIN)) return ActionResult.PASS;

            AffinityNetwork.CHANNEL.clientHandle().send(new SwivelEntityPacket(hand));
            return ActionResult.SUCCESS;
        });

        AffinityNetwork.CHANNEL.registerServerbound(SwivelEntityPacket.class, (message, access) -> {
            var playerStack = access.player().getStackInHand(message.hand);
            if (!(playerStack.getItem() instanceof SwivelStaffItem)) {
                return;
            }

            var result = InteractionUtil.raycastEntities(access.player(), 1f, 7, .1f, $ -> true);
            if (result == null) return;

            var blockRaycast = access.player().raycast(7, 1f, false);
            if (blockRaycast.getType() != HitResult.Type.MISS && blockRaycast.squaredDistanceTo(access.player()) < result.squaredDistanceTo(access.player())) return;

            var aethum = access.player().getComponent(AffinityComponents.PLAYER_AETHUM);
            if (!aethum.tryConsumeAethum(AETHUM_PER_ENTITY_SPIN)) return;

            var ticks = new MutableInt();
            ServerTasks.doFor(access.player().getServerWorld(), 30, () -> {
                int rotation = 40 - ticks.getValue();

                var entity = result.getEntity();
                entity.setYaw(entity.getYaw() + rotation);
                if (entity instanceof MobEntity mob) {
                    mob.setBodyYaw(mob.getBodyYaw() + rotation);
                    mob.setHeadYaw(mob.getHeadYaw() + rotation);
                }

                entity.velocityModified = true;
                entity.velocityDirty = true;

                ticks.increment();
                return true;
            }, () -> {});
        });
    }

    public record SwivelEntityPacket(Hand hand) {}

    public static final class PropertyLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

        private static final Map<Identifier, JsonElement> CACHED_DATA = new HashMap<>();

        public PropertyLoader() {
            super(new Gson(), "swivel_properties");
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
            CACHED_DATA.clear();
            CACHED_DATA.putAll(prepared);
        }

        @Override
        public Identifier getFabricId() {
            return Affinity.id("swivel_properties");
        }
    }

    private interface StatePredicate {

        BiMap<String, StructEndec<? extends StatePredicate>> TYPES = HashBiMap.create();
        Endec<StatePredicate> ENDEC = Endec.dispatchedStruct(Function.identity(), StatePredicate::endec, Endec.STRING.xmap(TYPES::get, TYPES.inverse()::get));

        boolean test(BlockState state);
        StructEndec<? extends StatePredicate> endec();
    }

    private record HasPropertyPredicate(String property, @Nullable String value) implements StatePredicate {

        private static final StructEndec<HasPropertyPredicate> ENDEC = StructEndecBuilder.of(
                Endec.STRING.fieldOf("property", HasPropertyPredicate::property),
                Endec.STRING.optionalFieldOf("value", HasPropertyPredicate::value, (String) null),
                HasPropertyPredicate::new
        );

        @Override
        @SuppressWarnings("rawtypes")
        public boolean test(BlockState state) {
            for (Property property : state.getProperties()) {
                if (!property.getName().equals(this.property)) continue;
                if (this.value == null || property.name(state.get(property)).equals(this.value)) return true;
            }

            return false;
        }

        @Override
        public StructEndec<? extends StatePredicate> endec() {
            return ENDEC;
        }
    }

    private record AllOfPredicate(List<StatePredicate> args) implements StatePredicate {
        private static final StructEndec<AllOfPredicate> ENDEC = StructEndecBuilder.of(
                StatePredicate.ENDEC.listOf().fieldOf("args", AllOfPredicate::args),
                AllOfPredicate::new
        );

        @Override
        public boolean test(BlockState state) {
            for (var predicate : this.args) {
                if (!predicate.test(state)) return false;
            }

            return true;
        }

        @Override
        public StructEndec<? extends StatePredicate> endec() {
            return ENDEC;
        }
    }

    private record AnyOfPredicate(List<StatePredicate> args) implements StatePredicate {
        private static final StructEndec<AnyOfPredicate> ENDEC = StructEndecBuilder.of(
                StatePredicate.ENDEC.listOf().fieldOf("args", AnyOfPredicate::args),
                AnyOfPredicate::new
        );

        @Override
        public boolean test(BlockState state) {
            for (var predicate : this.args) {
                if (predicate.test(state)) return true;
            }

            return false;
        }

        @Override
        public StructEndec<? extends StatePredicate> endec() {
            return ENDEC;
        }
    }

    private record NoneOfPredicate(List<StatePredicate> args) implements StatePredicate {
        private static final StructEndec<NoneOfPredicate> ENDEC = StructEndecBuilder.of(
                StatePredicate.ENDEC.listOf().fieldOf("args", NoneOfPredicate::args),
                NoneOfPredicate::new
        );

        @Override
        public boolean test(BlockState state) {
            for (var predicate : this.args) {
                if (predicate.test(state)) return false;
            }

            return true;
        }

        @Override
        public StructEndec<? extends StatePredicate> endec() {
            return ENDEC;
        }
    }
}
