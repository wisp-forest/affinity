package io.wispforest.affinity.component;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.onyxstudios.cca.api.v3.component.Component;
import io.wispforest.affinity.endec.BuiltInEndecs;
import io.wispforest.affinity.misc.util.EndecUtil;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.GlobalPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class EtherealNodeStorageComponent implements Component {

    private static final KeyedEndec<Map<GlobalPos, NodeState>> NODES_ENDEC = Endec.map(EndecUtil.GLOBAL_POS_ENDEC, NodeState.ENDEC).keyed("nodes", LinkedHashMap::new);
    private static final KeyedEndec<Map<GlobalPos, Text>> NODE_TO_NAME_ENDEC = Endec.map(EndecUtil.GLOBAL_POS_ENDEC, BuiltInEndecs.TEXT).keyed("node_to_name", HashMap::new);
    private static final KeyedEndec<Multimap<GlobalPos, GlobalPos>> NODE_TO_INJECTOR_ENDEC =
            Endec.map(EndecUtil.GLOBAL_POS_ENDEC, EndecUtil.GLOBAL_POS_ENDEC.listOf()).xmap(
                    blockPosListMap -> {
                        Multimap<GlobalPos, GlobalPos> multimap = HashMultimap.create();
                        blockPosListMap.forEach(multimap::putAll);
                        return multimap;
                    }, multimap -> {
                        var map = new HashMap<GlobalPos, List<GlobalPos>>();
                        multimap.asMap().forEach((node, injectors) -> map.put(node, new ArrayList<>(injectors)));
                        return map;
                    }
            ).keyed("node_to_injectors", HashMultimap::create);

    private Map<GlobalPos, NodeState> nodes = new LinkedHashMap<>();
    private Map<GlobalPos, Text> nodeToName = new HashMap<>();
    private Multimap<GlobalPos, GlobalPos> nodeToInjectors = HashMultimap.create();

    public void addNode(GlobalPos nodePos, @Nullable UUID owner, @Nullable Text name, boolean global) {
        this.nodes.put(nodePos, new NodeState(owner, global));
        if (name != null) {
            this.nodeToName.put(nodePos, name);
        } else {
            this.nodeToName.remove(nodePos);
        }
    }

    public void removeNode(GlobalPos nodePos) {
        this.nodes.remove(nodePos);
        this.nodeToInjectors.removeAll(nodePos);
        this.nodeToName.remove(nodePos);
    }

    public @Nullable UUID nodeOwner(GlobalPos nodePos) {
        if (!this.nodes.containsKey(nodePos)) return null;
        return this.nodes.get(nodePos).owner;
    }

    public @Nullable Text nodeName(GlobalPos nodePos) {
        if (!this.nodeToName.containsKey(nodePos)) return null;
        return this.nodeToName.get(nodePos);
    }

    public Stream<GlobalPos> listNodes(@NotNull UUID owner) {
        return this.nodes.entrySet().stream()
                .filter(entry -> {
                    var state = entry.getValue();
                    return owner.equals(state.owner);
                })
                .map(Map.Entry::getKey);
    }

    public Stream<GlobalPos> listGlobalNodes() {
        return this.nodes.entrySet().stream()
                .filter(entry -> {
                    var state = entry.getValue();
                    return state.owner == null || state.global;
                })
                .map(Map.Entry::getKey);
    }

    public @Nullable Collection<GlobalPos> listInjectors(GlobalPos nodePos) {
        if (!this.nodeToInjectors.containsKey(nodePos)) return null;
        return Collections.unmodifiableCollection(this.nodeToInjectors.get(nodePos));
    }

    public void addInjector(GlobalPos nodePos, GlobalPos injectorPos) {
        this.nodeToInjectors.put(nodePos, injectorPos);
    }

    public void removeInjector(GlobalPos nodePos, GlobalPos injectorPos) {
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
                io.wispforest.endec.impl.BuiltInEndecs.UUID.optionalFieldOf("owner", NodeState::owner, (UUID) null),
                Endec.BOOLEAN.fieldOf("global", NodeState::global),
                NodeState::new
        );
    }
}
