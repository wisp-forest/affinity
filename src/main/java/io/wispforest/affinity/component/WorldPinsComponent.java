package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import io.wispforest.affinity.mixin.access.ChunkTicketManagerAccessor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class WorldPinsComponent implements Component {
    public static final ChunkTicketType<BlockPos> TICKET_TYPE = ChunkTicketType.create("affinity:world_pin", Comparator.comparingLong(BlockPos::asLong));

    private final World w;
    private final Map<BlockPos, Integer> pins = new HashMap<>();

    public WorldPinsComponent(World w) {
        this.w = w;
    }

    public static boolean shouldTick(ChunkTicketManager manager, ChunkPos pos) {
        var ticketSet = ((ChunkTicketManagerAccessor) manager).getTicketsByPosition().get(pos.toLong());

        if (ticketSet == null) return false;

        for (ChunkTicket<?> ticket : ticketSet) {
            if (ticket.getType() == WorldPinsComponent.TICKET_TYPE)
                return true;
        }

        return false;
    }

    public void addPin(BlockPos pin, int radius) {
        pins.put(pin, radius);

        addPinTickets(pin, radius);
    }

    public void removePin(BlockPos pin, int radius) {
        if (pins.remove(pin) != null) {
            ChunkPos.stream(new ChunkPos(pin), radius).forEach(chunkPos -> {
                ((ServerWorld) w).getChunkManager().removeTicket(TICKET_TYPE, chunkPos, 1, pin);
            });
        }
    }

    private void addPinTickets(BlockPos pin, int radius) {
        ChunkPos.stream(new ChunkPos(pin), radius).forEach(chunkPos -> {
            ((ServerWorld) w).getChunkManager().addTicket(TICKET_TYPE, chunkPos, 1, pin);
        });
    }

    public void addAllPins() {
        for (Map.Entry<BlockPos, Integer> pinEntry : pins.entrySet()) {
            addPinTickets(pinEntry.getKey(), pinEntry.getValue());
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        NbtList pinsTag = tag.getList("Pins", NbtElement.COMPOUND_TYPE);

        pins.clear();
        for (int i = 0; i < pinsTag.size(); i++) {
            NbtCompound pinTag = pinsTag.getCompound(i);

            var pos = NbtHelper.toBlockPos(pinTag.getCompound("PinPos"));
            var radius = pinTag.getInt("Radius");

            pins.put(pos, radius);
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtList pinsTag = new NbtList();
        tag.put("Pins", pinsTag);

        for (Map.Entry<BlockPos, Integer> entry : pins.entrySet()) {
            NbtCompound pinTag = new NbtCompound();
            pinsTag.add(pinTag);

            pinTag.put("PinPos", NbtHelper.fromBlockPos(entry.getKey()));
            pinTag.putInt("Radius", entry.getValue());
        }
    }
}
