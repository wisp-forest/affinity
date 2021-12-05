package io.wispforest.affinity.blockentity;

public class AetherLink {

    public enum Element {
        NODE, MEMBER;

        public static Element of(AetherNetworkMember member) {
            return member instanceof AetherNetworkNode ? NODE : MEMBER;
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

        private Result(String translationKey) {
            this.translationKey = translationKey;
        }
    }

}
