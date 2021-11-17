package io.github.thingsdb.connector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.github.thingsdb.connector.exceptions.CancelledBeforeCompletion;

class RespMap {
    private final Map<Integer, CompletableFuture<Result>> requests;

    public RespMap() {
        requests =  Collections.synchronizedMap(new HashMap<>());
    }

    public CompletableFuture<Result> register(Integer pid) {
        CompletableFuture<Result> future = new CompletableFuture<>();
        CompletableFuture<Result> prev = requests.remove(pid);
        if (prev != null) {
            prev.completeExceptionally(new CancelledBeforeCompletion("futurue is overwritten by a new package before completion"));
        }
        requests.put(pid, future);
        return future;
    }

    public CompletableFuture<Result> get(Integer pid) {
        return requests.get(pid);
    }
}
