package io.wispforest.affinity.compat.owowhatsthis;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.BrewingCauldronBlockEntity;
import io.wispforest.affinity.blockentity.impl.ItemTransferNodeBlockEntity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.endec.BuiltInEndecs;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owowhatsthis.NumberFormatter;
import io.wispforest.owowhatsthis.OwoWhatsThis;
import io.wispforest.owowhatsthis.client.DisplayAdapters;
import io.wispforest.owowhatsthis.client.component.ColoredProgressBarComponent;
import io.wispforest.owowhatsthis.client.component.TexturedProgressBarComponent;
import io.wispforest.owowhatsthis.compat.OwoWhatsThisPlugin;
import io.wispforest.owowhatsthis.information.BlockStateWithPosition;
import io.wispforest.owowhatsthis.information.InformationProvider;
import io.wispforest.owowhatsthis.information.InformationProviders;
import io.wispforest.owowhatsthis.information.TargetType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class AffinityOwoWhatsThisPlugin implements OwoWhatsThisPlugin {

    static {
        PacketBufSerializer.register(PotionMixture.class, (buf, mixture) -> buf.write(PotionMixture.ENDEC, mixture), buf -> buf.read(PotionMixture.ENDEC));
        ReflectiveEndecBuilder.SHARED_INSTANCE.register(PotionMixture.ENDEC, PotionMixture.class);
    }

    @Override
    public void loadServer() {
        Registry.register(OwoWhatsThis.INFORMATION_PROVIDER, Affinity.id("block_aethum_flux_storage"), BLOCK_AETHUM_FLUX_STORAGE);
        Registry.register(OwoWhatsThis.INFORMATION_PROVIDER, Affinity.id("item_transfer_node_queue"), ITEM_TRANSFER_NODE_QUEUE);
        Registry.register(OwoWhatsThis.INFORMATION_PROVIDER, Affinity.id("brewing_cauldron_mixture"), BREWING_CAULDRON_MIXTURE);
    }

    @Override
    public void loadClient() {
        DisplayAdapters.register(BLOCK_AETHUM_FLUX_STORAGE, Client.AETHUM_STORAGE);
        DisplayAdapters.register(ITEM_TRANSFER_NODE_QUEUE, InformationProviders.DisplayAdapters.ITEM_STACK_LIST);
        DisplayAdapters.register(BREWING_CAULDRON_MIXTURE, Client.POTION_MIXTURE);
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

    public static final InformationProvider<BlockStateWithPosition, List<ItemStack>> ITEM_TRANSFER_NODE_QUEUE = InformationProvider.server(
            TargetType.BLOCK, true, 0,
            endecToBuf(BuiltInEndecs.ITEM_STACK.listOf()),
            (player, world, target) -> {
                if (!(world.getBlockEntity(target.pos()) instanceof ItemTransferNodeBlockEntity node)) return null;
                return node.displayItems();
            }
    );

    public static final InformationProvider<BlockStateWithPosition, BrewingCauldronData> BREWING_CAULDRON_MIXTURE = InformationProvider.server(
            TargetType.BLOCK, true, 0,
            BrewingCauldronData.class,
            (player, world, target) -> {
                if (!(world.getBlockEntity(target.pos()) instanceof BrewingCauldronBlockEntity cauldron)) return null;
                if (cauldron.storedPotion().isEmpty() || !cauldron.canPotionBeExtracted()) return null;

                return new BrewingCauldronData(cauldron.storedPotion(), cauldron.fillPercentage());
            }
    );

    public record AethumStorageData(long stored, long capacity) {}

    public record BrewingCauldronData(PotionMixture mixture, float fillLevel) {}

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

        public static final InformationProvider.DisplayAdapter<BrewingCauldronData> POTION_MIXTURE = data -> {
            return TexturedProgressBarComponent.ofSprite(
                    Text.translatable(data.mixture.basePotion().finishTranslationKey(Items.POTION.getTranslationKey() + ".effect.")),
                    data.fillLevel,
                    FluidVariantRendering.getSprite(FluidVariant.of(Fluids.WATER))
            ).color(Color.ofRgb(data.mixture.color()));
        };
    }

    private static <T> PacketBufSerializer<T> endecToBuf(Endec<T> endec) {
        return new PacketBufSerializer<>((packetByteBuf, t) -> packetByteBuf.write(endec, t), packetByteBuf -> packetByteBuf.read(endec));
    }
}
