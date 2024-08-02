package io.wispforest.affinity.blockentity.impl;

import com.mojang.authlib.GameProfile;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.component.EntityComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.wispforest.affinity.client.render.blockentity.HolographicStereopticonBlockEntityRenderer.Renderer;

public class HolographicStereopticonBlockEntity extends SyncedBlockEntity implements InWorldTooltipProvider, TickedBlockEntity, InteractableBlockEntity {

    public static final String IMPRINT_KIND_KEY_NAME = "ImprintKind";
    public static final String RENDERER_DATA_KEY = "RendererData";
    public static final KeyedEndec<Float> RENDER_SCALE_KEY = Endec.FLOAT.keyed("RenderScale", 1f);
    public static final KeyedEndec<Boolean> SPIN_KEY = Endec.BOOLEAN.keyed("Spin", true);

    @Environment(EnvType.CLIENT) private Renderer currentRenderer;
    @Environment(EnvType.CLIENT)
    private @Nullable Renderer nextRenderer;

    @Environment(EnvType.CLIENT) private long updateTimestamp;
    @Environment(EnvType.CLIENT) private long refreshIn;

    @Environment(EnvType.CLIENT) public float visualRenderScale;
    @Environment(EnvType.CLIENT) public float currentRotation;

    private NbtCompound rendererData = null;
    private float renderScale = 1f;
    private boolean spin = true;

