package io.wispforest.affinity.component;

import io.wispforest.affinity.mixin.access.ChunkTicketManagerAccessor;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class WorldPinsComponent implements Component, ServerTickingComponent {

    private static final KeyedEndec<Map<BlockPos, Integer>> PINS = Endec.map(MinecraftEndecs.BLOCK_POS, Endec.INT).keyed("pins", HashMap::new);

    public static final ChunkTicketType<BlockPos> TICKET_TYPE = ChunkTicketType.create("affinity:world_pin", Comparator.comparingLong(BlockPos::asLong));

    private final World world;
    private final Map<BlockPos, Integer> pins = new HashMap<>();

    public WorldPinsComponent(World world) {
        this.world = world;
    }

    public static boolean shouldTick(ChunkTicketManager manager, ChunkPos pos) {
        var ticketSet = ((ChunkTicketManagerAccessor) manager).getTicketsByPosition().get(pos.toLong());

        if (ticketSet == null) return false;

        for (ChunkTicket<?> ticket : ticketSet) {
            if (ticket.getType() == WorldPinsComponent.TICKET_TYPE) {
                return true;
            }
        }

        return false;
    }

    public void addPin(BlockPos pin, int radius) {
        this.pins.put(pin, radius);

        this.addPinTickets(pin, radius);
    }

    public void removePin(BlockPos pin, int radius) {
        if (this.pins.remove(pin) != null) {
            ChunkPos.stream(new ChunkPos(pin), radius).forEach(chunkPos -> {
                ((ServerWorld) this.world).getChunkManager().removeTicket(TICKET_TYPE, chunkPos, 2, pin);
            });
        }
    }

    private void addPinTickets(BlockPos pin, int radius) {
        ChunkPos.stream(new ChunkPos(pin), radius).forEach(chunkPos -> {
            ((ServerWorld) this.world).getChunkManager().addTicket(TICKET_TYPE, chunkPos, 2, pin);
        });
    }

    public void addAllPins() {
        for (Map.Entry<BlockPos, Integer> pinEntry : this.pins.entrySet()) {
            this.addPinTickets(pinEntry.getKey(), pinEntry.getValue());
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        this.pins.clear();
        this.pins.putAll(tag.get(SerializationContext.suppressed(SerializationAttributes.HUMAN_READABLE), PINS));
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.put(SerializationContext.suppressed(SerializationAttributes.HUMAN_READABLE), PINS, this.pins);
    }

    @Override
    public void serverTick() {
        if (!this.pins.isEmpty()) {
            ((ServerWorld) world).resetIdleTimeout();
        }
    }
}
