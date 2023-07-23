package io.wispforest.affinity.aethumflux.net;

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
}
