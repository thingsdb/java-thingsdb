package io.github.thingsdb.connector.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.github.thingsdb.connector.exceptions.CancelledBeforeCompletion;
import io.github.thingsdb.connector.exceptions.PackageIdNotFound;
import io.github.thingsdb.connector.exceptions.ProtoUnhandled;

public class RespMap {
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

    public void handle(Pkg pkg) throws PackageIdNotFound {
        switch (pkg.proto) {
            case ON_NODE_STATUS:
                break;
            case ON_WARN:
                break;
            case ON_ROOM_JOIN:
                break;
            case ON_ROOM_LEAVE:
                break;
            case ON_ROOM_EVENT:
                break;
            case ON_ROOM_DELETE:
                break;
            default:
                break;
        }

        Integer pid = Integer.valueOf(pkg.pid);
        CompletableFuture<Result> future = requests.get(pid);
        if (future == null) {
            throw new PackageIdNotFound(String.format("Package Id %d not found", pid));
        }

        switch (pkg.proto) {
            case RES_PONG:
                future.complete(Result.RESULT_PONG);
                return;
            case RES_OK:
                future.complete(Result.RESULT_OK);
                return;
            case RES_DATA:
                future.complete(Result.newResult(pkg.getData()));
                return;
            case RES_ERROR:
                future.completeExceptionally(TiError.fromData(pkg.getData()));
                return;
            default:
                Exception e = new ProtoUnhandled(String.format("Unhandled protocol (%s)", pkg.proto.toString()));
                future.completeExceptionally(e);
        }
    }
}
