package io.wispforest.affinity.blockentity.impl;

import com.mojang.authlib.GameProfile;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.quack.AffinityClientWorldExtension;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.worldmesher.WorldMesh;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;

import java.lang.ref.Cleaner;
import java.util.List;

public class HolographicStereopticonBlockEntity extends SyncedBlockEntity implements InWorldTooltipProvider, TickedBlockEntity, InteractableBlockEntity {

    @Environment(EnvType.CLIENT)
    private static final Cleaner MESH_CLEANER = Cleaner.create();

    public static final String IMPRINT_KIND_KEY_NAME = "ImprintKind";
    public static final NbtKey<NbtCompound> RENDERER_DATA_KEY = new NbtKey<>("RendererData", NbtKey.Type.COMPOUND);
    public static final NbtKey<Float> RENDER_SCALE_KEY = new NbtKey<>("RenderScale", NbtKey.Type.FLOAT);
    public static final NbtKey<Boolean> SPIN_KEY = new NbtKey<>("Spin", NbtKey.Type.BOOLEAN);

    @Environment(EnvType.CLIENT) private Renderer currentRenderer;
    @Environment(EnvType.CLIENT) private @Nullable Renderer nextRenderer;

    @Environment(EnvType.CLIENT) private long updateTimestamp = 0;
    @Environment(EnvType.CLIENT) private long refreshIn = 0;

    @Environment(EnvType.CLIENT) public float visualRenderScale = 0f;
    @Environment(EnvType.CLIENT) public float currentRotation = 0;

    private NbtCompound rendererData = null;
    private float renderScale = 1f;
    private boolean spin = true;

