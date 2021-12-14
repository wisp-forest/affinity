package io.wispforest.affinity.aethumflux.net;

public final class AethumLink {

    public enum Type {
        NORMAL("message.affinity.linking.started"),
        PUSH("message.affinity.linking.started_push");

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
        SUCCESS("message.affinity.linking.success"),
        NO_TARGET("message.affinity.linking.no_target"),
        ALREADY_LINKED("message.affinity.linking.already_linked"),
        OUT_OF_RANGE("message.affinity.linking.out_of_range"),
        TOO_MANY_LINKS("message.affinity.linking.too_many_links"),
        FAILED("message.affinity.linking.failed");

        public final String translationKey;

        Result(String translationKey) {
            this.translationKey = translationKey;
        }
    }

}