    public HolographicStereopticonBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.HOLOGRAPHIC_STEREOPTICON, pos, state);

        if (Affinity.onClient()) {
            this.currentRenderer = Renderer.EMPTY;
        }
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);

        if (this.world != null && this.world.isClient) {
            this.refreshRenderer();
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.renderScale = nbt.get(RENDER_SCALE_KEY);
        this.spin = nbt.get(SPIN_KEY);
        this.rendererData = nbt.contains(RENDERER_DATA_KEY, NbtElement.COMPOUND_TYPE) ? nbt.getCompound(RENDERER_DATA_KEY) : null;

        if (this.world != null && this.world.isClient) {
            this.refreshRenderer();
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        if (this.rendererData != null) nbt.put(RENDERER_DATA_KEY, this.rendererData);
        nbt.put(RENDER_SCALE_KEY, this.renderScale);
        nbt.put(SPIN_KEY, this.spin);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void tickClient() {
        if (this.refreshIn > 0 && --this.refreshIn == 0) {
            this.refreshRenderer();
        }
    }

    @Environment(EnvType.CLIENT)
    public void refreshRenderer() {
        if (this.world.getTime() - this.updateTimestamp < this.currentRenderer.updateDelay()) {
            this.refreshIn = this.currentRenderer.updateDelay() - (this.world.getTime() - this.updateTimestamp);
            return;
        }

        var newRenderer = Renderer.read(this, this.rendererData);
        if (newRenderer.ready() || currentRenderer == Renderer.EMPTY) {
            this.currentRenderer = newRenderer;
        } else {
            this.nextRenderer = newRenderer;
        }

        this.updateTimestamp = this.world.getTime();
    }

    @Environment(EnvType.CLIENT)
    public Renderer renderer() {
        if (this.nextRenderer != null && this.nextRenderer.ready()) {
            this.currentRenderer = this.nextRenderer;
            this.nextRenderer = null;
        }

        return this.currentRenderer;
    }

    @Environment(EnvType.CLIENT)
    public float renderScale() {
        return this.renderScale;
    }

    @Environment(EnvType.CLIENT)
    public boolean spin() {
        return this.spin;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltipEntries(List<Entry> entries) {
        entries.add(Entry.text(
                this.spin ? TextOps.withColor("✔", 0x28FFBF) : TextOps.withColor("❌ ", 0xEB1D36),
                Text.translatable(this.getCachedState().getBlock().getTranslationKey() + ".tooltip_" + (this.spin ? "spin" : "dont_spin"))
        ));
        entries.add(Entry.icon(Text.literal(MathUtil.rounded(this.renderScale, 2) + "x"), 24, 0));

        if (!this.currentRenderer.ready()) {
            entries.add(Entry.text(Text.empty(), Text.translatable(this.getCachedState().getBlock().getTranslationKey() + ".tooltip.assembling")));
        }
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.isSneaking()) return ActionResult.PASS;

        this.spin = !this.spin;
        this.world.markDirty(this.pos);

        this.sendPropertyUpdate();
        return ActionResult.SUCCESS;
    }

    public void changeScale(boolean direction) {
        this.renderScale = MathHelper.clamp(this.renderScale + (direction ? .25f : -.25f), .25f, 5f);
        this.world.markDirty(this.pos);

        this.sendPropertyUpdate();
    }

    private void sendPropertyUpdate() {
        if (this.world.isClient) return;
        AffinityNetwork.server(this).send(new StereopticonPropertiesPacket(this.pos, this.spin, this.renderScale));
    }

    public static void initNetwork() {
        AffinityNetwork.CHANNEL.registerClientbound(HolographicStereopticonBlockEntity.StereopticonPropertiesPacket.class, (message, access) -> {
            if (!(access.runtime().world.getBlockEntity(message.pos) instanceof HolographicStereopticonBlockEntity stereopticon)) {
                return;
            }

            stereopticon.spin = message.spin;
            stereopticon.renderScale = message.renderScale;
        });
    }

    public record StereopticonPropertiesPacket(BlockPos pos, boolean spin, float renderScale) {}

    public static abstract class ImprintKind<T> {

        @SuppressWarnings("rawtypes")
        public static final Endec<ImprintKind> ENDEC = Endec.STRING.xmap(ImprintKind::byId, kind -> kind.id);

        private static final String STEREOPTICON_TRANSLATION_KEY = AffinityBlocks.HOLOGRAPHIC_STEREOPTICON.getTranslationKey();

        public static final ImprintKind<BlockData> BLOCK = new ImprintKind<>("block") {
            @Override
            public @Nullable BlockData readData(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
                if (!nbt.contains("State", NbtElement.COMPOUND_TYPE)) return null;
                return new BlockData(
                        BlockState.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("State")).result().get().getFirst(),
                        nbt.contains("BlockEntityData", NbtElement.COMPOUND_TYPE) ? nbt.getCompound("BlockEntityData") : null
                );
            }

            @Override
            public void writeDataInner(NbtCompound nbt, BlockData data, RegistryWrapper.WrapperLookup registries) {
                nbt.put("State", BlockState.CODEC.encodeStart(NbtOps.INSTANCE, data.state).result().get());
                if (data.nbt != null) nbt.put("BlockEntityData", data.nbt);
            }

            @Override
            public void appendTooltipInner(List<Text> tooltip, @Nullable BlockData data) {
                tooltip.add(Text.translatable(STEREOPTICON_TRANSLATION_KEY + ".imprint_block_hint"));
                if (data != null) {
                    tooltip.add(Text.empty());
                    tooltip.add(Text.translatable(STEREOPTICON_TRANSLATION_KEY + ".imprinted", data.state.getBlock().getName()));
                }
            }
        };

        public static final ImprintKind<ItemStack> ITEM = new ImprintKind<>("item") {
            @Override
            public @Nullable ItemStack readData(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
                if (!nbt.contains("Stack", NbtElement.COMPOUND_TYPE)) return null;
                return ItemStack.fromNbt(registries, nbt.getCompound("Stack")).orElse(ItemStack.EMPTY);
            }

            @Override
            public void writeDataInner(NbtCompound nbt, ItemStack stack, RegistryWrapper.WrapperLookup registries) {
                nbt.put("Stack", stack.encode(registries));
            }

            @Override
            public void appendTooltipInner(List<Text> tooltip, @Nullable ItemStack data) {
                tooltip.add(Text.translatable(STEREOPTICON_TRANSLATION_KEY + ".imprint_item_hint"));
                if (data != null) {
                    tooltip.add(Text.empty());
                    tooltip.add(Text.translatable(STEREOPTICON_TRANSLATION_KEY + ".imprinted", data.getName()));
                }
            }
        };

        public static final ImprintKind<Entity> ENTITY = new ImprintKind<>("entity") {
            @Override
            @Environment(EnvType.CLIENT)
            public @Nullable Entity readData(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
                if (!nbt.contains("Entity", NbtElement.COMPOUND_TYPE)) return null;
                var entityData = nbt.getCompound("Entity");

                var entityId = Identifier.tryParse(entityData.getString("id"));
                if (Registries.ENTITY_TYPE.getId(EntityType.PLAYER).equals(entityId)) {
                    var player = EntityComponent.createRenderablePlayer(new GameProfile(entityData.getUuid("UUID"), entityData.getString("affinity:player_name")));
                    player.readNbt(entityData);

                    return player;
                }

                return EntityType.getEntityFromNbt(entityData, MinecraftClient.getInstance().world).orElse(null);
            }

            @Override
            public void writeDataInner(NbtCompound nbt, Entity entity, RegistryWrapper.WrapperLookup registries) {
                if (entity instanceof EnderDragonPart dragonPart) entity = dragonPart.owner;

                var entityNbt = new NbtCompound();
                if (entity instanceof PlayerEntity player) {
                    entityNbt.putString("affinity:player_name", player.getNameForScoreboard());
                    entityNbt.putString("id", Registries.ENTITY_TYPE.getId(EntityType.PLAYER).toString());
                    entity.writeNbt(entityNbt);
                } else {
                    entity.saveSelfNbt(entityNbt);
                }

                nbt.put("Entity", entityNbt);
            }

            @Override
            public void appendTooltipInner(List<Text> tooltip, @Nullable Entity data) {
                tooltip.add(Text.translatable(STEREOPTICON_TRANSLATION_KEY + ".imprint_entity_hint"));
                if (data != null) {
                    tooltip.add(Text.empty());
                    tooltip.add(Text.translatable(STEREOPTICON_TRANSLATION_KEY + ".imprinted", data.getName()));
                }
            }
        };

        public static final ImprintKind<SectionData> SECTION = new ImprintKind<>("section") {
            @Override
            public @Nullable SectionData readData(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
                if (!nbt.contains("StartPos", NbtElement.LONG_TYPE) || !nbt.contains("EndPos", NbtElement.LONG_TYPE)) {
                    return null;
                }

                return new SectionData(
                        BlockPos.fromLong(nbt.getLong("StartPos")),
                        BlockPos.fromLong(nbt.getLong("EndPos"))
                );
            }

            @Override
            public void writeDataInner(NbtCompound nbt, SectionData data, RegistryWrapper.WrapperLookup registries) {
                nbt.putLong("StartPos", data.start.asLong());
                nbt.putLong("EndPos", data.end.asLong());
            }

            @Override
            public void appendTooltipInner(List<Text> tooltip, @Nullable SectionData data) {
                tooltip.add(Text.translatable(STEREOPTICON_TRANSLATION_KEY + ".imprint_section_hint"));
                if (data != null) {
                    tooltip.add(Text.empty());
                    tooltip.add(Text.translatable(
                            STEREOPTICON_TRANSLATION_KEY + ".imprinted",
                            Text.translatable(
                                    STEREOPTICON_TRANSLATION_KEY + ".imprinted.section_at",
                                    Text.literal("[" + data.start.getX() + " " + data.start.getY() + " " + data.start.getZ() + "]")
                            )
                    ));
                }
            }
        };

        public final String id;
        private ImprintKind(String id) {
            this.id = id;
        }

        public abstract @Nullable T readData(NbtCompound nbt, RegistryWrapper.WrapperLookup registries);

        protected abstract void writeDataInner(NbtCompound nbt, T data, RegistryWrapper.WrapperLookup registries);
        public void writeData(NbtCompound nbt, T data, RegistryWrapper.WrapperLookup registries) {
            nbt.putString(IMPRINT_KIND_KEY_NAME, this.id);
            this.writeDataInner(nbt, data, registries);
        }

        protected abstract void appendTooltipInner(List<Text> tooltip, @Nullable T data);
        public void appendTooltip(List<Text> tooltip, @Nullable NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            this.appendTooltipInner(tooltip, nbt != null ? this.readData(nbt, registries) : null);
        }

        public ImprintKind<?> next() {
            if (this == BLOCK) return ITEM;
            if (this == ITEM) return ENTITY;
            if (this == ENTITY) return SECTION;
            return BLOCK;
        }

        public static ImprintKind<?> byId(String id) {
            return switch (id) {
                default -> BLOCK;
                case "item" -> ITEM;
                case "entity" -> ENTITY;
                case "section" -> SECTION;
            };
        }
    }

    public record BlockData(BlockState state, @Nullable NbtCompound nbt) {}

    public record SectionData(BlockPos start, BlockPos end) {}
}