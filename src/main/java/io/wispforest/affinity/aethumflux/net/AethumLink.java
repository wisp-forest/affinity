package io.wispforest.affinity.aethumflux.net;

import org.jetbrains.annotations.Nullable;

public final class AethumLink {

    public enum Type {
        NORMAL,
        PUSH;
    }

    public enum Element {
        NODE, MEMBER;

        public static Element of(AethumNetworkMember member) {
            return member instanceof AethumNetworkNode ? NODE : MEMBER;
        }
    }

    public enum Result {
        LINK_CREATED(null),
        NO_TARGET("message.affinity.linking.no_target"),
        ALREADY_LINKED("message.affinity.linking.already_linked"),
        OUT_OF_RANGE("message.affinity.linking.out_of_range"),
        TOO_MANY_LINKS("message.affinity.linking.too_many_links"),
        NOT_LINKED("message.affinity.linking.not_linked"),
        LINK_DESTROYED(null),
        FAILED("message.affinity.linking.failed");

        public final @Nullable String translationKey;

        Result(@Nullable String translationKey) {
            this.translationKey = translationKey;
        }
    }

}
