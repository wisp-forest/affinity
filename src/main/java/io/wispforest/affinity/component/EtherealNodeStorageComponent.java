package io.wispforest.affinity.component;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.onyxstudios.cca.api.v3.component.Component;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class EtherealNodeStorageComponent implements Component {

    private static final KeyedEndec<Map<BlockPos, NodeState>> NODES_ENDEC = Endec.map(BuiltInEndecs.BLOCK_POS, NodeState.ENDEC).keyed("nodes", LinkedHashMap::new);
    private static final KeyedEndec<Map<BlockPos, Text>> NODE_TO_NAME_ENDEC = Endec.map(BuiltInEndecs.BLOCK_POS, BuiltInEndecs.TEXT).keyed("node_to_name", HashMap::new);
    private static final KeyedEndec<Multimap<BlockPos, BlockPos>> NODE_TO_INJECTOR_ENDEC =
            Endec.map(BuiltInEndecs.BLOCK_POS, BuiltInEndecs.BLOCK_POS.listOf()).xmap(
                    blockPosListMap -> {
                        Multimap<BlockPos, BlockPos> multimap = HashMultimap.create();
                        blockPosListMap.forEach(multimap::putAll);
                        return multimap;
                    }, multimap -> {
                        var map = new HashMap<BlockPos, List<BlockPos>>();
                        multimap.asMap().forEach((node, injectors) -> map.put(node, new ArrayList<>(injectors)));
                        return map;
                    }
            ).keyed("node_to_injectors", HashMultimap::create);

    private Map<BlockPos, NodeState> nodes = new LinkedHashMap<>();
    private Map<BlockPos, Text> nodeToName = new HashMap<>();
    private Multimap<BlockPos, BlockPos> nodeToInjectors = HashMultimap.create();

    public void addNode(BlockPos nodePos, @Nullable UUID owner, @Nullable Text name, boolean global) {
        this.nodes.put(nodePos, new NodeState(owner, global));
        if (name != null) {
            this.nodeToName.put(nodePos, name);
        } else {
            this.nodeToName.remove(nodePos);
        }
    }

    public void removeNode(BlockPos nodePos) {
        this.nodes.remove(nodePos);
        this.nodeToInjectors.removeAll(nodePos);
        this.nodeToName.remove(nodePos);
    }

    public @Nullable UUID nodeOwner(BlockPos nodePos) {
        if (!this.nodes.containsKey(nodePos)) return null;
        return this.nodes.get(nodePos).owner;
    }

    public @Nullable Text nodeName(BlockPos nodePos) {
        if (!this.nodeToName.containsKey(nodePos)) return null;
        return this.nodeToName.get(nodePos);
    }

    public Stream<BlockPos> listNodes(@NotNull UUID owner) {
        return this.nodes.entrySet().stream()
                .filter(entry -> {
                    var state = entry.getValue();
                    return owner.equals(state.owner);
                })
                .map(Map.Entry::getKey);
    }

    public Stream<BlockPos> listGlobalNodes() {
        return this.nodes.entrySet().stream()
                .filter(entry -> {
                    var state = entry.getValue();
                    return state.owner == null || state.global;
                })
                .map(Map.Entry::getKey);
    }

    public @Nullable Collection<BlockPos> listInjectors(BlockPos nodePos) {
        if (!this.nodeToInjectors.containsKey(nodePos)) return null;
        return Collections.unmodifiableCollection(this.nodeToInjectors.get(nodePos));
    }

    public void addInjector(BlockPos nodePos, BlockPos injectorPos) {
        this.nodeToInjectors.put(nodePos, injectorPos);
    }

    public void removeInjector(BlockPos nodePos, BlockPos injectorPos) {
        this.nodeToInjectors.remove(nodePos, injectorPos);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.nodes = new LinkedHashMap<>(tag.get(NODES_ENDEC));
        this.nodeToName = new HashMap<>(tag.get(NODE_TO_NAME_ENDEC));
        this.nodeToInjectors = tag.get(NODE_TO_INJECTOR_ENDEC);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.put(NODES_ENDEC, this.nodes);
        tag.put(NODE_TO_NAME_ENDEC, this.nodeToName);
        tag.put(NODE_TO_INJECTOR_ENDEC, this.nodeToInjectors);
    }

    private record NodeState(@Nullable UUID owner, boolean global) {
        public static final Endec<NodeState> ENDEC = StructEndecBuilder.of(
                BuiltInEndecs.UUID.optionalFieldOf("owner", NodeState::owner, (UUID) null),
                Endec.BOOLEAN.fieldOf("global", NodeState::global),
                NodeState::new
        );
    }
}
