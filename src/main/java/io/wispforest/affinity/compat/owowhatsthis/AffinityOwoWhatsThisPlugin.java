package io.wispforest.affinity.compat.owowhatsthis;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.ItemTransferNodeBlockEntity;
import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.item.NimbleStaffItem;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owowhatsthis.NumberFormatter;
import io.wispforest.owowhatsthis.OwoWhatsThis;
import io.wispforest.owowhatsthis.client.DisplayAdapters;
import io.wispforest.owowhatsthis.client.component.ColoredProgressBarComponent;
import io.wispforest.owowhatsthis.compat.OwoWhatsThisPlugin;
import io.wispforest.owowhatsthis.information.BlockStateWithPosition;
import io.wispforest.owowhatsthis.information.InformationProvider;
import io.wispforest.owowhatsthis.information.InformationProviders;
import io.wispforest.owowhatsthis.information.TargetType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

import java.util.List;

public class AffinityOwoWhatsThisPlugin implements OwoWhatsThisPlugin {

    @Override
    public void loadServer() {
        Registry.register(OwoWhatsThis.INFORMATION_PROVIDER, Affinity.id("block_aethum_flux_storage"), BLOCK_AETHUM_FLUX_STORAGE);
        Registry.register(OwoWhatsThis.INFORMATION_PROVIDER, Affinity.id("nimble_staff_direction"), NIMBLE_STAFF_DIRECTION);
        Registry.register(OwoWhatsThis.INFORMATION_PROVIDER, Affinity.id("item_transfer_node_queue"), ITEM_TRANSFER_NODE_QUEUE);
    }

    @Override
    public void loadClient() {
        DisplayAdapters.register(BLOCK_AETHUM_FLUX_STORAGE, Client.AETHUM_STORAGE);
        DisplayAdapters.register(NIMBLE_STAFF_DIRECTION, InformationProviders.DisplayAdapters.TEXT);
        DisplayAdapters.register(ITEM_TRANSFER_NODE_QUEUE, InformationProviders.DisplayAdapters.ITEM_STACK_LIST);
    }

    public static final InformationProvider<BlockStateWithPosition, AethumStorageData> BLOCK_AETHUM_FLUX_STORAGE = InformationProvider.server(
            TargetType.BLOCK,
            true, 0, AethumStorageData.class,
            (player, world, target) -> {
                var member = Affinity.AETHUM_MEMBER.find(world, target.pos(), null);
                if (member == null) return null;

                return member instanceof AethumNetworkMemberBlockEntity be
                        ? new AethumStorageData(be.displayFlux(), be.displayFluxCapacity())
                        : new AethumStorageData(member.flux(), member.fluxCapacity());
            }
    );

    public static final InformationProvider<BlockStateWithPosition, Text> NIMBLE_STAFF_DIRECTION = InformationProvider.client(
            TargetType.BLOCK, 0,
            (player, world, target) -> {
                if (!(world.getBlockEntity(target.pos()) instanceof StaffPedestalBlockEntity pedestal)) return null;

                var item = pedestal.getItem();
                if (!(item.isOf(AffinityItems.NIMBLE_STAFF))) return null;

                var direction = NimbleStaffItem.getDirection(item);
                return Text.translatable(item.getTranslationKey() + ".direction." + direction.asString());
            }
    );

    @SuppressWarnings("unchecked")
    public static final InformationProvider<BlockStateWithPosition, List<ItemStack>> ITEM_TRANSFER_NODE_QUEUE = InformationProvider.server(
            TargetType.BLOCK, true, 0,
            (PacketBufSerializer<List<ItemStack>>) (Object) PacketBufSerializer.createCollectionSerializer(List.class, ItemStack.class),
            (player, world, target) -> {
                if (!(world.getBlockEntity(target.pos()) instanceof ItemTransferNodeBlockEntity node)) return null;
                return node.displayItems();
            }
    );

    public record AethumStorageData(long stored, long capacity) {}

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static final InformationProvider.DisplayAdapter<AethumStorageData> AETHUM_STORAGE = data -> {
            final var fuelText = Text.translatable(
                    "text.affinity.tooltip.aethum_storage",
                    NumberFormatter.quantityText(data.stored, ""),
                    NumberFormatter.quantityText(data.capacity, "")
            );

            return new ColoredProgressBarComponent(fuelText)
                    .progress(data.stored / (float) data.capacity)
                    .color(Affinity.AETHUM_FLUX_COLOR);
        };
    }
}
