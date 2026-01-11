package io.wispforest.affinity.misc.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FuturesUtil {

    public static CompletableFuture<Void> allOf(List<CompletableFuture> futuresList) {
        return CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
    }
}
