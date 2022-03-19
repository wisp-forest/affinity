package io.wispforest.affinity.aethumflux.net;

public final class AethumLink {

    public enum Type {
        NORMAL("message.affinity.linking.started_create"),
        PUSH("message.affinity.linking.started_create_push");

        public final String translationKey;

        Type(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    public enum Element {
        NODE, MEMBER;

        public static Element of(AethumNetworkMember member) {
            return member instanceof AethumNetworkNode ? NODE : MEMBER;
        }
    }

    public enum Result {
        LINK_CREATED("message.affinity.linking.link_created"),
        NO_TARGET("message.affinity.linking.no_target"),
        ALREADY_LINKED("message.affinity.linking.already_linked"),
        OUT_OF_RANGE("message.affinity.linking.out_of_range"),
        TOO_MANY_LINKS("message.affinity.linking.too_many_links"),
        NOT_LINKED("message.affinity.linking.not_linked"),
        LINK_DESTROYED("message.affinity.linking.link_destroyed"),
        FAILED("message.affinity.linking.failed");

        public final String translationKey;

        Result(String translationKey) {
            this.translationKey = translationKey;
        }
    }

}
