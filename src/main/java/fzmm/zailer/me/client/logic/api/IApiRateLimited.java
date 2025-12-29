package fzmm.zailer.me.client.logic.api;

import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface IApiRateLimited extends IApiBase {

    long nextAllowed();

    @Nullable
    Long parseDelayFromResponse(ApiResponse<?> response);

    void applyDelay(long value);

    default void applyDelay(ApiResponse<?> response, boolean isError) {
        long nextAllowed;
        if (isError) {
            nextAllowed = this.defaultDelayMillis();
        } else {
            Long delay = this.parseDelayFromResponse(response);
            nextAllowed = delay == null ? this.defaultDelayMillis() : delay;
        }

        this.applyDelay(Util.getEpochMillis() + nextAllowed);
    }

    default long defaultDelayMillis() {
        return 5000L;
    }

    @Override
    default void processResponse(ApiResponse<?> response, boolean isError) {
        this.applyDelay(response, isError);
    }

    default long getWaitMillis() {
        return Math.max(0, this.nextAllowed() - Util.getEpochMillis());
    }

    default CompletableFuture<Void> waitForNext() {
        long waitMillis = this.getWaitMillis();
        if (waitMillis == 0) return CompletableFuture.completedFuture(null);

        return this.waitFor(waitMillis);
    }

    default CompletableFuture<Void> waitFor(long waitMillis) {
        return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor(waitMillis, TimeUnit.MILLISECONDS));
    }
}