    public HolographicStereopticonBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.HOLOGRAPHIC_STEREOPTICON, pos, state);

        // i am frankly baffled why this is necessary
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
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
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.renderScale = nbt.get(RENDER_SCALE_KEY);
        this.spin = nbt.getOr(SPIN_KEY, true);
        this.rendererData = nbt.getOr(RENDERER_DATA_KEY, null);

        if (this.world != null && this.world.isClient) {
            this.refreshRenderer();
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putIfNotNull(RENDERER_DATA_KEY, this.rendererData);
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
    private void refreshRenderer() {
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
            entries.add(Entry.text(Text.empty(), Text.translatable(this.getCachedState().getBlock().getTranslationKey() + ".tooltip_assembling")));
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

    @Environment(EnvType.CLIENT)
    public interface Renderer {
        Renderer EMPTY = (scale, rotation, matrices, vertexConsumers, tickDelta, light, overlay) -> {};

        void render(float scale, float rotation, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light, int overlay);

        default boolean ready() {
            return true;
        }

        default int updateDelay() {
            return 0;
        }

        static Renderer read(HolographicStereopticonBlockEntity stereopticon, @Nullable NbtCompound nbt) {
            if (nbt == null) return EMPTY;
            return switch (nbt.getString(IMPRINT_KIND_KEY_NAME)) {
                case "item" -> {
                    var client = MinecraftClient.getInstance();
                    var item = ImprintKind.ITEM.readData(nbt);
                    if (item == null) yield EMPTY;

                    yield (scale, rotation, matrices, vertexConsumers, tickDelta, light, overlay) -> {
                        matrices.translate(.5f, .75f, .5f);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
                        matrices.scale(scale, scale, scale);

                        matrices.translate(0, client.getItemRenderer().getModel(item, client.world, null, 0).hasDepth() ? -.05 : .125, 0);

                        client.getItemRenderer().renderItem(item, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, client.world, 0);
                    };
                }
                case "block" -> {
                    var client = MinecraftClient.getInstance();
                    var data = ImprintKind.BLOCK.readData(nbt);
                    if (data == null) yield EMPTY;

                    BlockEntity entity;
                    if (data.nbt != null) {
                        entity = BlockEntity.createFromNbt(stereopticon.pos, data.state, data.nbt);
                        entity.setWorld(client.world);
                    } else {
                        entity = null;
                    }

                    yield (scale, rotation, matrices, vertexConsumers, tickDelta, light, overlay) -> {
                        matrices.translate(.5f, .75f, .5f);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
                        matrices.scale(.5f * scale, .5f * scale, .5f * scale);
                        matrices.translate(-.5f, 0, -.5f);

                        client.getBlockRenderManager().renderBlockAsEntity(data.state, matrices, vertexConsumers, light, overlay);
                        if (entity != null) {
                            client.getBlockEntityRenderDispatcher().renderEntity(entity, matrices, vertexConsumers, light, overlay);
                        }
                    };
                }
                case "entity" -> {
                    var client = MinecraftClient.getInstance();
                    var entity = ImprintKind.ENTITY.readData(nbt);
                    if (entity == null) yield EMPTY;

                    entity.updatePosition(stereopticon.pos.getX(), stereopticon.pos.getY(), stereopticon.pos.getZ());

                    yield (scale, rotation, matrices, vertexConsumers, tickDelta, light, overlay) -> {
                        matrices.translate(.5f, .75f, .5f);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
                        matrices.scale(.35f * scale, .35f * scale, .35f * scale);

                        client.getEntityRenderDispatcher().setRenderShadows(false);
                        client.getEntityRenderDispatcher().render(entity, 0, 0, 0, 0, 0, matrices, vertexConsumers, light);
                        client.getEntityRenderDispatcher().setRenderShadows(true);
                    };
                }
                case "section" -> {
                    var client = MinecraftClient.getInstance();

                    var data = ImprintKind.SECTION.readData(nbt);
                    if (data == null) yield EMPTY;

                    var mesh = new WorldMesh.Builder(client.world, data.start, data.end).build();
                    var updateDelay = (int) (mesh.dimensions().getXLength() * mesh.dimensions().getYLength() * mesh.dimensions().getZLength()) / 20000;

                    var realMeshDimensions = new Box(mesh.startPos(), mesh.endPos().add(1, 1, 1));
                    AffinityClientWorldExtension.BlockUpdateListener listener = (pos, from, to) -> {
                        if (!realMeshDimensions.contains(pos.getX(), pos.getY(), pos.getZ())) return true;
                        if (!client.getBakedModelManager().shouldRerender(from, to)) return true;

                        stereopticon.refreshRenderer();
                        return false;
                    };
                    client.send(() -> ((AffinityClientWorldExtension) client.world).affinity$addBlockUpdateListener(listener));

                    var renderer = new Renderer() {

                        private static int recursionDepth = 0;
                        private final AffinityClientWorldExtension.BlockUpdateListener noGcPlease = listener;

                        @Override
                        public void render(float scale, float rotation, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light, int overlay) {
                            if (!mesh.canRender()) return;

                            matrices.translate(.5f, .75f, .5f);
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));

                            float meshScale = Math.min(.75f, 3f / (int) Math.max(mesh.dimensions().getXLength(), Math.max(mesh.dimensions().getYLength(), mesh.dimensions().getZLength())));
                            matrices.scale(meshScale * scale, meshScale * scale, meshScale * scale);
                            matrices.translate(-.5 - mesh.dimensions().getXLength() / 2, 0, -.5 - mesh.dimensions().getZLength() / 2);

                            if (Affinity.CONFIG.renderBlockEntitiesInStereopticonSectionImprints()) {
                                MixinHooks.forceBlockEntityRendering = true;
                                mesh.renderInfo().blockEntities().forEach((blockPos, entity) -> {
                                    if (entity instanceof HolographicStereopticonBlockEntity && recursionDepth > Affinity.CONFIG.stereopticonSectionImprintRecursionLimit() - 1) {
                                        return;
                                    }

                                    matrices.push();
                                    matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                                    recursionDepth++;
                                    client.getBlockEntityRenderDispatcher().render(entity, tickDelta, matrices, vertexConsumers);
                                    recursionDepth--;

                                    matrices.pop();
                                });
                                MixinHooks.forceBlockEntityRendering = false;
                            }

                            if (Affinity.CONFIG.renderEntitiesInStereopticonSectionImprints()) {
                                client.world.getOtherEntities(null, realMeshDimensions).forEach(entity -> {
                                    var pos = entity.getLerpedPos(tickDelta).subtract(mesh.startPos().getX(), mesh.startPos().getY(), mesh.startPos().getZ());
                                    client.getEntityRenderDispatcher().render(entity, pos.x, pos.y, pos.z, entity.getYaw(tickDelta), tickDelta, matrices, vertexConsumers, light);
                                });
                            }

                            mesh.render(matrices);
                        }

                        @Override
                        public boolean ready() {
                            return mesh.canRender();
                        }

                        @Override
                        public int updateDelay() {
                            return updateDelay;
                        }
                    };

                    MESH_CLEANER.register(renderer, () -> MinecraftClient.getInstance().execute(mesh::reset));
                    yield renderer;
                }
                default -> EMPTY;
            };
        }
    }

    public static abstract class ImprintKind<T> {

        private static final String STEREOPTICON_TRANSLATION_KEY = AffinityBlocks.HOLOGRAPHIC_STEREOPTICON.getTranslationKey();

        public static final ImprintKind<BlockData> BLOCK = new ImprintKind<>("block") {
            @Override
            public @Nullable BlockData readData(NbtCompound nbt) {
                if (!nbt.contains("State", NbtElement.COMPOUND_TYPE)) return null;
                return new BlockData(
                        BlockState.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("State")).result().get().getFirst(),
                        nbt.contains("BlockEntityData", NbtElement.COMPOUND_TYPE) ? nbt.getCompound("BlockEntityData") : null
                );
            }

            @Override
            public void writeDataInner(NbtCompound nbt, BlockData data) {
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
            public @Nullable ItemStack readData(NbtCompound nbt) {
                if (!nbt.contains("Stack", NbtElement.COMPOUND_TYPE)) return null;
                return ItemStack.fromNbt(nbt.getCompound("Stack"));
            }

            @Override
            public void writeDataInner(NbtCompound nbt, ItemStack data) {
                nbt.put("Stack", data.writeNbt(new NbtCompound()));
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
            public @Nullable Entity readData(NbtCompound nbt) {
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
            public void writeDataInner(NbtCompound nbt, Entity data) {
                if (data instanceof EnderDragonPart dragonPart) data = dragonPart.owner;

                var entityNbt = new NbtCompound();
                if (data instanceof PlayerEntity player) {
                    entityNbt.putString("affinity:player_name", player.getEntityName());
                    entityNbt.putString("id", Registries.ENTITY_TYPE.getId(EntityType.PLAYER).toString());
                    data.writeNbt(entityNbt);
                } else {
                    data.saveSelfNbt(entityNbt);
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
            public @Nullable SectionData readData(NbtCompound nbt) {
                if (!nbt.contains("StartPos", NbtElement.LONG_TYPE) || !nbt.contains("EndPos", NbtElement.LONG_TYPE)) {
                    return null;
                }

                return new SectionData(
                        BlockPos.fromLong(nbt.getLong("StartPos")),
                        BlockPos.fromLong(nbt.getLong("EndPos"))
                );
            }

            @Override
            public void writeDataInner(NbtCompound nbt, SectionData data) {
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

        public abstract @Nullable T readData(NbtCompound nbt);

        protected abstract void writeDataInner(NbtCompound nbt, T data);
        public void writeData(NbtCompound nbt, T data) {
            nbt.putString(IMPRINT_KIND_KEY_NAME, this.id);
            this.writeDataInner(nbt, data);
        }

        protected abstract void appendTooltipInner(List<Text> tooltip, @Nullable T data);
        public void appendTooltip(List<Text> tooltip, @Nullable NbtCompound nbt) {
            this.appendTooltipInner(tooltip, nbt != null ? this.readData(nbt) : null);
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