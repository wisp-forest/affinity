package io.wispforest.affinity.blockentity.template;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface LinkableBlockEntity {

    Optional<String> beginLink(PlayerEntity player, NbtCompound linkData);

    Optional<LinkResult> finishLink(PlayerEntity player, BlockPos linkTo, NbtCompound linkData);

    Optional<LinkResult> destroyLink(PlayerEntity player, BlockPos destroyFrom, NbtCompound linkData);

    record LinkResult(@Nullable String messageTranslationKey) {
        public static final LinkResult LINK_CREATED = new LinkResult(null);
        public static final LinkResult LINK_DESTROYED = new LinkResult(null);

        public static final LinkResult NO_TARGET = new LinkResult("message.affinity.linking.no_target");
        public static final LinkResult ALREADY_LINKED = new LinkResult("message.affinity.linking.already_linked");
        public static final LinkResult OUT_OF_RANGE = new LinkResult("message.affinity.linking.out_of_range");
        public static final LinkResult TOO_MANY_LINKS = new LinkResult("message.affinity.linking.too_many_links");
        public static final LinkResult NOT_LINKED = new LinkResult("message.affinity.linking.not_linked");
        public static final LinkResult FAILED = new LinkResult("message.affinity.linking.failed");
    }

}
